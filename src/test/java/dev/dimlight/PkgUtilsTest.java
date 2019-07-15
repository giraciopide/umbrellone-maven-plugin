package dev.dimlight;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.dimlight.umbrellone.shade.PackageUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.SilentLog;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PkgUtilsTest {

    private final Log log = new SilentLog();

    @Test
    public void rootPackagesShouldWorkEvenIfTheRootPackageIsGiven() {
        final List<String> pkgs = ImmutableList.of(
                "a.k",
                "a.k.a",
                "",
                "a.b.d",
                "a.b.c",
                "b.c.d");

        Assert.assertEquals(ImmutableSet.of("", "a", "b.c.d"), ImmutableSet.copyOf(PackageUtils.of(log).rootPackages(pkgs)));
    }

    @Test
    public void rootPackagesShouldWork() {
        final List<String> pkgs2 = ImmutableList.of(
                "a.k",
                "a.k.a",
                "a.b.d",
                "a.b.c",
                "b.c.d");

        Assert.assertEquals(ImmutableSet.of("a", "b.c.d"), ImmutableSet.copyOf(PackageUtils.of(log).rootPackages(pkgs2)));
    }
}