package com.ss.editor.ui.controller.model.property.control;

import static com.ss.editor.ui.controller.model.property.control.ModelPropertyControl.newChangeHandler;

import com.ss.editor.model.undo.editor.ModelChangeConsumer;
import com.ss.editor.ui.controller.property.impl.AbstractDefaultPropertyControl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The default implementation of the property controller.
 *
 * @param <D> the type parameter
 * @param <T> the type parameter
 * @author JavaSaBr
 */
public class DefaultModelPropertyControl<D, T> extends AbstractDefaultPropertyControl<ModelChangeConsumer, D, T> {

    /**
     * Instantiates a new Default model property controller.
     *
     * @param propertyValue  the property value
     * @param propertyName   the property name
     * @param changeConsumer the change consumer
     */
    public DefaultModelPropertyControl(@Nullable final T propertyValue, @NotNull final String propertyName,
                                       @NotNull final ModelChangeConsumer changeConsumer) {
        super(propertyValue, propertyName, changeConsumer, newChangeHandler());
    }
}
