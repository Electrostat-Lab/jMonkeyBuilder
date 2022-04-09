package com.ss.editor.ui.component.editing.terrain.paint;

import static com.ss.editor.ui.component.editing.terrain.TerrainEditingComponent.FIELD_PERCENT;
import static com.ss.editor.ui.component.editing.terrain.TerrainEditingComponent.LABEL_PERCENT;
import com.ss.editor.Messages;
import com.ss.editor.ui.controller.choose.NominatedTextureController;
import com.ss.editor.ui.controller.property.AbstractPropertyControl;
import com.ss.editor.ui.css.CSSClasses;
import com.ss.rlib.ui.control.input.FloatTextField;
import com.ss.rlib.ui.util.FXUtils;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import java.nio.file.Path;

/**
 * Texture layer cell adapter.
 *
 * @author JavaSaBr
 * @author pavl_g.
 */
public class TextureLayerCell extends ListCell<TextureLayer> {

    private NominatedTextureController diffuseTextureControl;
    private NominatedTextureController normalTextureControl;

    private FloatTextField scaleField;
    private Label layerField;
    private GridPane settingContainer;
    private Label scaleController;

    private boolean ignoreListeners;

    /**
     * Instantiates a new Texture layer cell.
     *
     * @param prefWidth the pref width
     * @param maxWidth  the max width
     */
    public TextureLayerCell(DoubleBinding prefWidth, DoubleBinding maxWidth) {
        initContainer(prefWidth, maxWidth);

        initLayerImageField();
        initDiffuseTextureController();
        initNormalTextureController();
        initScaleController();

        FXUtils.addClassTo(settingContainer, CSSClasses.DEF_GRID_PANE);
        FXUtils.addClassTo(layerField, CSSClasses.ABSTRACT_PARAM_CONTROL_PARAM_NAME);
        FXUtils.addClassTo(this, CSSClasses.PROCESSING_COMPONENT_TERRAIN_EDITOR_TEXTURE_LAYER);
        FXUtils.addClassTo(scaleController, CSSClasses.ABSTRACT_PARAM_CONTROL_PARAM_NAME_SINGLE_ROW);
        FXUtils.addClassTo(scaleField, CSSClasses.ABSTRACT_PARAM_CONTROL_COMBO_BOX);

    }
    private void initContainer(DoubleBinding prefWidth, DoubleBinding maxWidth) {
        settingContainer = new GridPane();
        settingContainer.prefWidthProperty().bind(prefWidth);
        settingContainer.maxWidthProperty().bind(maxWidth);
    }
    private void initLayerImageField() {
        layerField = new Label();
        layerField.prefWidthProperty().bind(settingContainer.widthProperty());
        settingContainer.add(layerField, 0, 0, 2, 1);
    }
    private void initDiffuseTextureController() {
        diffuseTextureControl = new NominatedTextureController("Diffuse");
        diffuseTextureControl.startUiTransactions();
        diffuseTextureControl.setChangeHandler(this::updateDiffuse);
        diffuseTextureControl.setControlWidthPercent(AbstractPropertyControl.CONTROL_WIDTH_PERCENT_2);
        settingContainer.add(diffuseTextureControl, 0, 1, 2, 1);
    }
    private void initNormalTextureController() {
        normalTextureControl = new NominatedTextureController("Normal");
        normalTextureControl.startUiTransactions();
        normalTextureControl.setChangeHandler(this::updateNormal);
        normalTextureControl.setControlWidthPercent(AbstractPropertyControl.CONTROL_WIDTH_PERCENT_2);
        settingContainer.add(normalTextureControl, 0, 2, 2, 1);
    }
    private void initScaleController() {
        scaleController = new Label(Messages.EDITING_COMPONENT_SCALE + ":");
        scaleController.prefWidthProperty().bind(settingContainer.widthProperty().multiply(LABEL_PERCENT));

        scaleField = new FloatTextField();
        scaleField.setMinMax(0.0001F, Integer.MAX_VALUE);
        scaleField.setScrollPower(3F);
        scaleField.addChangeListener((observable, oldValue, newValue) -> updateScale(newValue));
        scaleField.prefWidthProperty().bind(settingContainer.widthProperty().multiply(FIELD_PERCENT));

        settingContainer.add(scaleController, 0, 3);
        settingContainer.add(scaleField, 1, 3);

    }
    /**
     * @return the texture scale field.
     */
    private FloatTextField getScaleField() {
        return scaleField;
    }

    /**
     * @return the diffuse texture chooser.
     */
    public NominatedTextureController getDiffuseTextureControl() {
        return diffuseTextureControl;
    }

    /**
     * @return the normal texture chooser.
     */
    private NominatedTextureController getNormalTextureControl() {
        return normalTextureControl;
    }

    /**
     * @return the layer field.
     */
    private Label getLayerField() {
        return layerField;
    }

    /**
     * @return the settings container.
     */
    private GridPane getSettingContainer() {
        return settingContainer;
    }

    /**
     * @param ignoreListeners true if need to ignore listeners.
     */
    private void setIgnoreListeners(final boolean ignoreListeners) {
        this.ignoreListeners = ignoreListeners;
    }

    /**
     * @return true if need to ignore listeners.
     */
    private boolean isIgnoreListeners() {
        return ignoreListeners;
    }

    /**
     * Update texture scale.
     */
    private void updateScale(final Float newValue) {
        if (isIgnoreListeners()) {
            return;
        }
        getItem().setScale(newValue);
    }

    /**
     * Update a normal texture.
     */
    private void updateNormal() {
        if (isIgnoreListeners()) {
            return;
        }
        final NominatedTextureController normalTextureControl = getNormalTextureControl();
        final Path textureFile = normalTextureControl.getTextureFile();
        getItem().setNormalFile(textureFile);
    }

    /**
     * Update a diffuse texture.
     */
    private void updateDiffuse() {
        if (isIgnoreListeners() ) {
            return;
        }
        final NominatedTextureController diffuseTextureControl = getDiffuseTextureControl();
        final Path textureFile = diffuseTextureControl.getTextureFile();
        getItem().setDiffuseFile(textureFile);
    }

    @Override
    protected void updateItem(final TextureLayer item, final boolean empty) {
        super.updateItem(item, empty);

        setText("");

        if (item == null) {
            setGraphic(null);
            return;
        }

        refresh();
        setGraphic(getSettingContainer());
    }

    /**
     * Refresh this cell.
     */
    protected final void refresh() {

        final TextureLayer item = getItem();
        if (item == null) return;

        setIgnoreListeners(true);
        try {

            final FloatTextField scaleField = getScaleField();
            scaleField.setValue(item.getScale());

            final NominatedTextureController normalTextureControl = getNormalTextureControl();
            normalTextureControl.setTextureFile(item.getNormalFile());

            final NominatedTextureController diffuseTextureControl = getDiffuseTextureControl();
            diffuseTextureControl.setTextureFile(item.getDiffuseFile());

            final Label layerField = getLayerField();
            layerField.setText(Messages.EDITING_COMPONENT_LAYER + " #" + (item.getLayer() + 1));

        } finally {
            setIgnoreListeners(false);
        }
    }
}
