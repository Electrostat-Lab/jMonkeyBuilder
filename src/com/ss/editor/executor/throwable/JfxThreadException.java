package com.ss.editor.executor.throwable;

/**
 *
 * @author pavl_g.
 */
public class JfxThreadException extends Exception {
    private JfxThreadException(final String message) {
        super(message);
    }
    public static JfxThreadException throwNotFoundException() {
        return new JfxThreadException("Jfx Thread instance not found !");
    }

    public static JfxThreadException createExceptionWithMessage(final String message) {
        return new JfxThreadException(message);
    }
}
