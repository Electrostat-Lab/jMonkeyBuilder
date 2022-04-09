package com.ss.editor;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Environment;
import com.jme3.bounding.BoundingSphere;
import com.jme3.environment.EnvironmentCamera;
import com.jme3.environment.LightProbeFactory;
import com.jme3.environment.generation.JobProgressAdapter;
import com.jme3.light.LightProbe;
import com.jme3.material.Material;
import com.jme3.material.TechniqueDef;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.post.filters.ToneMapFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.system.NativeLibraryLoader;
import com.jme3x.jfx.injfx.JmeToJFXApplication;
import com.jme3x.jfx.util.os.OperatingSystem;
import com.ss.editor.config.Config;
import com.ss.editor.config.EditorConfig;
import com.ss.editor.executor.impl.GLTaskExecutor;
import com.ss.editor.extension.loader.SceneLoader;
import com.ss.editor.manager.ExecutorManager;
import com.ss.editor.manager.WorkspaceManager;
import com.ss.editor.ui.event.FXEventManager;
import com.ss.editor.ui.event.impl.WindowChangeFocusEvent;
import com.ss.editor.manager.EditorStateManager;
import com.ss.rlib.logging.Logger;
import com.ss.rlib.logging.LoggerLevel;
import com.ss.rlib.logging.LoggerManager;
import com.ss.rlib.logging.impl.FolderFileListener;
import com.ss.rlib.manager.InitializeManager;
import jme3_ext_xbuf.XbufLoader;
import tonegod.emitter.filter.TonegodTranslucentBucketFilter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import static com.jme3.environment.LightProbeFactory.makeProbe;
import static com.ss.rlib.util.ObjectUtils.notNull;
import static com.ss.rlib.util.Utils.run;
import static java.nio.file.Files.createDirectories;

/**
 * The implementation of the {@link com.jme3.app.Application} of this Editor.
 *
 * @author JavaSaBr
 * @author pavl_g.
 */
public class Editor extends JmeToJFXApplication {

    private final Node previewNode;
    private ViewPort previewViewPort;
    private Camera previewCamera;
    private EnvironmentCamera environmentCamera;
    private EnvironmentCamera previewEnvironmentCamera;
    private LightProbe lightProbe;
    private LightProbe previewLightProbe;
    private FilterPostProcessor postProcessor;
    private FXAAFilter fxaaFilter;
    private ToneMapFilter toneMapFilter;
    private TonegodTranslucentBucketFilter translucentBucketFilter;
    private Material defaultMaterial;
    private static final Editor EDITOR = new Editor();

    private static final Logger LOGGER = LoggerManager.getLogger(Editor.class);

    /**
     * The empty job adapter for handling creating {@link LightProbe}.
     */
    private static final JobProgressAdapter<LightProbe> EMPTY_JOB_ADAPTER = new JobProgressAdapter<LightProbe>() {
        public void done(final LightProbe result) {
        }
    };

    private Editor() {
        this.previewNode = new Node("Preview Node");
    }



    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static Editor getInstance() {
        return EDITOR;
    }

    /**
     * Prepare to start editor.
     *
     * @return the editor
     */
    public static Editor prepareToStart() {

        if (Config.DEV_DEBUG) {
            System.err.println("config is loaded.");
        }

        configureLogger();
        try {

            final EditorConfig config = EditorConfig.getInstance();
            final AppSettings settings = config.getSettings();

            EDITOR.setSettings(settings);
            EDITOR.setShowSettings(false);
            EDITOR.setDisplayStatView(false);
            EDITOR.setDisplayFps(false);

        } catch (final Exception e) {
            LOGGER.warning(e);
            throw new RuntimeException(e);
        }

        return EDITOR;
    }

    private static void configureLogger() {

        // disable the standard logger
        if (!Config.DEV_DEBUG) {
            java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);
        }

        // configure our logger
        LoggerLevel.DEBUG.setEnabled(Config.DEV_DEBUG);
        LoggerLevel.INFO.setEnabled(true);
        LoggerLevel.ERROR.setEnabled(true);
        LoggerLevel.WARNING.setEnabled(true);

        final Path logFolder = Config.getFolderForLog();

        if (!Files.exists(logFolder)) {
            run(() -> createDirectories(logFolder));
        }

