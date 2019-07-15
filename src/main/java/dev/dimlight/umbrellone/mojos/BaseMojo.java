package dev.dimlight.umbrellone.mojos;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

import java.util.List;

/**
 * @author Marco Nicolini
 */
public abstract class BaseMojo extends AbstractMojo {

    /**
     * A prefix for the relocated packages.
     */
    @Parameter(property = "relocationPrefix", defaultValue = "relocated.", required = true)
    protected String relocationPrefix;

    /**
     * The current Maven session.
     */
    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    protected MavenSession session;

    /**
     * The current Maven project.
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    protected MavenProject project;

    /**
     * Contains the full list of projects in the reactor.
     */
    @Parameter( defaultValue = "${reactorProjects}", readonly = true, required = true )
    protected List<MavenProject> reactorProjects;

    @Component
    protected MavenProjectHelper projectHelper;

    @Component
    protected DependencyGraphBuilder dependencyGraphBuilder;
}
