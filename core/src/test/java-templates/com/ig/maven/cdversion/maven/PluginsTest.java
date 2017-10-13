package com.ig.maven.cdversion.maven;

import java.io.IOException;
import java.io.StringReader;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PluginsTest {

    private Plugins item;

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @Before
    public void setup() {
        item = new Plugins();
    }

    @Test
    public void testGetEnforcerPlugin()
            throws MavenExecutionException,
            XmlPullParserException,
            IOException {
        Plugin result = item.getEnforcerPlugin();

        Assert.assertEquals("GroupId", "org.apache.maven.plugins", result.getGroupId());
        Assert.assertEquals("ArtifactId", "maven-enforcer-plugin", result.getArtifactId());
        Assert.assertEquals("Version", "1.4.1", result.getVersion());
        Assert.assertEquals("Executions.Size", 1, result.getExecutions().size());

        PluginExecution execution = result.getExecutions().get(0);
        Assert.assertEquals("Executions[0].Id", "no-snapshot-deps", execution.getId());
        Assert.assertEquals("Executions[0].Goals.Size", 1, execution.getGoals().size());
        Assert.assertEquals("Executions[0].Goals[0]", "enforce", execution.getGoals().get(0));

        Assert.assertEquals("Executions[0].Configuration",
                Xpp3DomBuilder.build(new StringReader("<configuration><rules><requireReleaseDeps><message>No Snapshots Allowed!</message></requireReleaseDeps></rules></configuration>")),
                execution.getConfiguration());
    }

    @Test
    public void testGetVersionFixPlugin()
            throws MavenExecutionException,
            XmlPullParserException,
            IOException {
        Plugin result = item.getVersionFixPlugin();

        Assert.assertEquals("GroupId", "com.iggroup.maven.cdversion", result.getGroupId());
        Assert.assertEquals("ArtifactId", "versionfix-maven-plugin", result.getArtifactId());
        Assert.assertEquals("Version", "1.0.0-SNAPSHOT", result.getVersion());
        Assert.assertEquals("Executions.Size", 1, result.getExecutions().size());

        PluginExecution execution = result.getExecutions().get(0);
        Assert.assertEquals("Executions[0].Id", "versionfix", execution.getId());
        Assert.assertEquals("Executions[0].Goals.Size", 1, execution.getGoals().size());
        Assert.assertEquals("Executions[0].Goals[0]", "versionfix", execution.getGoals().get(0));
    }
}
