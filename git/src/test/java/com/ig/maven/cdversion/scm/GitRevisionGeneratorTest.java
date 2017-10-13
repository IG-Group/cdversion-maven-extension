/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ig.maven.cdversion.scm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectChecker;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 * @author gordona
 */
@RunWith(JUnitParamsRunner.class)
public class GitRevisionGeneratorTest {

    private GitRevisionGenerator item;
    private Git git;
    private Logger logger;

    @Rule
    public ExpectedException exceptions = ExpectedException.none();
    
    @Before
    public void setup() {
        item = new GitRevisionGenerator();
        git = mock(Git.class);
        logger = mock(Logger.class);
    }

    @Test
    public void testInit_nullRepository()
            throws RevisionGeneratorException,
            IOException {

        when(git.getRepository()).thenReturn(null);

        exceptions.expect(RevisionGeneratorException.class);
        exceptions.expectMessage("This project is not a Git repository: Either remove the .mvn/extensions.xml file or put the project under Git control");

        try {
            item.init(git, logger);
        } finally {
            verify(git).getRepository();
            verifyNoMoreInteractions(git);
            verifyZeroInteractions(logger);
        }
    }

    @Test
    public void testInit_bareRepository()
            throws RevisionGeneratorException,
            IOException {

        Repository repo = mock(Repository.class);

        when(git.getRepository()).thenReturn(repo);
        when(repo.isBare()).thenReturn(Boolean.TRUE);

        exceptions.expect(RevisionGeneratorException.class);
        exceptions.expectMessage("This project is not a Git repository: Either remove the .mvn/extensions.xml file or put the project under Git control");

        try {
            item.init(git, logger);
        } finally {
            verify(git).getRepository();
            verify(repo).isBare();
            verifyNoMoreInteractions(git);
            verifyNoMoreInteractions(repo);
            verifyZeroInteractions(logger);
        }
    }

