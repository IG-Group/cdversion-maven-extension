package com.ig.maven.cdversion;

import com.ig.maven.cdversion.maven.Plugins;
import com.ig.maven.cdversion.maven.PluginMerger;
import com.ig.maven.cdversion.scm.RevisionGenerator;
import com.ig.maven.cdversion.scm.RevisionGeneratorException;
import java.util.Properties;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

public abstract class CDVersionLifecycleParticipant extends AbstractMavenLifecycleParticipant {

    private static final String VERSION_PROPERTY = "revision";
    private static final String FAIL_SNAPSHOT_VERSION_PROPERTY = "cdversion.snapshot.fail";
    @Requirement
    private Logger logger;
    private RevisionGenerator revisionGenerator;
    private PluginMerger pluginMerger;
    private Plugins plugins;

    public CDVersionLifecycleParticipant(RevisionGenerator revisionGenerator) {
        this(revisionGenerator, new PluginMerger(), new Plugins());
    }

    public CDVersionLifecycleParticipant(
            RevisionGenerator revisionGenerator,
            PluginMerger pluginMerger,
            Plugins plugins) {
        this.revisionGenerator = revisionGenerator;
        this.pluginMerger = pluginMerger;
        this.plugins = plugins;
    }

    @Override
    public void afterSessionStart(MavenSession session) throws MavenExecutionException {

        try {
            revisionGenerator.init(session, logger);
            String revision = revisionGenerator.getRevision();
            if (revision == null) {
                throw new RevisionGeneratorException("RevisionGenerator returned a null revision value");
            }
            Properties userProps = session.getUserProperties();
            if (revisionGenerator.isDirty() && Boolean.valueOf((String) userProps.get(FAIL_SNAPSHOT_VERSION_PROPERTY))) {
                throw new NoSnapshotVersionException("Failed Build: The generated version[" + revision + "] is a SNAPSHOT and the " + FAIL_SNAPSHOT_VERSION_PROPERTY + " property is set to true");
            }
            userProps.setProperty(VERSION_PROPERTY, revision);
        } catch (RevisionGeneratorException ex) {
            throw new MavenExecutionException(ex.getMessage(), ex);
        } catch (NoSnapshotVersionException ex) {
            throw new MavenExecutionException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new MavenExecutionException("Unexpected Exception during RevisionGenerator Initialisation", ex);
        }

    }

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        try {
            for (MavenProject project : session.getProjects()) {
                if (!revisionGenerator.isDirty()) {
                    pluginMerger.merge(project, plugins.getEnforcerPlugin(project));
                }
//                pluginMerger.merge(project, plugins.getVersionFixPlugin());
            }
        } catch (Exception ex) {
            throw new MavenExecutionException("Unexpected Exception during Plugin Merging", ex);
        }
    }
}
