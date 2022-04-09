package com.ss.editor.ui.controller.filter.property.control;

import static com.ss.editor.ui.controller.filter.property.control.FilterPropertyControl.newChangeHandler;

import com.ss.editor.model.undo.editor.SceneChangeConsumer;
import com.ss.editor.ui.controller.property.AbstractPropertyControl;
import com.ss.editor.ui.controller.property.impl.AbstractFloatPropertyControl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of the {@link AbstractPropertyControl} to edit float values.
 *
 * @param <T> the type parameter
 * @author JavaSaBr
 */
public class FloatFilterPropertyControl<T> extends AbstractFloatPropertyControl<SceneChangeConsumer, T> {

    /**
     * Instantiates a new Float filter property controller.
     *
     * @param propertyValue  the property value
     * @param propertyName   the property name
     * @param changeConsumer the change consumer
     */
    public FloatFilterPropertyControl(@Nullable final Float propertyValue, @NotNull final String propertyName,
                                      @NotNull final SceneChangeConsumer changeConsumer) {
        super(propertyValue, propertyName, changeConsumer, newChangeHandler());
    }
}
