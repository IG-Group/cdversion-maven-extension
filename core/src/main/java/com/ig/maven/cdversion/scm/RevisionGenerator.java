/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ig.maven.cdversion.scm;

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.logging.Logger;

/**
 *
 * @author gordona
 */
public interface RevisionGenerator {
    
    void init(MavenSession session, Logger logger) throws RevisionGeneratorException;
    
    /**
     * Generates a suitable revision string which is compatible with Maven
     * version's so that newer commits are seen as newer versions in Maven.
     * 
     * @return The revision string to make up the end part of the projects version
     */
    String getRevision();
    
    /**
     * Says whether the project has any local modifications as defined by the
     * SCM system
     * 
     * @return Whether the project has local modifications according to the SCM
     */
    boolean isDirty();
}
