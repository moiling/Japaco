package com.moi.test.testall;

public class StaticTestAll {
    public static void testAll(int a, int b) {
        switch (a) {
            case 0:
                System.out.println("a == 0");
                break;
            case 1:
                System.out.println("a == 1");
                break;
            case 2:
                System.out.println("a == 2");
                break;
            default:
                System.out.println("a != (0 or 1 or 2)");
                break;
        }

        for (int i = 0; i < b; i++) {
            System.out.println("b = " + i);
        }

        if (a > b) {
            System.out.println("a > b");
        } else if (a < b) {
            System.out.println("a < b");
        } else {
            System.out.println("a == b");
        }
    }
}
