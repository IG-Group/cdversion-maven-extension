package com.ig.maven.cdversion.scm;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitRevisionGenerator implements RevisionGenerator {

    private String revision;
    private boolean dirty;

    public GitRevisionGenerator() {
    }

    public GitRevisionGenerator(String revision, boolean dirty) {
        this.revision = revision;
        this.dirty = dirty;
    }

    @Override
    public void init(MavenSession session, Logger logger) throws RevisionGeneratorException {
        Git git = null;
        try {
            if (session.getExecutionRootDirectory() == null) {
                revision = "1.0-SNAPSHOT";
                dirty = true;
            } else {
                File gitDir = new FileRepositoryBuilder()
                        .findGitDir(new F‌ile(session.getExecutionRootDirectory()))
                        .getGitDir();
                if (gitDir != null && gitDir.exists()) {
                    git = Git.open(gitDir);
                    init(git, logger);
                } else {
                    revision = "1.0-SNAPSHOT";
                    dirty = true;
                }
            }
        } catch (IOException ex) {
            throw new RevisionGeneratorException("Issue opening Git repository for the project", ex);
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }

    protected void init(Git git, Logger logger) throws RevisionGeneratorException, IOException {
        Repository repo = git.getRepository();
        if (repo == null || repo.isBare()) {
            throw new RevisionGeneratorException("This project is not a Git repository: Either remove the .mvn/extensions.xml file or put the project under Git control");
        }
        Ref head = repo.getRef("HEAD");
        if (head != null && head.getObjectId() != null) {
            String branch = safeBranchName(repo.getBranch());
            String hash = head.getObjectId().abbreviate(5).name();

            long commitTime = 0;
            try {
                Iterable<RevCommit> commits = git.log().setMaxCount(1).call();
                commitTime = (long) commits.iterator().next().getCommitTime() * 1000;
            } catch (GitAPIException ex) {
                throw new RevisionGeneratorException("Issue getting Git CommitTime", ex);
            }

            try {
                Status status = git.status().call();
                dirty = !status.isClean();
            } catch (GitAPIException ex) {
                throw new RevisionGeneratorException("Issue getting Git Status", ex);
            }

            // If dirty increment by 1 second so that the snapshot is on the next time interval
            // This will make it higher in version than the previous commit which is more correct.
            if (dirty) {
                commitTime += 1000;
            }
            SimpleDateFormat commitFormatter = new SimpleDateFormat("yyMMdd.HHmmss");

            StringBuilder revisionBuilder = new StringBuilder();
            revisionBuilder.append(commitFormatter.format(new Date(commitTime)))
                    .append("-")
                    .append(hash);
            if (!"master".equals(branch)) {
                revisionBuilder.append("-").append(branch);
            }
            if (dirty) {
                revisionBuilder.append("-SNAPSHOT");
            }

            revision = revisionBuilder.toString();
        } else {
            logger.warn("The Git repository is initialised, but no commits have been done: Setting revision to SNAPSHOT");
            revision = "SNAPSHOT";
            dirty = false;
        }
    }

    protected String safeBranchName(String branch) {
        String result = branch.replaceAll("[^a-zA-Z0-9-]", "-");
        return result.replaceAll("-+", "-");
    }

    @Override
    public String getRevision() {
        return revision;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }
}
