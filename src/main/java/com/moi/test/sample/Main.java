package com.moi.test.sample;

import com.moi.test.otherpackage.Printer;

public class Main {
    public static void test(int a, int b, String s) {
        System.out.println("================= This is " + s + " =================");

        Computer computer = new Computer(a, b);
        Printer p = new Printer();
        int c;
        switch (a) {
            case 0:
                c = computer.add();
                break;
            case 1:
                c = computer.sub();
                break;
            default:
                c = computer.div();
        }

        for (int i = 0; i < b; i++) {
            if (i > 1) {
                p.print("[i>1]i = " + i);
            } else {
                p.print("[i<=1]i = " + i);
            }
        }

        other(a, c);
    }

    private static void other(int a, int c) {
        if (a > c) {
            System.out.println("a > c");
        } else if (a < c) {
            System.out.println("a < c");
        } else {
            System.out.println("a == c");
        }
    }
}
