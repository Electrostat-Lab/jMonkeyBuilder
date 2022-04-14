package com.ss.editor;

import com.ss.editor.manager.ExecutorManager;

/**
 * Launches the editor in a jfx editor thread,
 * the jfx thread then dispatches a new GL_Thread with a jMonkeyEngine application.
 *
 * @author pavl_g
 */
public class Launcher {

    public static void main(String[] args) {
        ExecutorManager.dispatchJfxThread();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                semaphore.waitForUnlock();
//                testMutex();
//            }
//        }).start();
//        new Thread(() -> {
//            System.out.println("Started");
//            mutex.setLockData(EditorStateManager.State.INITIALIZING);
//
//            mutex.setMonitorObject(mutex);
//            // lock the mutex
//            semaphore.lock(mutex);
//            // do some action
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            semaphore.unlock(mutex);
//
//        }).start();
//


    }
    public static void testMutex() {
        System.out.println("Reached finally");
    }
}
