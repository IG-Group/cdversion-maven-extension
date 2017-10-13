package com.ig.maven.cdversion.maven;

import com.shazam.shazamcrest.matcher.Matchers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PluginMergerTest {

    private PluginMerger item;
    private MavenProject project;
    private List<Plugin> dummyPlugins;

    @Before
    public void setup() {
        item = new PluginMerger();
        project = new MavenProject();

        Plugin dummyPlugin1 = new Plugin();
        dummyPlugin1.setArtifactId("dummy-artifact");
        Plugin dummyPlugin2 = new Plugin();
        dummyPlugin2.setGroupId("dummy.group");
        dummyPlugin2.setArtifactId("dummy-artifact");
        dummyPlugins = new ArrayList();
        dummyPlugins.add(dummyPlugin1);
        dummyPlugins.add(dummyPlugin2);
    }

    @Test
    public void testMerge_nullArtifactId() {
        Plugin mergePlugin = new Plugin();

        item.merge(project, mergePlugin);
        
        Assert.assertTrue("BuildPlugins.IsEmpty", project.getBuildPlugins().isEmpty());
    }

    @Test
    public void testMerge_emptyArtifactId() {
        Plugin mergePlugin = new Plugin();
        mergePlugin.setArtifactId("");

        item.merge(project, mergePlugin);
        
        Assert.assertTrue("BuildPlugins.IsEmpty", project.getBuildPlugins().isEmpty());
    }

    @Test
    public void testMerge_noPluginsPresent() {
        Plugin mergePlugin = new Plugin();
        mergePlugin.setArtifactId("merge-artifact");

        item.merge(project, mergePlugin);

        Assert.assertEquals("Plugins.Size", 1, project.getBuildPlugins().size());
        Assert.assertThat("Plugins[0]", project.getBuildPlugins().get(0), Matchers.sameBeanAs(mergePlugin));
    }

    @Test
    public void testMerge_pluginNotFound() {
        project.getBuild().getPlugins().addAll(dummyPlugins);

        Plugin mergePlugin = new Plugin();
        mergePlugin.setArtifactId("merge-artifact");

        item.merge(project, mergePlugin);

        Assert.assertEquals("Plugins.Size", dummyPlugins.size() + 1, project.getBuildPlugins().size());
        for (int i = 0; i < dummyPlugins.size(); i++) {
            Assert.assertThat("Plugins["+i+"]", project.getBuildPlugins().get(i), Matchers.sameBeanAs(dummyPlugins.get(i)));
        }
        Assert.assertThat("Plugins["+dummyPlugins.size()+"]", project.getBuildPlugins().get(dummyPlugins.size()), Matchers.sameBeanAs(mergePlugin));
    }
    
    @Test
    public void testMerge_pluginFoundWithNoExecutions() {
        Plugin buildPlugin = new Plugin();
        buildPlugin.setArtifactId("merge-artifact");
        List<Plugin> plugins = project.getBuild().getPlugins();
        plugins.addAll(dummyPlugins);
        plugins.add(buildPlugin);

        PluginExecution exec = new PluginExecution();
        exec.setId("merge-execution-id");
        exec.setGoals(Arrays.asList("some-goal"));
        exec.setPhase("random-phase");
        exec.setPriority(1);
        
        Plugin mergePlugin = new Plugin();
        mergePlugin.setArtifactId("merge-artifact");
        mergePlugin.getExecutions().add(exec);

        item.merge(project, mergePlugin);

        Assert.assertEquals("Plugins.Size", dummyPlugins.size() + 1, project.getBuildPlugins().size());
        for (int i = 0; i < dummyPlugins.size(); i++) {
            Assert.assertThat("Plugins["+i+"]", project.getBuildPlugins().get(i), Matchers.sameBeanAs(dummyPlugins.get(i)));
        }
        Assert.assertThat("Plugins["+dummyPlugins.size()+"]", project.getBuildPlugins().get(dummyPlugins.size()), Matchers.sameBeanAs(mergePlugin));
    }
    
    @Test
    public void testMerge_pluginFoundWithExecutions() {
        PluginExecution buildExec = new PluginExecution();
        buildExec.setId("random-execution-id");
        buildExec.setGoals(Arrays.asList("some-goal"));
        buildExec.setPhase("random-phase");
        buildExec.setPriority(1);
        
        Plugin buildPlugin = new Plugin();
        buildPlugin.setArtifactId("merge-artifact");
        buildPlugin.addExecution(buildExec);
        
        List<Plugin> plugins = project.getBuild().getPlugins();
        plugins.addAll(dummyPlugins);
        plugins.add(buildPlugin);

        PluginExecution mergeExec = new PluginExecution();
        mergeExec.setId("merge-execution-id");
        mergeExec.setGoals(Arrays.asList("some-goal"));
        mergeExec.setPhase("random-phase");
        mergeExec.setPriority(1);
        
        Plugin mergePlugin = new Plugin();
        mergePlugin.setArtifactId("merge-artifact");
        mergePlugin.getExecutions().add(mergeExec);

        item.merge(project, mergePlugin);
        
        Plugin expectedPlugin = new Plugin();
        expectedPlugin.setArtifactId("merge-artifact");
        expectedPlugin.getExecutions().add(buildExec);
        expectedPlugin.getExecutions().add(mergeExec);

        Assert.assertEquals("Plugins.Size", dummyPlugins.size() + 1, project.getBuildPlugins().size());
        for (int i = 0; i < dummyPlugins.size(); i++) {
            Assert.assertThat("Plugins["+i+"]", project.getBuildPlugins().get(i), Matchers.sameBeanAs(dummyPlugins.get(i)));
        }
        Assert.assertThat("Plugins["+dummyPlugins.size()+"]", project.getBuildPlugins().get(dummyPlugins.size()), Matchers.sameBeanAs(expectedPlugin));
    }
    
    @Test
    public void testMerge_pluginFoundWithConflictingExecutions() {
        PluginExecution buildExec = new PluginExecution();
        buildExec.setId("merge-execution-id");
        buildExec.setGoals(Arrays.asList("original-goal"));
        buildExec.setPhase("original-phase");
        buildExec.setPriority(1);
        
        Plugin buildPlugin = new Plugin();
        buildPlugin.setArtifactId("merge-artifact");
        buildPlugin.addExecution(buildExec);
        
        List<Plugin> plugins = project.getBuild().getPlugins();
        plugins.addAll(dummyPlugins);
        plugins.add(buildPlugin);

        PluginExecution mergeExec = new PluginExecution();
        mergeExec.setId("merge-execution-id");
        mergeExec.setGoals(Arrays.asList("merge-goal"));
        mergeExec.setPhase("merge-phase");
        mergeExec.setPriority(2);
        
        Plugin mergePlugin = new Plugin();
        mergePlugin.setArtifactId("merge-artifact");
        mergePlugin.getExecutions().add(mergeExec);

        item.merge(project, mergePlugin);
        
        Plugin expectedPlugin = new Plugin();
        expectedPlugin.setArtifactId("merge-artifact");
        expectedPlugin.getExecutions().add(buildExec);

        Assert.assertEquals("Plugins.Size", dummyPlugins.size() + 1, project.getBuildPlugins().size());
        for (int i = 0; i < dummyPlugins.size(); i++) {
            Assert.assertThat("Plugins["+i+"]", project.getBuildPlugins().get(i), Matchers.sameBeanAs(dummyPlugins.get(i)));
        }
        Assert.assertThat("Plugins["+dummyPlugins.size()+"]", project.getBuildPlugins().get(dummyPlugins.size()), Matchers.sameBeanAs(expectedPlugin));
    }
}
