package com.ig.maven.cdversion;

import com.ig.maven.cdversion.scm.GitRevisionGenerator;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "GitCDVersion")
public class GitCDVersionLifecycleParticipant extends CDVersionLifecycleParticipant {

    public GitCDVersionLifecycleParticipant() {
        super(new GitRevisionGenerator());
    }
}
