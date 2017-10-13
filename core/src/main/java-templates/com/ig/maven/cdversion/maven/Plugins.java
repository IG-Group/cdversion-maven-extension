package com.ig.maven.cdversion.maven;

import java.io.IOException;
import java.io.StringReader;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class Plugins {

    public Plugin getEnforcerPlugin(MavenProject project)
            throws MavenExecutionException {
        StringBuilder configString = new StringBuilder()
                .append("<configuration><rules>")
                .append("<requireReleaseDeps><message>No Snapshots Allowed!</message><excludes><exclude>"+project.getGroupId()+":*</exclude></excludes></requireReleaseDeps>")
                .append("</rules></configuration>");
        Xpp3Dom config = null;
        try {
            config = Xpp3DomBuilder.build(new StringReader(configString.toString()));
        } catch (XmlPullParserException | IOException ex) {
            throw new MavenExecutionException("Issue creating cofig for enforcer plugin", ex);
        }

        PluginExecution execution = new PluginExecution();
        execution.setId("no-snapshot-deps");
        execution.addGoal("enforce");
        execution.setConfiguration(config);

        Plugin result = new Plugin();
        result.setArtifactId("maven-enforcer-plugin");
        result.setVersion("1.4.1");
        result.addExecution(execution);

        return result;
    }
    
    public Plugin getVersionFixPlugin() {
        PluginExecution execution = new PluginExecution();
        execution.setId("versionfix");
        execution.addGoal("versionfix");

        Plugin result = new Plugin();
        result.setGroupId("com.iggroup.maven.cdversion");
        result.setArtifactId("versionfix-maven-plugin");
        result.setVersion("${project.version}");
        result.addExecution(execution);
        
        return result;
    }
}
