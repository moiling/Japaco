package com.moi.test.sample;

public class Single {
    public static void test(int a, int b, String s) {
        System.out.println("================= This is " + s + " =================");

        for (int i = 0; i < b; i++) {
            switch (i) {
                case 0:
                    System.out.println("0");
                    break;
                case 1:
                    System.out.println("1");
                    break;
                default:
                    return;
            }
        }
    }
}
