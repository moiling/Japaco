package com.moi.test.sample;

public class Computer {
    private int a;
    private int b;

    public Computer(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int add() {
        return a + b;
    }

    public int sub() {
        return a - b;
    }

    public int div() {
        if (b != 0) {
            return a / b;
        } else {
            return -1;
        }
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
}
