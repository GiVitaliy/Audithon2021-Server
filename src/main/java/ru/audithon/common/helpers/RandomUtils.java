package ru.audithon.common.helpers;

import java.util.Random;

public class RandomUtils {
    private static Random random = new Random();

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return random.nextInt((max - min) + 1) + min;
    }
}
