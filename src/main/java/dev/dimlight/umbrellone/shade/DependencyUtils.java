package dev.dimlight.umbrellone.shade;

import com.google.common.collect.ImmutableList;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Methods to walk and collect all dependencies for the project.
 *
 * @author Marco Nicolini
 */
public class DependencyUtils {

    private final Log log;
    private final MavenSession session;
    private final MavenProject project;
    private final DependencyGraphBuilder dependencyGraphBuilder;
    private final List<MavenProject> reactorProjects;

    private DependencyUtils(Log log, MavenSession session, MavenProject project, DependencyGraphBuilder dependencyGraphBuilder, List<MavenProject> reactorProjects) {
        this.log = Objects.requireNonNull(log);
        this.session = Objects.requireNonNull(session);
        this.project = Objects.requireNonNull(project);
        this.dependencyGraphBuilder = Objects.requireNonNull(dependencyGraphBuilder);
        this.reactorProjects = Objects.requireNonNull(reactorProjects);
    }

    public static DependencyUtils of(Log log,
                                     MavenSession session,
                                     MavenProject project,
                                     DependencyGraphBuilder dependencyGraphBuilder,
                                     List<MavenProject> reactorProjects) {
        return new DependencyUtils(log, session, project, dependencyGraphBuilder, reactorProjects);
    }

    public Collection<DependencyNode> getAllDependencies(ArtifactFilter artifactFilter) throws MojoExecutionException {
        final ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest( session.getProjectBuildingRequest() );
        buildingRequest.setProject(project);

        final DependencyNode root;
        try {
            root = dependencyGraphBuilder.buildDependencyGraph(buildingRequest, artifactFilter, reactorProjects);
        } catch (final DependencyGraphBuilderException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        final boolean doVisitChildren = true;
        final boolean doVisitSiblings = true;
        final LinkedList<DependencyNode> dependecies = new LinkedList<>();
        final DependencyNodeVisitor visitor = new DependencyNodeVisitor() {
            @Override
            public boolean visit(final DependencyNode node) {
                log.debug("Collecting dependency [" + node + "]");
                dependecies.add(node);
                return doVisitChildren;
            }

            @Override
            public boolean endVisit(final DependencyNode node) {
                return doVisitSiblings;
            }
        };
        root.accept(visitor);

        dependecies.removeFirst(); // skip the root node it's the host project itself

        return ImmutableList.copyOf(dependecies);
    }
}
