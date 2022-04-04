package com.ss.editor;

import com.ss.editor.manager.*;
import com.ss.rlib.manager.InitializeManager;

/**
 * Launches the editor in a jfx editor thread,
 * the jfx thread then dispatches a new GL_Thread with a jMonkeyEngine application.
 *
 * @author pavl_g
 */
public class Launcher {
    public static void main(String[] args) {


        ExecutorManager.dispatchJfxThread();
    }
}
