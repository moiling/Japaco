package com.moi.japaco;

import java.util.ArrayList;

public class Data {
    public static ArrayList<String> array = new ArrayList<>();

    public static ArrayList<String> getArray() {
        ArrayList<String> result = array;
        array = new ArrayList<>();
        return result;
    }
}
