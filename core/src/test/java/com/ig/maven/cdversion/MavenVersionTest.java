package com.ig.maven.cdversion;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class MavenVersionTest {
    
    @Test
    @Parameters({
        "1.0-20160225-101534-abcdef, 1.0-20160225-101534-abcdef-SNAPSHOT", // Release after Snapshot
        "1.0-20160225-101535-abcdef-SNAPSHOT, 1.0-20160225-101534-abcdef", // Dev after Release
        "1.0-20160225-101534-abcdef, 1.0-B-12345-20160225-101534-abcdef", // Master after Branch (Same Commit)
        "1.0-20160225-101534-abcdef, 1.0-B-12345-20160225-112123-fedcba" // Master after Branch (Branch Later)
    })
    public void testMavenVersionOrdering(String version1, String version2) {
        ComparableVersion compare1 = new ComparableVersion(version1);
        ComparableVersion compare2 = new ComparableVersion(version2);
        Assert.assertEquals(1, compare1.compareTo(compare2));
    }
}
