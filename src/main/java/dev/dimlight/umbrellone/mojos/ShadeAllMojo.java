package dev.dimlight.umbrellone.mojos;

import dev.dimlight.umbrellone.shade.ShadePluginConfiguration;
import dev.dimlight.umbrellone.shade.ShadePluginConfigurationGenerator;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.*;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Goal that inspects all dependencies and produces a configuration for the maven-shade-plugin relocation rules.
 *
 * @author Marco Nicolini
 */
@Mojo(name = "shade-all", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ShadeAllMojo extends BaseMojo {

    /**
     * A prefix for the relocated packages.
     */
    @Parameter(property = "shadePluginVersion", defaultValue = "3.2.1", required = true)
    protected String shadePluginVersion;

    @Component
    private BuildPluginManager pluginManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Log log = getLog();

        final ShadePluginConfigurationGenerator confGen = new ShadePluginConfigurationGenerator(log, session, project,
                dependencyGraphBuilder, io.vavr.collection.List.ofAll(reactorProjects));

        final ShadePluginConfiguration conf = confGen.generateShadePluginConfiguration(relocationPrefix);

        log.info("Delegating execution to the shade plugin mojo...");
        executeMojo(
                plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-shade-plugin"),
                        version(shadePluginVersion)
                ),
                goal("shade"),
                configuration(
                        element("artifactSet",
                                element("includes", conf.getIncludedArtifacts()
                                        .map(a -> element("include", a.getGroupId() + ":" + a.getArtifactId()))
                                    .toJavaArray(Element[]::new))),
                        element("relocations", conf.getRelocationRules()
                                .map(rr -> element("relocation",
                                                element("pattern", rr.pattern),
                                                element("shadedPattern", rr.shadedPattern)))
                                            .toJavaArray(Element[]::new))
                ),
                executionEnvironment(
                        project,
                        session,
                        pluginManager
                )
        );
        log.info("Delegated execution to the shade plugin mojo ended");
    }
}
