package com.ss.editor.manager;

import com.jme3x.jfx.injfx.JmeToJFXApplication;
import com.ss.editor.Editor;
import com.ss.editor.EditorThread;
import com.ss.editor.JFXApplication;
import com.ss.editor.executor.TaskExecutor;
import com.ss.editor.executor.impl.BackgroundTaskExecutor;
import com.ss.editor.executor.impl.FXTaskExecutor;
import com.ss.editor.executor.impl.GLTaskExecutor;
import com.ss.editor.executor.throwable.GLThreadException;
import com.ss.editor.executor.throwable.JfxThreadException;
import com.ss.rlib.concurrent.atomic.AtomicInteger;
import com.ss.rlib.logging.Logger;
import com.ss.rlib.logging.LoggerManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages, executes tasks and starts editor threads.
 *
 * @author JavaSaBr
 * @author pavl_g.
 */
public class ExecutorManager {

    private static final Logger LOGGER = LoggerManager.getLogger(ExecutorManager.class);
    private static final Runtime RUNTIME = Runtime.getRuntime();
    private static final int PROP_BACKGROUND_TASK_EXECUTORS = RUNTIME.availableProcessors();
    private static ExecutorManager instance;
    private final ScheduledExecutorService scheduledExecutorService;
    private final TaskExecutor[] backgroundTaskExecutors;
    private final GLTaskExecutor editorGLTaskExecutor;
    private final TaskExecutor fxTaskExecutor;
    private final AtomicInteger nextBackgroundTaskExecutor;
    public static EditorThread GL_THREAD;
    public static EditorThread JFX_THREAD;

    private ExecutorManager() {
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.backgroundTaskExecutors = new TaskExecutor[PROP_BACKGROUND_TASK_EXECUTORS];
        this.editorGLTaskExecutor = GLTaskExecutor.getInstance();
        this.fxTaskExecutor = new FXTaskExecutor();
        this.nextBackgroundTaskExecutor = new AtomicInteger(0);

        for (int i = 0, length = backgroundTaskExecutors.length; i < length; i++) {
            backgroundTaskExecutors[i] = new BackgroundTaskExecutor(i + 1);
        }

        LOGGER.info("initialized.");
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static ExecutorManager getInstance() {
        if (instance == null) instance = new ExecutorManager();
        return instance;
    }

    /**
     * Dispatches a static Gl thread for the editor.
     *
     * @return a static instance for the Gl thread.
     */
    public static EditorThread dispatchGLThread() {
        if (GL_THREAD == null) {
            synchronized (ExecutorManager.class) {
                if (GL_THREAD == null) {
                    final JmeToJFXApplication application = Editor.prepareToStart();
                    final ThreadGroup editorGroup = new ThreadGroup("Editor-Group");
                    final Runnable startWrapper = application::start;
                    GL_THREAD = new EditorThread(editorGroup, startWrapper, "GL Render");
                    GL_THREAD.start();
                }
            }
        }
        return GL_THREAD;
    }

    /**
     * Dispatches a static jfx thread for the editor.
     *
     * @return a static instance for the jfx thread.
     */
    public static EditorThread dispatchJfxThread() {
        if (JFX_THREAD == null) {
            synchronized (ExecutorManager.class) {
                if (JFX_THREAD == null) {
                    final ThreadGroup editorGroup = new ThreadGroup("JFX-Group");
                    final Runnable startWrapper = JFXApplication::beginUiTransactions;
                    JFX_THREAD = new EditorThread(editorGroup, startWrapper, "JFX");
                    JFX_THREAD.start();
                }
            }
        }
        return JFX_THREAD;
    }

    /**
     * Add a new background task.
     *
     * @param task the background task.
     */
    public void addBackgroundTask(final Runnable task) {
        if (task == null) {
            return;
        }
        final TaskExecutor[] executors = getBackgroundTaskExecutors();
        final AtomicInteger nextTaskExecutor = getNextBackgroundTaskExecutor();

        final int index = nextTaskExecutor.incrementAndGet();

        if (index < executors.length) {
            executors[index].execute(task);
        } else {
            nextTaskExecutor.set(0);
            executors[0].execute(task);
        }
    }

    /**
     * Add a new javaFX task.
     *
     * @param task the javaFX task.
     */
    public void addFXTask(final Runnable task) {
        if (task == null) {
            return;
        }
        final TaskExecutor executor = getFxTaskExecutor();
        executor.execute(task);
    }

    /**
     * Add a new editor task.
     *
     * @param task the editor task.
     */
    public void addEditorThreadTask(final Runnable task) {
        if (task == null) {
            return;
        }
        final GLTaskExecutor executor = getEditorThreadExecutor();
        executor.addToExecute(task);
    }

    /**
     * @return the list of background tasks executors.
     */
    private TaskExecutor[] getBackgroundTaskExecutors() {
        return backgroundTaskExecutors;
    }

    /**
     * @return the executor of javaFX tasks.
     */
    private TaskExecutor getFxTaskExecutor() {
        return fxTaskExecutor;
    }

    /**
     * @return the index of a next background executor.
     */
    private AtomicInteger getNextBackgroundTaskExecutor() {
        return nextBackgroundTaskExecutor;
    }

    /**
     * @return the executor of editor tasks.
     */
    private GLTaskExecutor getEditorThreadExecutor() {
        return editorGLTaskExecutor;
    }

    /**
     * Add a scheduled task.
     *
     * @param runnable the scheduled task.
     * @param timeout  the timeout.
     */
    public void schedule(final Runnable runnable, final long timeout) {
        if (runnable == null) {
            return;
        }
        scheduledExecutorService.schedule(runnable, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the gl thread static instance.
     *
     * @return the gl thread.
     * @throws GLThreadException if the thread is not found.
     */
    public static EditorThread getGLThread() throws GLThreadException {
        if (GL_THREAD == null) {
            throw GLThreadException.throwNotFoundException();
        }
        return GL_THREAD;
    }

    /**
     * Gets the jfx thread static instance.
     *
     * @return the jfx thread.
     * @throws JfxThreadException if the thread is not found.
     */
    public static EditorThread getJfxThread() throws JfxThreadException {
        if (JFX_THREAD == null) {
            throw JfxThreadException.throwNotFoundException();
        }
        return JFX_THREAD;
    }
}