        LoggerManager.addListener(new FolderFileListener(logFolder));
    }

    @Override
    public void destroy() {
        super.destroy();

        final WorkspaceManager workspaceManager = WorkspaceManager.getInstance();
        workspaceManager.save();

        System.exit(0);
    }

    @Override
    public Camera getCamera() {
        return super.getCamera();
    }

    @Override
    public void start() {
        NativeLibraryLoader.loadNativeLibrary("jinput", true);
        NativeLibraryLoader.loadNativeLibrary("jinput-dx8", true);

        super.start();
    }

    @Override
    public void simpleInitApp() {
        InitializeManager.initialize();

        renderManager.setPreferredLightMode(TechniqueDef.LightMode.SinglePass);
        renderManager.setSinglePassLightBatchSize(5);

        SceneLoader.install(this);

        assetManager.registerLoader(XbufLoader.class, FileExtensions.MODEL_XBUF);

        final EditorConfig editorConfig = EditorConfig.getInstance();
        final OperatingSystem system = new OperatingSystem();

        LOGGER.info(this, "OS: " + system.getDistribution());

        final AssetManager assetManager = getAssetManager();
        assetManager.registerLocator("", FolderAssetLocator.class);
        assetManager.addAssetEventListener(EditorConfig.getInstance());

        final AudioRenderer audioRenderer = getAudioRenderer();
        audioRenderer.setEnvironment(new Environment(Environment.Garage));

        viewPort.setBackgroundColor(new ColorRGBA(50 / 255F, 50 / 255F, 50 / 255F, 1F));
        cam.setFrustumPerspective(55, (float) cam.getWidth() / cam.getHeight(), 1f, 10000);

        defaultMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        // create preview view port
        previewCamera = cam.clone();
        previewViewPort = renderManager.createPostView("Preview viewport", previewCamera);
        previewViewPort.setClearFlags(true, true, true);
        previewViewPort.attachScene(previewNode);
        previewViewPort.setBackgroundColor(viewPort.getBackgroundColor());

        final Node guiNode = getGuiNode();
        guiNode.detachAllChildren();

        ExecutorManager.getInstance();

        flyCam.setDragToRotate(true);
        flyCam.setEnabled(false);

        postProcessor = new FilterPostProcessor(assetManager);
        postProcessor.initialize(renderManager, viewPort);

        fxaaFilter = new FXAAFilter();
        fxaaFilter.setEnabled(false);
        fxaaFilter.setSubPixelShift(1.0f / 4.0f);
        fxaaFilter.setVxOffset(0.0f);
        fxaaFilter.setSpanMax(8.0f);
        fxaaFilter.setReduceMul(1.0f / 8.0f);

        toneMapFilter = new ToneMapFilter();
        toneMapFilter.setEnabled(false);
        toneMapFilter.setWhitePoint(editorConfig.getToneMapFilterWhitePoint());
        toneMapFilter.setEnabled(editorConfig.isToneMapFilter());

        translucentBucketFilter = new TonegodTranslucentBucketFilter(true);
        translucentBucketFilter.setEnabled(false);

        postProcessor.addFilter(fxaaFilter);
        postProcessor.addFilter(toneMapFilter);
        postProcessor.addFilter(translucentBucketFilter);

        viewPort.addProcessor(postProcessor);

        if (Config.ENABLE_PBR) {
            environmentCamera = new EnvironmentCamera(64, Vector3f.ZERO);
            previewEnvironmentCamera = new EnvironmentCamera(64, Vector3f.ZERO);
            stateManager.attach(environmentCamera);
            stateManager.attach(previewEnvironmentCamera);
        }

        createProbe();
    }

    @Override
    public void loseFocus() {
        super.loseFocus();

        final WindowChangeFocusEvent event = new WindowChangeFocusEvent();
        event.setFocused(false);

        final FXEventManager eventManager = FXEventManager.getInstance();
        eventManager.notify(event);
    }

    @Override
    public void gainFocus() {
        super.gainFocus();

        final WindowChangeFocusEvent event = new WindowChangeFocusEvent();
        event.setFocused(true);

        final FXEventManager eventManager = FXEventManager.getInstance();
        eventManager.notify(event);
    }
    @Override
    public void simpleUpdate(final float tpf) {
        super.simpleUpdate(tpf);

        previewNode.updateLogicalState(tpf);
        previewNode.updateGeometricState();
    }

    @Override
    public void update() {
        // finish if the editor state isn't for updating the scene
        if (!EditorStateManager.isUpdating()) {
            return;
        }
        // update the editor enqueued components before being hooked to jme3 update
        final GLTaskExecutor editorGLTaskExecutor = GLTaskExecutor.getInstance();
        editorGLTaskExecutor.dispatch();
        // hook up jme3 update --> calls --> simpleUpdate
        super.update();
        listener.setLocation(cam.getLocation());
        listener.setRotation(cam.getRotation());
    }

    /**
     * Gets post processor.
     *
     * @return the processor of post effects.
     */
    public FilterPostProcessor getPostProcessor() {
        return notNull(postProcessor);
    }

    /**
     * Create the light probe for the PBR render.
     */
    private void createProbe() {
        final EnvironmentCamera environmentCamera = getEnvironmentCamera();
        final EnvironmentCamera previewEnvironmentCamera = getPreviewEnvironmentCamera();
        if (environmentCamera == null || previewEnvironmentCamera == null) {
            return;
        }
        if (environmentCamera.getApplication() == null) {
            final GLTaskExecutor gameGLTaskExecutor = GLTaskExecutor.getInstance();
            gameGLTaskExecutor.addToExecute(this::createProbe);
            return;
        }
        lightProbe = makeProbe(environmentCamera, rootNode, EMPTY_JOB_ADAPTER);
        previewLightProbe = makeProbe(previewEnvironmentCamera, previewNode, EMPTY_JOB_ADAPTER);
        BoundingSphere bounds = (BoundingSphere) lightProbe.getBounds();
        bounds.setRadius(100);
        bounds = (BoundingSphere) previewLightProbe.getBounds();
        bounds.setRadius(100);
        rootNode.addLight(lightProbe);
        previewNode.addLight(previewLightProbe);
    }

    /**
     * Update the light probe.
     *
     * @param progressAdapter the progress adapter
     */
    public void updateProbe(final JobProgressAdapter<LightProbe> progressAdapter) {
        final LightProbe lightProbe = getLightProbe();
        final EnvironmentCamera environmentCamera = getEnvironmentCamera();

        if (lightProbe == null || environmentCamera == null) {
            progressAdapter.done(null);
            return;
        }
        LightProbeFactory.updateProbe(lightProbe, environmentCamera, rootNode, progressAdapter);
    }

    /**
     * Update the light probe.
     *
     * @param progressAdapter the progress adapter
     */
    public void updatePreviewProbe(final JobProgressAdapter<LightProbe> progressAdapter) {
        final LightProbe lightProbe = getPreviewLightProbe();
        final EnvironmentCamera environmentCamera = getPreviewEnvironmentCamera();
        if (lightProbe == null || environmentCamera == null) {
            progressAdapter.done(null);
            return;
        }
        LightProbeFactory.updateProbe(lightProbe, environmentCamera, previewNode, progressAdapter);
    }

    /**
     * Sets paused.
     *
     * @param paused true if this app is paused.
     */
    protected void setPaused(final boolean paused) {
        this.paused = paused;
    }

    /**
     * @return the light probe.
     */
    private LightProbe getLightProbe() {
        return lightProbe;
    }

    /**
     * Gets preview view port.
     *
     * @return The preview view port.
     */
    public ViewPort getPreviewViewPort() {
        return previewViewPort;
    }

    /**
     * @return the preview light probe.
     */
    private LightProbe getPreviewLightProbe() {
        return previewLightProbe;
    }

    /**
     * @return the environment camera.
     */
    private EnvironmentCamera getEnvironmentCamera() {
        return environmentCamera;
    }

    /**
     * Gets preview camera.
     *
     * @return the camera for preview.
     */
    public Camera getPreviewCamera() {
        return previewCamera;
    }

    /**
     * @return the preview environment camera.
     */
    private EnvironmentCamera getPreviewEnvironmentCamera() {
        return previewEnvironmentCamera;
    }

    /**
     * Gets preview node.
     *
     * @return the node for preview.
     */
    public Node getPreviewNode() {
        return previewNode;
    }

    /**
     * Gets tone map filter.
     *
     * @return the filter of color correction.
     */
    public ToneMapFilter getToneMapFilter() {
        return toneMapFilter;
    }

    /**
     * Gets fxaa filter.
     *
     * @return The FXAA filter.
     */
    public FXAAFilter getFXAAFilter() {
        return fxaaFilter;
    }

    /**
     * Gets translucent bucket filter.
     *
     * @return the translucent bucket filter.
     */
    public TonegodTranslucentBucketFilter getTranslucentBucketFilter() {
        return translucentBucketFilter;
    }

    /**
     * Gets a default material.
     *
     * @return the default material.
     */
    public Material getDefaultMaterial() {
        return defaultMaterial;
    }
}
