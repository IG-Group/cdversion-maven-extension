package com.ig.maven.cdversion.maven;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;

public class PluginMerger {
    
    public void merge(
            MavenProject project, 
            Plugin mergePlugin) {
        if (mergePlugin.getArtifactId() == null || mergePlugin.getArtifactId().isEmpty()) {
            return;
        }
        
        List<Plugin> plugins = project.getBuild().getPlugins();
        Plugin foundPlugin = null;
        for (Plugin plugin : plugins) {
            if (mergePlugin.getGroupId().equals(plugin.getGroupId())
                    && mergePlugin.getArtifactId().equals(plugin.getArtifactId())) {
                foundPlugin = plugin;
                break;
            }
        }
        if (foundPlugin == null) {
            plugins.add(mergePlugin);
        } else {
            mergeExecutions(foundPlugin.getExecutions(), mergePlugin.getExecutions());
        }
    }
    
    private void mergeExecutions(
            List<PluginExecution> executions,
            List<PluginExecution> mergeExecution) {
        Map<String, PluginExecution> executionMap = convertToMap(executions);
        Map<String, PluginExecution> mergeExecutionMap = convertToMap(mergeExecution);
        for (String mergeKey : mergeExecutionMap.keySet()) {
            if (!executionMap.containsKey(mergeKey)) {
                executions.add(mergeExecutionMap.get(mergeKey));
            }
        }
    }
    
    private Map<String, PluginExecution> convertToMap(List<PluginExecution> executions) {
        Map<String, PluginExecution> result = new LinkedHashMap();
        for (PluginExecution pluginExecution : executions) {
            result.put(pluginExecution.getId(), pluginExecution);
        }
        return result;
    }
}
