package dev.dimlight.umbrellone.mojo;

import dev.dimlight.umbrellone.shade.ShadePluginConfiguration;
import dev.dimlight.umbrellone.shade.ShadePluginConfigurationGenerator;
import dev.dimlight.umbrellone.shade.Xml;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal that inspects all dependencies and produces a configuration for the maven-shade-plugin relocation rules.
 *
 * @author Marco Nicolini
 */
@Mojo( name = "generate-conf", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class GenerateShadePluginConfigurationMojo extends AbstractBaseMojo {

    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        final Log log = getLog();

        final ShadePluginConfigurationGenerator confGen = new ShadePluginConfigurationGenerator(log, session, project,
                dependencyGraphBuilder, io.vavr.collection.List.ofAll(reactorProjects));

        final ShadePluginConfiguration conf = confGen.generateShadePluginConfiguration(getArtifactFilter(), relocationPrefix);

        log.info("generated relocate configuration: \n\n" + Xml.render(conf));
    }
}
