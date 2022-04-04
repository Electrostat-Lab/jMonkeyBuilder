package com.ss.editor.ui.controller.choose;

import com.ss.editor.executor.throwable.JfxThreadException;
import com.ss.editor.ui.controller.property.AbstractPropertyControl;
import com.ss.rlib.ui.util.FXUtils;
import com.ss.rlib.util.StringUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.Map;

/**
 * The named controller to choose textures.
 *
 * @author JavaSaBr
 * @author pavl_g
 */
public class NominatedTextureController extends TextureChooser {

    private static final double LABEL_PERCENT = 1D - AbstractPropertyControl.CONTROL_WIDTH_PERCENT;
    private static final double FIELD_PERCENT = AbstractPropertyControl.CONTROL_WIDTH_PERCENT;

    private final Map<Label, String> title = new HashMap<>();
    private final Label name;

    /**
     * Instantiates a new Named choose texture controller.
     *
     * @param name the name
     */
    public NominatedTextureController(String name) {
        super();
        this.name = new Label();
        this.title.put(this.name, name);
    }

    @Override
    protected void createComponents() {

        name.maxWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));
        name.prefWidthProperty().bind(widthProperty().multiply(LABEL_PERCENT));

        FXUtils.addToPane(name, this);

        super.createComponents();

        final HBox wrapper;
        try {
            wrapper = getWrapper();
            wrapper.maxWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
            wrapper.prefWidthProperty().bind(widthProperty().multiply(FIELD_PERCENT));
        } catch (JfxThreadException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void processRemove() {
       super.processRemove();
    }

    /**
     * Sets a width percent of a controller.
     *
     * @param percent the percent.
     */
    public void setControlWidthPercent(final double percent) {

        name.maxWidthProperty().bind(widthProperty().multiply(1D - percent));
        name.prefWidthProperty().bind(widthProperty().multiply(1D - percent));

        final HBox wrapper;
        try {
            wrapper = getWrapper();
            wrapper.maxWidthProperty().bind(widthProperty().multiply(percent));
            wrapper.prefWidthProperty().bind(widthProperty().multiply(percent));
        } catch (JfxThreadException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void reload() {
        super.reload();

        name.setText(title.get(name));
    }
}
