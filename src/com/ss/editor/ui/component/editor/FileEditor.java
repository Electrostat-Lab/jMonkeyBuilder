package com.ss.editor.ui.component.editor;

import com.ss.editor.state.editor.EditorAppState;

import com.ss.editor.util.EditorStateManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import javafx.beans.property.BooleanProperty;
import javafx.scene.Parent;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;

/**
 * The interface for implementing file editor.
 *
 * @author JavaSaBr.
 */
public interface FileEditor {

    /**
     * The Empty states.
     */
    Array<EditorAppState> EMPTY_STATES = ArrayFactory.newArray(EditorAppState.class);

    /**
     * Get the page for showing the editor.
     *
     * @return the page for showing the editor.
     */
    Parent getPage();

    /**
     * Gets file name.
     *
     * @return the file name of the current opened file.
     */
    String getFileName();

    /**
     * Gets edit file.
     *
     * @return the editing file.
     */
    Path getEditFile();

    /**
     * Open the file.
     *
     * @param file the file.
     */
    void openFile(@NotNull final Path file);

    /**
     * Dirty property boolean property.
     *
     * @return the dirty property of this editor.
     */
    BooleanProperty dirtyProperty();

    /**
     * Is dirty boolean.
     *
     * @return true if the current file was changed.
     */
    boolean isDirty();

    default void doSave() {
    }

    default void onSaved() {
    }

    /**
     * Gets states.
     *
     * @return the 3D part of this editor.
     */
    default Array<EditorAppState> getStates() {
        return EMPTY_STATES;
    }

    /**
     * Notify that this editor was closed.
     */
    default void onClosed() {
    }

    /**
     * Notify about renamed files.
     *
     * @param prevFile the prev file
     * @param newFile  the new file
     */
    default void onRenamed(@NotNull final Path prevFile, @NotNull final Path newFile) {
    }

    /**
     * Notify about moved file.
     *
     * @param prevFile the prev file
     * @param newFile  the new file
     */
    default void onMoved(@NotNull final Path prevFile, @NotNull final Path newFile) {
    }

    /**
     * Gets description.
     *
     * @return the description of this editor.
     */
    EditorDescription getDescription();

    /**
     * Notify that this editor was showed.
     */
    default void onShown() {
    }

    /**
     * Notify that this editor was hided.
     */
    default void onDismissed() {
    }

    /**
     * Is inside boolean.
     *
     * @param sceneX the scene x
     * @param sceneY the scene y
     * @return true if the point is inside in this editor.
     */
    default boolean isInside(double sceneX, double sceneY) {
        return false;
    }
}
