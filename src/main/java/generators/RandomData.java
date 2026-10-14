package generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomData {
    private RandomData(){}

    public static String getDoubleName(){
        return RandomStringUtils.randomAlphabetic(10) + " " + RandomStringUtils.randomAlphabetic(10);
    }
    public static String getUserName(){
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String getUserPassword(){
        return  RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                RandomStringUtils.randomNumeric(5) + "%$#";
    }
    public static double getRandomAmount(double min, double max) {
        int minKopecks = (int) Math.round(min * 100);
        int maxKopecks = (int) Math.round(max * 100);

        return ThreadLocalRandom.current()
                .nextInt(minKopecks, maxKopecks + 1) / 100.0;
    }

}
