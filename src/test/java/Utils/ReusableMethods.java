package Utils;

import java.util.UUID;

public class ReusableMethods {
    public static String randomString() {
        UUID randomUUID = UUID.randomUUID();
        String randomStr = randomUUID.toString().replaceAll("_", "");
        return randomStr.substring(1, 7);
    }

    public static String fetchPrice(String amount) {
        String w[] = amount.split(" ");
        return w[1];
    }

    public static String randomNumber() {
        long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        return Long.toString(number);
    }


}
