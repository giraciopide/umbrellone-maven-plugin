package dev.dimlight.umbrellone.shade;

import dev.dimlight.umbrellone.util.MoreStrings;
import io.vavr.collection.List;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import javax.lang.model.SourceVersion;

public class ShadePluginConfigurationGenerator {

    private final Log log;
    private final MavenSession session;
    private final MavenProject project;
    private final DependencyGraphBuilder dependencyGraphBuilder;
    private final List<MavenProject> reactorProjects;

    public ShadePluginConfigurationGenerator(Log log, MavenSession session, MavenProject project, DependencyGraphBuilder dependencyGraphBuilder, List<MavenProject> reactorProjects) {
        this.log = log;
        this.session = session;
        this.project = project;
        this.dependencyGraphBuilder = dependencyGraphBuilder;
        this.reactorProjects = reactorProjects;
    }

    /**
     * @return a string that is fit to be used as a package name in java.
     */
    public static void checkPrefixValidity(String pkgPrefix) throws MojoFailureException {
        final String prefix = MoreStrings.removeSuffix(pkgPrefix, ".");
        if (!SourceVersion.isName(prefix)) {
            throw new MojoFailureException("The package prefix [" + prefix + "] is not a valid name for a java package");
        }
    }

    public ShadePluginConfiguration generateShadePluginConfiguration(String relocationPrefix) throws MojoExecutionException, MojoFailureException {
        return generateShadePluginConfiguration(artifact -> artifact.getType().equalsIgnoreCase("jar") && !artifact.getScope().equalsIgnoreCase("test"), relocationPrefix);
    }

    public ShadePluginConfiguration generateShadePluginConfiguration(ArtifactFilter artifactFilter, String relocationPrefix) throws MojoExecutionException, MojoFailureException {
        checkPrefixValidity(relocationPrefix);

        final PackageUtils pkgUtils = PackageUtils.of(log);
        final DependencyUtils depUtils = DependencyUtils.of(log, session, project, dependencyGraphBuilder, reactorProjects.toJavaList());

        final List<DependencyNode> dependencies = List.ofAll(depUtils.getAllDependencies(artifactFilter));

        final List<String> rootPackages = dependencies
                .peek(depNode -> log.info("found dependency [" + depNode.toNodeString() + "]"))
                .map(depNode -> pkgUtils.scanForPackages(depNode))
                .distinct()
                .flatMap(pkgs -> pkgUtils.rootPackages(pkgs))
                .distinct();

        rootPackages.forEach(rootPkg -> log.info("found root pkg: [" + rootPkg + "]"));

        final List<RelocationRule> rules = pkgUtils.simplifyPackages(rootPackages)
                .map(pkg -> RelocationRule.of(pkg, relocationPrefix + pkg));

        return ShadePluginConfiguration.ofDependencies(dependencies, rules);
    }
}
