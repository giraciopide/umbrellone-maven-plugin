package dev.dimlight.umbrellone.util;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import io.vavr.Tuple2;

import java.util.LinkedList;

public class MoreStrings {

    private MoreStrings() {}

    public static String removeSuffix(String s, String suffix) {
        if (s.endsWith(suffix)) {
            return s.substring(0, s.length() - suffix.length());
        }
        return s;
    }

    public static String removePrefix(String prefix, String s) {
        if (s.startsWith(prefix)) {
            return s.substring(prefix.length());
        }
        return s;
    }

    /**
     * @return splits on the last occurrence of sep and returns the 2 pieces.
     */
    public static Tuple2<String, String> splitOnLast(String str, String sep) {
        Preconditions.checkState(!str.endsWith(sep));
        final LinkedList<String> parts = new LinkedList<>(Splitter.on(sep).splitToList(str));

        final String file = parts.removeLast();
        final String directory = Joiner.on(sep).join(parts);
        return new Tuple2<>(directory, file);
    }
}
