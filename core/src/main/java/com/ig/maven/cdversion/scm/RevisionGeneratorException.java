/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ig.maven.cdversion.scm;

/**
 *
 * @author gordona
 */
public class RevisionGeneratorException extends Exception {

    /**
     * Constructs an instance of <code>RevisionGeneratorException</code> with
     * the message.
     *
     * @param msg the error message.
     */
    public RevisionGeneratorException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>RevisionGeneratorException</code> with
     * the message and underlying cause.
     *
     * @param msg the error message.
     * @param t the underlying cause.
     */
    public RevisionGeneratorException(String msg, Throwable t) {
        super(msg, t);
    }
}
