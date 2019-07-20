package dev.dimlight.umbrellone.shade;

import java.util.Objects;

/**
 * Represents a relocation rule.
 *
 * @author Marco Nicolini
 */
public class RelocationRule {

    public final String pattern;

    public final String shadedPattern;

    private RelocationRule(String pattern, String shadedPattern) {
        this.pattern = pattern;
        this.shadedPattern = shadedPattern;
    }

    public static RelocationRule of(String pattern, String shadedPattern) {
        return new RelocationRule(pattern, shadedPattern);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelocationRule that = (RelocationRule) o;
        return Objects.equals(pattern, that.pattern) &&
                Objects.equals(shadedPattern, that.shadedPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern, shadedPattern);
    }

    @Override
    public String toString() {
        return "Relocation from [" + pattern + "] to [" + shadedPattern + "]";
    }
}
