package me.raska.opendvs.worker.poller.exception;

public class UnsatisfiedDependencyException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -6463355489237204874L;

    public UnsatisfiedDependencyException(String msg) {
        super(msg);
    }
}
