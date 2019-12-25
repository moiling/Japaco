package com.moi.test;

public class B {
    public static long timer;
    public void m() throws InterruptedException {
        timer -= System.currentTimeMillis();
        Thread.sleep(100);
        timer += System.currentTimeMillis();
    }
}
