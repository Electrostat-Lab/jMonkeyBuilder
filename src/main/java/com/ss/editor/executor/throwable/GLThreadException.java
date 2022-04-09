package com.ss.editor.executor.throwable;

/**
 *
 * @author pavl_g.
 */
public class GLThreadException extends Exception {

    private GLThreadException(String message) {
        super(message);
    }

    public static GLThreadException throwNotFoundException() {
        return new GLThreadException("GL Thread instance not found !");
    }

    public static GLThreadException createExceptionWithMessage(final String message) {
        return new GLThreadException(message);
    }
}
