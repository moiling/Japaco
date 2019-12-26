package com.moi.test.testReturn;

public class TestMultiReturn {
    public static void test(int a, int b) {
        switch (a) {
            case 0:
                System.out.println("a = 0");
                break;
            case 1:
                System.out.println("a = 1");
                break;
            case 2:
                System.out.println("a = 2");
                break;
            default:
                System.out.println("a != (0 or 1 or 2)");
                break;
        }

        for (int i = 0; i < b; i++) {
            switch (i) {
                case 0:
                    System.out.println("i == 0");
                    break;
                case 1:
                    System.out.println("i == 1");
                    break;
                default:
                    return;
            }
        }

        for (int i = 0; i < a; i++) {
            System.out.println(i);
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
