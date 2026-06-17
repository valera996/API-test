package iteration2;

import java.util.Random;

public class DataClass {
    public static String randomUserName(){
        Random random = new Random();
        int randomNum = random.nextInt(1000, 10000);
        StringBuilder name = new StringBuilder();
        name.append("kate").append(randomNum);
        return name.toString();
    }
}
