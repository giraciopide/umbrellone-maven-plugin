package dev.dimlight.umbrellone.mojo;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import dev.dimlight.umbrellone.util.MoreStrings;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

import javax.lang.model.SourceVersion;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Marco Nicolini
 */
public abstract class AbstractBaseMojo extends AbstractMojo {

    /**
     * A prefix for the relocated packages.
     */
    @Parameter(property = "relocationPrefix", defaultValue = "relocated.", required = true)
    protected String relocationPrefix;

    /**
     * Artifacts to include/exclude from the final artifact. Artifacts are denoted by composite identifiers of the
     * general form <code>groupId:artifactId:type:classifier</code>. Since version 1.3, the wildcard characters '*' and
     * '?' can be used within the sub parts of those composite identifiers to do pattern matching. For convenience, the
     * syntax <code>groupId</code> is equivalent to <code>groupId:*:*:*</code>, <code>groupId:artifactId</code> is
     * equivalent to <code>groupId:artifactId:*:*</code> and <code>groupId:artifactId:classifier</code> is equivalent to
     * <code>groupId:artifactId:*:classifier</code>. For example:
     *
     * <pre>
     * &lt;artifactSet&gt;
     *   &lt;includes&gt;
     *     &lt;include&gt;org.apache.maven:*&lt;/include&gt;
     *   &lt;/includes&gt;
     *   &lt;excludes&gt;
     *     &lt;exclude&gt;*:maven-core&lt;/exclude&gt;
     *   &lt;/excludes&gt;
     * &lt;/artifactSet&gt;
     * </pre>
     */
    @Parameter
    private ArtifactSet artifactSet;

    /**
     * The current Maven session.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    protected MavenSession session;

    /**
     * The current Maven project.
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true)
    protected MavenProject project;

    /**
     * Contains the full list of projects in the reactor.
     */
    @Parameter(defaultValue = "${reactorProjects}", readonly = true, required = true)
    protected List<MavenProject> reactorProjects;

    @Component
    protected MavenProjectHelper projectHelper;

    @Component
    protected DependencyGraphBuilder dependencyGraphBuilder;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateConfiguration();
        doExecute();
    }

    /**
     * Will be executed after configuration validation.
     */
    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    /**
     * @return the correct artifact filter to use in generating the configuration of the shade plugin
     */
    protected ArtifactFilter getArtifactFilter() {
        final ArtifactFilter excludingArtifactFilter = artifactSet != null ? new ExcludesArtifactFilter(new ArrayList<>(artifactSet.getExcludes())) : ArtifactFilters.INCLUDE_ALL;

        // combine the (not-configurable) exclusion of test and non-jar artifacts, with the user provided excludes.
        return ArtifactFilters.and(ArtifactFilters.NOT_TEST_NOR_JAR, excludingArtifactFilter);
    }

    private void validateConfiguration() throws MojoFailureException {
        validatePackagePrefix();
    }

    private void validatePackagePrefix() throws MojoFailureException {
        if (Strings.isNullOrEmpty(relocationPrefix)) {
            throw new MojoFailureException("Relocation prefix cannot be null or empty");
        }

        final String prefix = MoreStrings.removeSuffix(relocationPrefix, ".");


        final LinkedList<String> prefixFragments = new LinkedList<>(Splitter.on(".").splitToList(prefix));

        if (prefixFragments.isEmpty()) {
            throw new MojoFailureException("Relocation prefix is invalid (made only of dots???!?)");
        }

        // if it ended with a "." is ok, but the last fragment will be null/empty so we don't have to validate it.
        if (Strings.isNullOrEmpty(prefixFragments.getLast())) {
            prefixFragments.removeLast();
        }

        final Set<String> invalidFragments = prefixFragments.stream()
                .filter(pf -> !SourceVersion.isName(pf))
                .collect(Collectors.toSet());

        if (!invalidFragments.isEmpty()) {
            throw new MojoFailureException("Relocation prefix contains invalid package names: [" + invalidFragments + "]");
        }
    }
}
