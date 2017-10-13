package com.ig.maven.cdversion;

public class NoSnapshotVersionException extends Exception {

    /**
     * Creates a new instance of <code>NoSnapshotVersionException</code> without
     * detail message.
     */
    public NoSnapshotVersionException() {
    }

    /**
     * Constructs an instance of <code>NoSnapshotVersionException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public NoSnapshotVersionException(String msg) {
        super(msg);
    }
}
