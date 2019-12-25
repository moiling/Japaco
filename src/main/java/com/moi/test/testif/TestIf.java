package com.moi.test.testif;

public class TestIf {
    public void testIf(int a) {
        if (a > 0) {
            doAMoreThanZero();
        } else if (a < 0) {
            doALessThanZero();
        } else {
            doAEqualZero();
        }
    }

    private void doAMoreThanZero() {
        new More().run();
    }

    private void doALessThanZero() {
        new Less().run();
    }

    private void doAEqualZero() {
        new Equal().run();
    }
}
