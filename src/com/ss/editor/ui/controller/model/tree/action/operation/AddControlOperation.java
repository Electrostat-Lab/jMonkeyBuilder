package com.ss.editor.ui.controller.model.tree.action.operation;

import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.ss.editor.model.undo.editor.ModelChangeConsumer;
import com.ss.editor.model.undo.impl.AbstractEditorOperation;

import org.jetbrains.annotations.NotNull;

/**
 * The implementation of the {@link AbstractEditorOperation} to add a controller to a node.
 *
 * @author JavaSaBr
 */
public class AddControlOperation extends AbstractEditorOperation<ModelChangeConsumer> {

    /**
     * The new controller.
     */
    @NotNull
    private final Control newControl;

    /**
     * The parent.
     */
    @NotNull
    private final Spatial spatial;

    /**
     * Instantiates a new Add controller operation.
     *
     * @param newControl the new controller
     * @param spatial    the spatial
     */
    public AddControlOperation(@NotNull final Control newControl, @NotNull final Spatial spatial) {
        this.newControl = newControl;
        this.spatial = spatial;
    }

    @Override
    protected void redoImpl(@NotNull final ModelChangeConsumer editor) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> {
            spatial.addControl(newControl);
            EXECUTOR_MANAGER.addFXTask(() -> editor.notifyAddedChild(spatial, newControl, -1));
        });
    }

    @Override
    protected void undoImpl(@NotNull final ModelChangeConsumer editor) {
        EXECUTOR_MANAGER.addEditorThreadTask(() -> {
            spatial.removeControl(newControl);
            EXECUTOR_MANAGER.addFXTask(() -> editor.notifyRemovedChild(spatial, newControl));
        });
    }
}
