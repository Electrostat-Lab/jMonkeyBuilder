package com.ss.editor;

import com.ss.editor.manager.ExecutorManager;

public class Launcher {

    public static void main(String[] args) {
        ExecutorManager.dispatchGLThread();
    }

}
