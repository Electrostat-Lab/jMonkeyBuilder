package com.ss.editor.executor.impl;

import com.ss.editor.annotation.JMEThread;
import com.ss.editor.annotation.FromAnyThread;
import com.ss.editor.util.EditorUtil;

import org.jetbrains.annotations.NotNull;

import com.ss.rlib.logging.Logger;
import com.ss.rlib.logging.LoggerManager;
import com.ss.rlib.util.ArrayUtils;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import com.ss.rlib.util.array.ConcurrentArray;

/**
 * The executor to dispatch tasks in the editor thread.
 *
 * @author JavaSaBr
 * @author pavl_g.
 */
public class GLTaskExecutor extends AbstractTaskExecutor {

    @NotNull
    private static final Logger LOGGER = LoggerManager.getLogger(GLTaskExecutor.class);

    @NotNull
    private static final GLTaskExecutor INSTANCE = new GLTaskExecutor();

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static GLTaskExecutor getInstance() {
        return INSTANCE;
    }

    /**
     * The list of waited tasks.
     */
    @NotNull
    private final ConcurrentArray<Runnable> waitTasks;

    /**
     * The list with tasks to dispatch.
     */
    @NotNull
    private final Array<Runnable> execute;

    private GLTaskExecutor() {
        this.waitTasks = ArrayFactory.newConcurrentAtomicARSWLockArray(Runnable.class);
        this.execute = ArrayFactory.newArray(Runnable.class);
    }

    /**
     * Add a task to dispatch.
     *
     * @param task the task.
     */
    @FromAnyThread
    public void addToExecute(@NotNull final Runnable task) {
        ArrayUtils.runInWriteLock(waitTasks, task, (tasks, toAdd) -> tasks.add(task));
    }

    /**
     * Execute waited tasks.
     */
    @JMEThread
    public void dispatch() {
        if (waitTasks.isEmpty()) {
            return;
        }
        ArrayUtils.runInWriteLock(waitTasks, execute, ArrayUtils::move);
        for (final Runnable runnable: execute.array()) {
            GLTaskExecutor.dispatch(runnable);
        }
        execute.clear();

    }

    @JMEThread
    private static void dispatch(final Runnable runnable) {
        try {
            if (runnable != null) {
                runnable.run();
            }
        } catch (final Exception e) {
            EditorUtil.handleException(LOGGER, getInstance(), e);
        }
    }

    @Override
    protected void doExecute(@NotNull Array<Runnable> execute, @NotNull Array<Runnable> executed) {

    }
}
