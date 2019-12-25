package com.moi.test.testArray;

import com.moi.test.Data;

import java.util.ArrayList;

public class TestArray {

    public void testArray() {
        Data.array.add("L0->");
        addMethod();
        new AddArrayClass().addMethod();
    }

    private void addMethod() {
        Data.array.add("L1->");
    }

}
