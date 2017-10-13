package com.ig.maven.cdversion;

import com.ig.maven.cdversion.maven.PluginMerger;
import com.ig.maven.cdversion.maven.Plugins;
import com.ig.maven.cdversion.scm.RevisionGenerator;
import com.ig.maven.cdversion.scm.RevisionGeneratorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;

@RunWith(JUnitParamsRunner.class)
public class CDVersionLifecycleParticipantTest {

    private RevisionGenerator revisionGenerator;
    private PluginMerger pluginMerger;
    private Plugins plugins;
    private MavenSession session;
    private CDVersionLifecycleParticipant item;

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    public class DummyCDVersionLifecycleParticipant extends CDVersionLifecycleParticipant {

        public DummyCDVersionLifecycleParticipant(RevisionGenerator rg, PluginMerger pm, Plugins p) {
            super(rg, pm, p);
        }
    }

    @Before
    public void setup() {
        revisionGenerator = mock(RevisionGenerator.class);
        pluginMerger = mock(PluginMerger.class);
        plugins = mock(Plugins.class);
        session = mock(MavenSession.class);
        item = new DummyCDVersionLifecycleParticipant(revisionGenerator, pluginMerger, plugins);
    }

    @Test
    public void testAfterSessionStart_nullRevision()
            throws MavenExecutionException,
            RevisionGeneratorException {
        String revision = null;
        exceptions.expect(MavenExecutionException.class);
        exceptions.expectMessage("RevisionGenerator returned a null revision value");
        exceptions.expectCause(IsInstanceOf.any(RevisionGeneratorException.class));
        when(revisionGenerator.getRevision()).thenReturn(revision);

        try {
            item.afterSessionStart(session);
        } finally {
            verify(revisionGenerator).init(eq(session), any(Logger.class));
            verify(revisionGenerator).getRevision();
            verifyNoMoreInteractions(revisionGenerator);
            verifyZeroInteractions(session);
            verifyZeroInteractions(pluginMerger);
            verifyZeroInteractions(plugins);
        }
    }

    @Test
    @Parameters({
        "false, 20160101-133423-abcdefg, false",
        "false, 20160101-133423-abcdefg, true",
        "true, 20160101-133423-abcdefg-SNPASHOT, false",
        "true, 20160101-133423-abcdefg-SNAPSHOT, true",})
    public void testAfterSessionStart_withRevision(boolean dirty, String revision, boolean failSnapshot)
            throws MavenExecutionException,
            RevisionGeneratorException {
        Properties userProperties = new Properties();
        userProperties.put("cdversion.snapshot.fail", String.valueOf(failSnapshot));

        if (dirty && failSnapshot) {
            exceptions.expect(MavenExecutionException.class);
        }

        when(session.getUserProperties()).thenReturn(userProperties);
        when(revisionGenerator.getRevision()).thenReturn(revision);
        when(revisionGenerator.isDirty()).thenReturn(dirty);

        try {
            item.afterSessionStart(session);

            Assert.assertEquals("UserProperties.Size", 2, userProperties.size());
            Assert.assertNotNull("UserProperties[revision]", userProperties.get("revision"));
            Assert.assertEquals("UserProperties[revision]", revision, userProperties.get("revision"));
        } finally {
            verify(session).getUserProperties();
            verify(revisionGenerator).init(eq(session), any(Logger.class));
            verify(revisionGenerator).isDirty();
            verify(revisionGenerator).getRevision();
            verifyNoMoreInteractions(session);
            verifyNoMoreInteractions(revisionGenerator);
            verifyZeroInteractions(pluginMerger);
            verifyZeroInteractions(plugins);
        }
    }

    @Test
    public void afterSessionStart_initThrowsRevisionGeneratorException()
            throws MavenExecutionException,
            RevisionGeneratorException {

        RevisionGeneratorException exception = new RevisionGeneratorException("msg", new RuntimeException("dummy sub cause"));

        exceptions.expect(MavenExecutionException.class);
        exceptions.expectMessage(exception.getMessage());
        exceptions.expectCause(IsEqual.equalTo(exception));
        doThrow(exception).when(revisionGenerator).init(eq(session), any(Logger.class));

        try {
            item.afterSessionStart(session);
        } finally {
            verify(revisionGenerator).init(eq(session), any(Logger.class));
        }
    }