    @Test
    public void testInit_nullHeadRef()
            throws RevisionGeneratorException,
            IOException {

        Repository repo = mock(Repository.class);

        when(git.getRepository()).thenReturn(repo);
        when(repo.isBare()).thenReturn(Boolean.FALSE);
        when(repo.getRef(eq("HEAD"))).thenReturn(null);

        item.init(git, logger);

        Assert.assertEquals("GetRevision", "SNAPSHOT", item.getRevision());
        Assert.assertEquals("IsDirty", false, item.isDirty());

        verify(git).getRepository();
        verify(repo).isBare();
        verify(repo).getRef(eq("HEAD"));
        verify(logger).warn(eq("The Git repository is initialised, but no commits have been done: Setting revision to SNAPSHOT"));
        verifyNoMoreInteractions(git);
        verifyNoMoreInteractions(repo);
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void testInit_nullHeadRefObjectId()
            throws RevisionGeneratorException,
            IOException {

        Repository repo = mock(Repository.class);
        Ref headRef = mock(Ref.class);

        when(git.getRepository()).thenReturn(repo);
        when(repo.isBare()).thenReturn(Boolean.FALSE);
        when(repo.getRef(eq("HEAD"))).thenReturn(headRef);
        when(headRef.getObjectId()).thenReturn(null);

        item.init(git, logger);

        Assert.assertEquals("GetRevision", "SNAPSHOT", item.getRevision());
        Assert.assertEquals("IsDirty", false, item.isDirty());

        verify(git).getRepository();
        verify(repo).isBare();
        verify(repo).getRef(eq("HEAD"));
        verify(logger).warn(eq("The Git repository is initialised, but no commits have been done: Setting revision to SNAPSHOT"));
        verifyNoMoreInteractions(git);
        verifyNoMoreInteractions(repo);
        verifyNoMoreInteractions(logger);
    }

    @Test
    public void testInit_gitLogApiException()
            throws RevisionGeneratorException,
            GitAPIException, 
            IOException {
            
        String branch = "master";
        String hash = UUID.randomUUID().toString().replaceAll("-", "");
        String abbreviatedHash = hash.substring(0, 5);
        AbbreviatedObjectId abbreviatedObjectId = AbbreviatedObjectId.fromString(abbreviatedHash);
        
        Repository repo = mock(Repository.class);
        Ref headRef = mock(Ref.class);
        ObjectId headObjectId = mock(ObjectId.class);
        LogCommand logCmd = mock(LogCommand.class);
        
        GitAPIException exception = new NoHeadException("Dummy Git API Exception");

        when(git.getRepository()).thenReturn(repo);
        when(repo.isBare()).thenReturn(Boolean.FALSE);
        when(repo.getBranch()).thenReturn(branch);
        when(repo.getRef(eq("HEAD"))).thenReturn(headRef);
        when(headRef.getObjectId()).thenReturn(headObjectId);
        when(headObjectId.abbreviate(eq(5))).thenReturn(abbreviatedObjectId);
        when(git.log()).thenReturn(logCmd);
        when(logCmd.setMaxCount(1)).thenReturn(logCmd);
        when(logCmd.call()).thenThrow(exception);
        
        exceptions.expect(RevisionGeneratorException.class);
        exceptions.expectMessage("Issue getting Git CommitTime");
        exceptions.expectCause(IsInstanceOf.any(GitAPIException.class));

        try {
            item.init(git, logger);
        } finally {
            verify(git).getRepository();
            verify(git).log();
            verify(repo).isBare();
            verify(repo).getRef(eq("HEAD"));
            verify(repo).getBranch();
            verify(headRef, times(2)).getObjectId();
            verify(headObjectId).abbreviate(eq(5));
            verify(logCmd).setMaxCount(eq(1));
            verify(logCmd).call();
            verifyNoMoreInteractions(git);
            verifyNoMoreInteractions(repo);
            verifyNoMoreInteractions(headRef);
            verifyNoMoreInteractions(headObjectId);
            verifyNoMoreInteractions(logCmd);
            verifyZeroInteractions(logger);
        }
    }
    
    @Test
    public void testInit_gitStatusApiException()
            throws RevisionGeneratorException,
            GitAPIException, 
            IOException {

        String branch = "master";
        String hash = UUID.randomUUID().toString().replaceAll("-", "")+UUID.randomUUID().toString().replaceAll("-", "").substring(0,8);
        String abbreviatedHash = hash.substring(0, 5);
        AbbreviatedObjectId abbreviatedObjectId = AbbreviatedObjectId.fromString(abbreviatedHash);
        int commitTime = (int) (System.currentTimeMillis()/1000);
        
        RevCommit headCommit = createRevCommit(hash, commitTime);
        
        
        Repository repo = mock(Repository.class);
        Ref headRef = mock(Ref.class);
        ObjectId headObjectId = mock(ObjectId.class);
        LogCommand logCmd = mock(LogCommand.class);
        StatusCommand statusCmd = mock(StatusCommand.class);
        
        GitAPIException exception = new NoHeadException("Dummy Git API Exception");

        when(git.getRepository()).thenReturn(repo);
        when(repo.isBare()).thenReturn(Boolean.FALSE);
        when(repo.getBranch()).thenReturn(branch);
        when(repo.getRef(eq("HEAD"))).thenReturn(headRef);
        when(headRef.getObjectId()).thenReturn(headObjectId);
        when(headObjectId.abbreviate(eq(5))).thenReturn(abbreviatedObjectId);
        when(git.log()).thenReturn(logCmd);
        when(logCmd.setMaxCount(1)).thenReturn(logCmd);
        when(logCmd.call()).thenReturn(Arrays.asList(headCommit));
        when(git.status()).thenReturn(statusCmd);
        when(statusCmd.call()).thenThrow(exception);
        
        exceptions.expect(RevisionGeneratorException.class);
        exceptions.expectMessage("Issue getting Git Status");
        exceptions.expectCause(IsInstanceOf.any(GitAPIException.class));

        try {
            item.init(git, logger);
        } finally {
            verify(git).getRepository();
            verify(git).log();
            verify(repo).isBare();
            verify(repo).getRef(eq("HEAD"));
            verify(repo).getBranch();
            verify(headRef, times(2)).getObjectId();
            verify(headObjectId).abbreviate(eq(5));
            verify(logCmd).setMaxCount(eq(1));
            verify(logCmd).call();
            verify(git).status();
            verify(statusCmd).call();
            verifyNoMoreInteractions(git);
            verifyNoMoreInteractions(repo);
            verifyNoMoreInteractions(headRef);
            verifyNoMoreInteractions(headObjectId);
            verifyNoMoreInteractions(logCmd);
            verifyNoMoreInteractions(statusCmd);
            verifyZeroInteractions(logger);
        }
    }
    
    @Test
    @Parameters({
        "master, false, %s-%s",
        "master, true, %s-%s-SNAPSHOT",
        "some_branch, false, %s-%s-some-branch",
        "some_branch, true, %s-%s-some-branch-SNAPSHOT",
        "bad/*+=()branch?\\naming, false, %s-%s-bad-branch-naming",
        "bad/*+=()branch?\\naming, true, %s-%s-bad-branch-naming-SNAPSHOT"
    })
    public void testInit_cleanRepoOnMaster(
            String branch, boolean dirty, String expectedRevision)
            throws RevisionGeneratorException,
            GitAPIException,
            IOException {

        String hash = UUID.randomUUID().toString().replaceAll("-", "") + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
        String abbreviatedHash = hash.substring(0, 5);
        AbbreviatedObjectId abbreviatedObjectId = AbbreviatedObjectId.fromString(abbreviatedHash);
        int commitTime = (int) (System.currentTimeMillis() / 1000);

        RevCommit headCommit = createRevCommit(hash, commitTime);

        Repository repo = mock(Repository.class);
        Ref headRef = mock(Ref.class);
        ObjectId headObjectId = mock(ObjectId.class);
        LogCommand logCmd = mock(LogCommand.class);
        StatusCommand statusCmd = mock(StatusCommand.class);
        Status status = mock(Status.class);

        when(git.getRepository()).thenReturn(repo);
        when(repo.isBare()).thenReturn(Boolean.FALSE);
        when(repo.getRef(eq("HEAD"))).thenReturn(headRef);
        when(repo.getBranch()).thenReturn(branch);
        when(headRef.getObjectId()).thenReturn(headObjectId);
        when(headObjectId.abbreviate(eq(5))).thenReturn(abbreviatedObjectId);
        when(git.log()).thenReturn(logCmd);
        when(logCmd.setMaxCount(1)).thenReturn(logCmd);
        when(logCmd.call()).thenReturn(Arrays.asList(headCommit));
        when(git.status()).thenReturn(statusCmd);
        when(statusCmd.call()).thenReturn(status);
        when(status.isClean()).thenReturn(!dirty);

        try {
            item.init(git, logger);
            
            SimpleDateFormat commitFmt = new SimpleDateFormat("yyMMdd.HHmmss");
            Date commitDate;
            if (dirty) {
                commitDate = new Date(((long)commitTime+1)*1000);
            } else {
                commitDate = new Date((long)commitTime*1000);
            }
            Assert.assertEquals(
                    "Revision",
                    String.format(expectedRevision, commitFmt.format(commitDate), abbreviatedHash), 
                    item.getRevision());
            Assert.assertEquals("Dirty", dirty, item.isDirty());
        } finally {
            verify(git).getRepository();
            verify(git).log();
            verify(repo).isBare();
            verify(repo).getRef(eq("HEAD"));
            verify(repo).getBranch();
            verify(headRef, times(2)).getObjectId();
            verify(headObjectId).abbreviate(eq(5));
            verify(logCmd).setMaxCount(eq(1));
            verify(logCmd).call();
            verify(git).status();
            verify(statusCmd).call();
            verifyNoMoreInteractions(git);
            verifyNoMoreInteractions(repo);
            verifyNoMoreInteractions(headRef);
            verifyNoMoreInteractions(headObjectId);
            verifyNoMoreInteractions(logCmd);
            verifyNoMoreInteractions(statusCmd);
            verifyZeroInteractions(logger);
        }
    }

    @Test
    @Parameters({"true", "false"})
    public void testGetters(boolean dirty) {
        String revision = "dummy-revision";
        item = new GitRevisionGenerator(revision, dirty);

        Assert.assertEquals("GetRevision", revision, item.getRevision());
        Assert.assertEquals("IsDirty", dirty, item.isDirty());
    }
    
    @Test
    @Parameters({
        "B-12345, B-12345",
        "B_12345, B-12345",
        "bugfix/B-12345, bugfix-B-12345",
        "hotfix/-12345, hotfix-12345",
        "hotfix/B?/\\-12345, hotfix-B-12345"
    })
    public void testSafeBranchName(String branch, String expected) {
        String result = item.safeBranchName(branch);
        Assert.assertEquals(expected, result);
    }
    
    private RevCommit createRevCommit(String id, int commitTime) 
            throws UnsupportedEncodingException {
        ByteBuffer buf = ByteBuffer.allocate(67);
        buf.put((byte)0).put((byte)0).put((byte)0).put((byte)0).put((byte)0);
        buf.put(id.getBytes("ASCII"));
        buf.put((byte)0);
        buf.put(ObjectChecker.committer);
        buf.put((byte)'>');
        buf.put(String.valueOf(commitTime).getBytes("ASCII"));
        byte[] raw = buf.array();
        return RevCommit.parse(raw);
    }
}
