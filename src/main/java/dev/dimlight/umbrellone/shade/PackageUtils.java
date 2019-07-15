package dev.dimlight.umbrellone.shade;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import dev.dimlight.umbrellone.util.MoreStrings;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.TreeSet;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Helper to reason about packages hierarchies.
 *
 * @author Marco Nicolini
 */
public class PackageUtils {

    private static final String PATH_SEP = "/";
    private final Log log;

    private PackageUtils(Log log) {
        this.log = log;
    }

    public static PackageUtils of(Log log) {
        return new PackageUtils(log);
    }

    /**
     * Given a dependency node, scans its jar file and extract the complete list of packages contained therein.
     * @param node the maven dependency node from which to get the location of the artifact
     * @return the complete list of fully qualified java packages found in the artifact of the given dependency node.
     */
    public Set<String> scanForPackages(DependencyNode node) {
        final java.util.Set<String> directories = new java.util.TreeSet<>();
        final java.util.Set<String> directoriesWithFiles = new java.util.TreeSet<>();

        log.info("searching for packages in [" + node.getArtifact().getFile() + "]");
        try (JarFile jar = new JarFile(node.getArtifact().getFile())) {

            final Enumeration<JarEntry> en = jar.entries();
            while (en.hasMoreElements()) {
                JarEntry entry = en.nextElement();
                String entryName = entry.getName();

                if (entryName == null) {
                    continue;
                }

                if (entryName.endsWith(PATH_SEP)) { // it's a directory. Might be considered a package if it has files in it
                    directories.add(MoreStrings.removeSuffix(entryName, "/"));
                    continue;
                }

                if (entryName.startsWith("META-INF")) { // skip meta inf.
                    continue;
                }

                final String parentDir = baseName(entryName);
                if (directories.contains(parentDir)) {
                    directoriesWithFiles.add(parentDir);
                }

            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return directoriesWithFiles.stream()
                .map(PackageUtils::dirToPkgName)
                .collect(TreeSet.collector(String::compareTo));
    }

    private static String baseName(String entry) {
        return MoreStrings.splitOnLast(entry, PATH_SEP)._1;
    }

    private static String dirToPkgName(String dir) {
        return dir.replaceAll(PATH_SEP, ".");
    }

    /**
     * @param packageNames  a list of fully qualified java package names (i.e. "org.junit", "com.google.common").
     * @return the minimal list of packages that is able to contain all of them.
     *         The root package is present in this list only if it's present in the output. // FIXME this makes no sense.
     */
    public Set<String> rootPackages(Iterable<String> packageNames) {
        final Node root = Node.of("root");
        List.ofAll(packageNames)
                .map(PackageUtils::breakApart)
                .map(p -> p.prepend("root")) // common fake root to all packages so that they fall under it.
                .forEachWithIndex((p, i) -> root.addTail(p.tail()));

        return Node.findRootPackages(root, List.of("root"))
                .map(p -> MoreStrings.removePrefix("root.", p));
    }

    private static List<String> breakApart(String packageName) {
        return List.ofAll(Splitter.on(".").trimResults().split(packageName));
    }

    private static String glueBack(List<String> packageName) {
        return Joiner.on(".").join(packageName);
    }

    /**
     * Represents a node in a fully qualified package name.
     */
    private static class Node {
        static final Comparator<Node> COMPARATOR = Comparator.comparing(node -> node.value);
        String value;
        Set<Node> children = TreeSet.empty(COMPARATOR);

        static Node of(String value) {
            final Node node = new Node();
            node.value = value;
            return node;
        }

        void addTail(List<String> pkg) {
            if (pkg.isEmpty()) {
                return;
            }

            // either the first child already exists or needs to be created.
            final Node nodeOnWhichToAdd = children
                    .find(child -> child.value.equals(pkg.head()))
                    .getOrElse(() -> Node.of(pkg.head()));

            this.children = this.children.add(nodeOnWhichToAdd);
            nodeOnWhichToAdd.addTail(pkg.tail());
        }

        /**
         * Visiting method that collects all the "root" packages from the package node tree
         *
         * @param node the root from which to start traversing
         * @param currentPath the dotted.joined.path of the current node, i.e. List.of("root")
         * @return the list of root packages.
         */
        private static Set<String> findRootPackages(Node node, List<String> currentPath) {
            // base case
            // stop when you encounter zero o >1 child, but don't stop at the root.
            if (node.children.size() != 1 && currentPath.size() > 1) {
                return TreeSet.of(glueBack(currentPath));

            // go deep in every child
            } else {
                return node.children.flatMap(child -> findRootPackages(child, currentPath.append(child.value)));
            }
        }
    }

    /**
     * Similar to root packages but won't ever return package that was not in the list to beging with.
     * E.g. given ('a.b', 'a.c', 'a.b.c' will return ('a.b', 'a.c), not ('a') like {@link #rootPackages(Iterable)} would do.
     *
     * @return  a new list of packages excluding the packages that could have been contained in another package already
     *          present in the given list
     */
    public List<String> simplifyPackages(Iterable<String> packageNames) {
        // this implementation is naive and not optimal.

        // we check all packages in couples and discard any package that can be contained in another.
        final Set<String> all = TreeSet.ofAll(String::compareTo, Objects.requireNonNull(packageNames));
        Set<String> discardedPkgs = TreeSet.empty(String::compareTo);

        for (Tuple2<String, String> pp : List.ofAll(packageNames).crossProduct()) {
            Preconditions.checkState(!pp._1.isEmpty()); // right now we don't support the root package here.

            if (pp._1.startsWith(pp._2 + ".")) { // 1 is contained in 2
                log.info("package [" + pp._1 + "] will be omitted because is contained by [" + pp._2 + "]");
                discardedPkgs = discardedPkgs.add(pp._1);
            }
        }

        return all.diff(discardedPkgs).toList();
    }
}
