package com.ss.editor.file.delete.handler.impl;

import static com.ss.editor.util.EditorUtil.getAssetFile;
import static com.ss.editor.util.EditorUtil.toAssetPath;
import static com.ss.rlib.util.ObjectUtils.notNull;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Spatial;
import com.ss.editor.FileExtensions;
import com.ss.editor.Messages;
import com.ss.editor.ui.dialog.ConfirmDialog;
import com.ss.editor.ui.scene.EditorFXScene;
import com.ss.editor.util.EditorUtil;
import com.ss.editor.util.NodeUtils;
import com.ss.rlib.util.FileUtils;
import com.ss.rlib.util.StringUtils;
import com.ss.rlib.util.array.Array;
import com.ss.rlib.util.array.ArrayFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The implementation of delete handler to delete related materials.
 *
 * @author JavaSaBr
 */
public class DeleteMaterialsModelFileDeleteHandler extends AbstractFileDeleteHandler {

    @Nullable
    private Spatial model;

    @Override
    public void preDelete(@NotNull final Path file) {
        super.preDelete(file);

        final AssetManager assetManager = EDITOR.getAssetManager();
        final Path assetFile = notNull(getAssetFile(file));
        final String assetPath = toAssetPath(assetFile);

        this.model = assetManager.loadModel(assetPath);
    }

    /**
     * @return removed model.
     */
    @NotNull
    private Spatial getModel() {
        return notNull(model);
    }

    @Override
    public void postDelete(@NotNull final Path file) {
        super.postDelete(file);

        String question = Messages.FILE_DELETE_HANDLER_DELETE_MATERIALS;
        question = question.replace("%file_name%", file.getFileName().toString());

        final EditorFXScene scene = JFX_APPLICATION.getScene();
        final ConfirmDialog confirmDialog = new ConfirmDialog(this::handle);
        confirmDialog.show(scene.getWindow());
    }

    private void handle(@NotNull final Boolean result) {
        if (!result) return;

        final Array<String> assetKeys = ArrayFactory.newArray(String.class);

        final Spatial model = getModel();
        NodeUtils.visitGeometry(model, geometry -> {
            final Material material = geometry.getMaterial();
            final String assetName = material.getAssetName();
            if (!StringUtils.isEmpty(assetName)) assetKeys.add(assetName);
        });

        assetKeys.stream().map(EditorUtil::getRealFile)
                .filter(Files::exists)
                .forEach(FileUtils::delete);
    }

    @Override
    public boolean isNeedHandle(@NotNull final Path file) {
        final String extension = FileUtils.getExtension(file);
        return FileExtensions.JME_OBJECT.equals(extension);
    }
}
