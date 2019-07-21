package dev.dimlight.umbrellone.mojo;

import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

/**
 * @author Marco Nicolini
 */
public class ArtifactFilters {

    private ArtifactFilters() {}

    public static final ArtifactFilter NOT_TEST_NOR_JAR = artifact -> artifact.getType().equalsIgnoreCase("jar") && !artifact.getScope().equalsIgnoreCase("test");

    public static final ArtifactFilter INCLUDE_ALL = artifact -> true;

    public static ArtifactFilter and(ArtifactFilter a, ArtifactFilter b) {
        return artifact -> a.include(artifact) && b.include(artifact);
    }

    public static ArtifactFilter or(ArtifactFilter a, ArtifactFilter b) {
        return artifact -> a.include(artifact) || b.include(artifact);
    }
}