    @Test
    public void afterSessionStart_initThrowsRuntimeException()
            throws MavenExecutionException,
            RevisionGeneratorException {

        RuntimeException exception = new RuntimeException("random exception");

        exceptions.expect(MavenExecutionException.class);
        exceptions.expectMessage("Unexpected Exception during RevisionGenerator Initialisation");
        exceptions.expectCause(IsEqual.equalTo(exception));
        doThrow(exception).when(revisionGenerator).init(eq(session), any(Logger.class));

        try {
            item.afterSessionStart(session);
        } finally {
            verify(revisionGenerator).init(eq(session), any(Logger.class));
        }
    }

    @Test
    public void testAfterProjectsRead_EmptyProjects()
            throws MavenExecutionException {
        when(session.getProjects()).thenReturn(new ArrayList());

        item.afterProjectsRead(session);

        verify(session).getProjects();
        verifyZeroInteractions(revisionGenerator);
        verifyZeroInteractions(pluginMerger);
        verifyZeroInteractions(plugins);
    }

    @Test
    @Parameters({
        "true",
        "false"
    })
    public void testAfterProjectsRead(boolean dirty)
            throws MavenExecutionException {

        List<MavenProject> projects = Arrays.asList(
                mock(MavenProject.class),
                mock(MavenProject.class),
                mock(MavenProject.class));
        Plugin enforcer = mock(Plugin.class);
//        Plugin versionFix = mock(Plugin.class);

        when(session.getProjects()).thenReturn(projects);
        when(revisionGenerator.isDirty()).thenReturn(dirty);
        if (!dirty) {
            for (MavenProject project : projects) {
                when(plugins.getEnforcerPlugin(eq(project))).thenReturn(enforcer);
            }
        }
//        when(plugins.getVersionFixPlugin()).thenReturn(versionFix);

        item.afterProjectsRead(session);

        verify(session).getProjects();
        verify(revisionGenerator, times(3)).isDirty();
        if (!dirty) {
            for (MavenProject project : projects) {
                verify(plugins).getEnforcerPlugin(eq(project));
            }
        }
//        verify(plugins, times(3)).getVersionFixPlugin();
        for (int i = 0; i < 3; i++) {
            if (!dirty) {
                verify(pluginMerger).merge(eq(projects.get(i)), eq(enforcer));
            }
//            verify(pluginMerger).merge(eq(projects.get(i)), eq(versionFix));
        }
        verifyNoMoreInteractions(session);
        verifyNoMoreInteractions(revisionGenerator);
        verifyNoMoreInteractions(pluginMerger);
        verifyNoMoreInteractions(plugins);
    }

//    @Test
//    public void testAfterProjectsRead_randomException()
//            throws MavenExecutionException {
//
//        RuntimeException exception = new RuntimeException("random exception");
//        exceptions.expect(MavenExecutionException.class);
//        exceptions.expectMessage("Unexpected Exception during Plugin Merging");
//        exceptions.expectCause(IsEqual.equalTo(exception));
//
//        MavenProject project = mock(MavenProject.class);
////        Plugin versionFixPlugin = mock(Plugin.class);
//        when(session.getProjects()).thenReturn(Arrays.asList(project));
//        when(revisionGenerator.isDirty()).thenReturn(true);
////        when(plugins.getVersionFixPlugin()).thenReturn(versionFixPlugin);
////        doThrow(exception).when(pluginMerger).merge(eq(project), eq(versionFixPlugin));
//
//        try {
//            item.afterProjectsRead(session);
//        } finally {
//            verify(session).getProjects();
//            verify(revisionGenerator).isDirty();
////            verify(plugins).getVersionFixPlugin();
////            verify(pluginMerger).merge(eq(project), eq(versionFixPlugin));
//            verifyNoMoreInteractions(session);
//            verifyNoMoreInteractions(revisionGenerator);
//            verifyNoMoreInteractions(pluginMerger);
//            verifyNoMoreInteractions(plugins);
//        }
//    }
}
