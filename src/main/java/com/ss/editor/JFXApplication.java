package com.ss.editor;

import com.jme3x.jfx.injfx.processor.FrameTransferSceneProcessor;
import com.ss.editor.config.Config;
import com.ss.editor.config.EditorConfig;
import com.ss.editor.executor.impl.GLTaskExecutor;
import com.ss.editor.manager.*;
import com.ss.editor.ui.builder.EditorFXSceneBuilder;
import com.ss.editor.ui.scene.EditorFXScene;
import com.ss.editor.util.Semaphore;
import com.ss.rlib.manager.InitializeManager;
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static com.jme3x.jfx.injfx.JmeToJFXIntegrator.bind;

/**
 * Starts the editor in a new Java editor thread.
 *
 * @author JavaSaBr
 * @author pavl_g
 */
public class JFXApplication extends Application {

    private final GLTaskExecutor executor = GLTaskExecutor.getInstance();
    private final Editor editor = Editor.getInstance();
    private final EditorConfig editorConfig = EditorConfig.getInstance();

    public static final Semaphore.Mutex mutex = Semaphore.Mutex.SIMPLE_MUTEX;
    public static final Semaphore semaphore = Semaphore.build(mutex);

    private static JFXApplication instance;
    private volatile EditorFXScene scene;
    private volatile FrameTransferSceneProcessor sceneProcessor;
    private Stage stage;

    /**
     * Begins the editor Ui in a new jfx instance.
     */
    public static void beginUiTransactions() {
        mutex.setLockData(EditorStateManager.State.INITIALIZING);
        mutex.setMonitorObject(instance);
        // lock the mutex
        semaphore.lock(JFXApplication.mutex);

        InitializeManager.register(ResourceManager.class);
        InitializeManager.register(JavaFXImageManager.class);
        InitializeManager.register(FileIconManager.class);
        InitializeManager.register(WorkspaceManager.class);
        InitializeManager.register(ClasspathManager.class);

        launch();
    }

    /**
     * Callback after initializing the editor.
     *
     * @param stage the jfx stage.
     */
    @Override
    public void start(final Stage stage) throws Exception {
        JFXApplication.instance = this;
        // unlock this semaphore for other waiting threads (initialization and loading)
        JFXApplication.semaphore.unlock(JFXApplication.mutex);
        this.stage = stage;

        SvgImageLoaderFactory.install();

        final EditorConfig setupConfig = setupDefaultStageConfig(stage);
        observeWindowChanges(setupConfig);

        // start jme renderer with lwjgl on a gl thread
        // then build the scene
        ExecutorManager.dispatchGLThread(new Runnable() {
            @Override
            public void run() {
                buildScene();
                JFXApplication.semaphore.waitForUnlock();
                InitializeManager.initialize();
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        editorConfig.save();
        executor.addToExecute(editor::destroy);
    }

    private void observeWindowChanges(final EditorConfig setupConfig) {
        if (!stage.isMaximized()) {
            stage.centerOnScreen();
        }

        stage.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (stage.isMaximized()) {
                return;
            }
            setupConfig.setScreenWidth(newValue.intValue());
        });

        stage.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (stage.isMaximized()) {
                return;
            }
            setupConfig.setScreenHeight(newValue.intValue());
        });

        stage.maximizedProperty().addListener((observable, oldValue, newValue) ->
                setupConfig.setMaximized(newValue));
    }

    private EditorConfig setupDefaultStageConfig(final Stage stage) {
        stage.initStyle(StageStyle.DECORATED);
        stage.setMinHeight(600);
        stage.setMinWidth(800);
        stage.setWidth(editorConfig.getScreenWidth());
        stage.setHeight(editorConfig.getScreenHeight());
        stage.setMaximized(editorConfig.isMaximized());
        stage.setTitle(Config.TITLE);
        stage.show();
        return editorConfig;
    }

    private void buildScene() {
        this.scene = EditorFXSceneBuilder.build(stage);
        executor.addToExecute(() -> createSceneProcessor(scene, editor));
    }

    private void createSceneProcessor(final EditorFXScene scene, final Editor editor) {
        sceneProcessor = bind(editor, scene.getCanvas(), editor.getViewPort());
        sceneProcessor.setEnabled(false);
        stage.focusedProperty().addListener((observable, oldValue, newValue) ->
                editor.setPaused(editorConfig.isStopRenderOnLostFocus() && !newValue));
        Platform.runLater(scene::notifyFinishBuild);
    }

    /**
     * Get the current JavaFX scene.
     *
     * @return the JavaFX scene.
     */
    public EditorFXScene getScene() {
        return scene;
    }

    /**
     * Get the current scene processor of this application.
     *
     * @return the scene processor.
     */
    public FrameTransferSceneProcessor getSceneProcessor() {
        return sceneProcessor;
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static JFXApplication getInstance() {
        return instance;
    }
}
