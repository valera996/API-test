package generators;

import com.github.curiousoddman.rgxgen.RgxGen;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomData {
    private RandomData(){}
    private static final double MIN_DEPOSIT_AMOUNT = 0.01;
    private static final double MAX_DEPOSIT_AMOUNT = 5000.00;
    private static final double MAX_TRANSFER_AMOUNT = 10000.00;
    private static final double MIN_TRANSFER_AMOUNT = 0.01;


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
    public static double getValidDepositAmount() {
        return getRandomAmount(MIN_DEPOSIT_AMOUNT, MAX_DEPOSIT_AMOUNT);
    }

    public static double getInvalidDepositAmount() {
        return ThreadLocalRandom.current()
                .nextDouble(MAX_DEPOSIT_AMOUNT, Double.MAX_VALUE);
    }
    public static double getInvalidTransferAmountAboveMax() {
        return getRandomAmount(
                MAX_TRANSFER_AMOUNT + 0.01,
                MAX_TRANSFER_AMOUNT * 2
        );
    }
    public static double getInvalidTransferAmountBelowMin() {
        return getRandomAmount(-10000.00, MIN_TRANSFER_AMOUNT - 0.01);
    }
    public static String getValidName() {
        return new RgxGen("[A-Za-z]{10} [A-Za-z]{10}").generate();
    }
}
