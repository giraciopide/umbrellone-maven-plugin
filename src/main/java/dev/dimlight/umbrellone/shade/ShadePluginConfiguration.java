package dev.dimlight.umbrellone.shade;

import io.vavr.collection.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.graph.DependencyNode;

/**
 * @author Marco Nicolini
 */
public class ShadePluginConfiguration {

    private final List<Artifact> includedArtifacts;
    private final List<RelocationRule> relocationRules;

    public ShadePluginConfiguration(List<Artifact> includedArtifacts, List<RelocationRule> relocationRules) {
        this.includedArtifacts = includedArtifacts;
        this.relocationRules = relocationRules;
    }

    public static ShadePluginConfiguration of(List<Artifact> includedArtifacts, List<RelocationRule> relocationRules) {
        return new ShadePluginConfiguration(includedArtifacts, relocationRules);
    }

    public static ShadePluginConfiguration ofDependencies(List<DependencyNode> dependencyNodes, List<RelocationRule> relocationRules) {
        return new ShadePluginConfiguration(dependencyNodes.map(DependencyNode::getArtifact), relocationRules);
    }

    public List<Artifact> getIncludedArtifacts() {
        return includedArtifacts;
    }

    public List<RelocationRule> getRelocationRules() {
        return relocationRules;
    }
}
