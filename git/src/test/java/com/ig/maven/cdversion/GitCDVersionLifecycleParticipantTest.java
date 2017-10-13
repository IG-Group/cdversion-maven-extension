/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ig.maven.cdversion;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author gordona
 */
public class GitCDVersionLifecycleParticipantTest {
    
    @Test
    public void testConstructor() {
        
        GitCDVersionLifecycleParticipant item = new GitCDVersionLifecycleParticipant();
        Assert.assertNotNull("GitCDVersionLifecycleParticipant", item);
    }
}
