package dev.dimlight.umbrellone.mojo;

import java.util.Set;

/**
 * Mojo configuration object @see {@link AbstractBaseMojo}.
 *
 * @author Marco Nicolini
 */
public class ArtifactSet {

    private Set<String> excludes;

    public Set<String> getExcludes() {
        return excludes;
    }
}