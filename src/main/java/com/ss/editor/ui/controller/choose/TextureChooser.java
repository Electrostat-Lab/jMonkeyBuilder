package com.ss.editor.ui.controller.choose;

import static com.ss.editor.FileExtensions.TEXTURE_EXTENSIONS;
import static com.ss.editor.util.EditorUtil.getAssetFile;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.ss.editor.Messages;
import com.ss.editor.executor.throwable.JfxThreadException;
import com.ss.editor.manager.JavaFXImageManager;
import com.ss.editor.ui.Icons;
import com.ss.editor.ui.component.asset.tree.context.menu.action.DeleteFileAction;
import com.ss.editor.ui.component.asset.tree.context.menu.action.NewFileAction;
import com.ss.editor.ui.component.asset.tree.context.menu.action.RenameFileAction;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.editor.ui.tooltip.ImageChannelPreview;
import com.ss.editor.ui.util.DynamicIconSupport;
import com.ss.editor.ui.util.UIUtils;
import com.ss.rlib.ui.util.FXUtils;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * The Ui Component to choose textures.
 *
 * @author JavaSaBr
 * @author pavl_g
 */
public class TextureChooser extends HBox {

    private static final Predicate<Class<?>> ACTION_TESTER = type -> type == NewFileAction.class ||
            type == DeleteFileAction.class ||
            type == RenameFileAction.class;

    private static final JavaFXImageManager IMAGE_MANAGER = JavaFXImageManager.getInstance();
    private ImageChannelPreview textureTooltip;
    private ImageView texturePreview;
    private Label textureLabel;
    private HBox wrapper;
    private Path textureFile;
    private Runnable changeHandler;

    /**
     * Instantiates a new Choose texture controller.
     */
    public TextureChooser() {
    }

    /**
     * Handle dropped files to editor.
     */
    private void dragDropped(DragEvent dragEvent) {
        UIUtils.handleDroppedFile(dragEvent, TEXTURE_EXTENSIONS, this, TextureChooser::setTextureFile);
    }

    /**
     * Handle drag over.
     */
    private void dragOver(DragEvent dragEvent) {
        UIUtils.acceptIfHasFile(dragEvent, TEXTURE_EXTENSIONS);
    }

    /**
     * Sets change handler.
     *
     * @param changeHandler the handler.
     */
    public void setChangeHandler(Runnable changeHandler) {
        this.changeHandler = changeHandler;
    }

    /**
     * tThe handler.
     */
    private Runnable getChangeHandler() {
        return changeHandler;
    }

    /**
     * Starts creating Ui components and updating the listeners.
     */
    public final void startUiTransactions() {
        createComponents();
        setOnDragOver(this::dragOver);
        setOnDragDropped(this::dragDropped);
        reload();
        FXUtils.addClassesTo(this, CSSClasses.DEF_HBOX, CSSClasses.CHOOSE_TEXTURE_CONTROL);
    }

    /**
     * Create components.
     */
    protected void createComponents() {

        textureLabel = new Label();
        textureTooltip = new ImageChannelPreview();

        final VBox previewContainer = new VBox();

        texturePreview = new ImageView();
        texturePreview.fitHeightProperty().bind(previewContainer.heightProperty());
        texturePreview.fitWidthProperty().bind(previewContainer.widthProperty());

        Tooltip.install(texturePreview, textureTooltip);

        final Button addButton = new Button();
        addButton.setGraphic(new ImageView(Icons.ADD_12));
        addButton.setOnAction(event -> processAdd());

        final Button removeButton = new Button();
        removeButton.setGraphic(new ImageView(Icons.REMOVE_12));
        removeButton.setOnAction(event -> processRemove());

        wrapper = new HBox(textureLabel, previewContainer, addButton, removeButton);
        wrapper.prefWidthProperty().bind(widthProperty());

        textureLabel.prefWidthProperty().bind(wrapper.widthProperty());

        FXUtils.addToPane(wrapper, this);
        FXUtils.addToPane(texturePreview, previewContainer);

        FXUtils.addClassTo(textureLabel, CSSClasses.CHOOSE_TEXTURE_CONTROL_TEXTURE_LABEL);
        FXUtils.addClassTo(previewContainer, CSSClasses.CHOOSE_TEXTURE_CONTROL_PREVIEW);
        FXUtils.addClassesTo(wrapper, CSSClasses.TEXT_INPUT_CONTAINER, CSSClasses.DEF_HBOX);
        FXUtils.addClassesTo(addButton, removeButton, CSSClasses.FLAT_BUTTON,
                CSSClasses.INPUT_CONTROL_TOOLBAR_BUTTON);

        DynamicIconSupport.addSupport(addButton, removeButton);

        removeButton.disableProperty().bind(texturePreview.imageProperty().isNull());
    }

    /**
     * Gets texture label.
     *
     * @return the label for the path to a texture.
     */
    protected Label getTextureLabel() {
        return notNull(textureLabel);
    }

    /**
     * Gets wrapper.
     *
     * @return the wrapper.
     */

    public final HBox getWrapper() throws JfxThreadException {
        if (wrapper == null) {
            throw JfxThreadException.createExceptionWithMessage("Wrapper hasn't been instantiated !");
        }
        return wrapper;
    }

    /**
     * @return The image channels preview.
     */

    private ImageChannelPreview getTextureTooltip() {
        return notNull(textureTooltip);
    }

    /**
     * Add new texture.
     */
    private void processAdd() {
        UIUtils.openAssetDialog(this, this::setTextureFile, TEXTURE_EXTENSIONS, ACTION_TESTER);
    }

    /**
     * Gets texture file.
     *
     * @return the selected file.
     */
    public Path getTextureFile() {
        return textureFile;
    }

    /**
     * Sets texture file.
     *
     * @param textureFile the selected file.
     */
    public void setTextureFile(Path textureFile) {
        this.textureFile = textureFile;

        reload();

        final Runnable changeHandler = getChangeHandler();
        if (changeHandler != null) changeHandler.run();
    }

    /**
     * Remove the texture.
     */
    protected void processRemove() {
        setTextureFile(null);
    }

    /**
     * @return the image preview.
     */
    private ImageView getTexturePreview() {
        return texturePreview;
    }

    /**
     * Reload.
     */
    protected void reload() {

        final ImageChannelPreview textureTooltip = getTextureTooltip();
        final Label textureLabel = getTextureLabel();
        final ImageView preview = getTexturePreview();

        final Path textureFile = getTextureFile();

        if (textureFile == null) {
            textureLabel.setText(Messages.MATERIAL_MODEL_PROPERTY_CONTROL_NO_TEXTURE);
            preview.setImage(null);
            textureTooltip.showImage(null);
            return;
        }

        final Path assetFile = getAssetFile(textureFile);

        assert assetFile != null;
        textureLabel.setText(assetFile.toString());
        preview.setImage(IMAGE_MANAGER.getTexturePreview(textureFile, 28, 28));
        textureTooltip.showImage(textureFile);
    }
}
