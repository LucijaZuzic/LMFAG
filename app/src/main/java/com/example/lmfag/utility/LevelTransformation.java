package com.example.lmfag.utility;

public class LevelTransformation {

    private static final int NUM_POINTS = 1000;

    public static int level(double points) {
        int level = 1;
        while (!(lower_bound(level) <= points && upper_bound(level) >= points)) {
            level++;
        }
        return level;
    }

    public static double lower_bound(int level) {
        if (level < 2) {
            return 0;
        }
        return Math.pow(2, level - 2) * NUM_POINTS;
    }

    public static double upper_bound(int level) {
        if (level < 1) {
            return 0;
        }
        return Math.pow(2, level - 1) * NUM_POINTS;
    }

}
