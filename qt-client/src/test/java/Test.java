import java.math.BigDecimal;
import java.math.RoundingMode;

public class Test {

    public static void main(String[] args) {

        BigDecimal a = new BigDecimal(0.013520);
        BigDecimal b = new BigDecimal(982.54509400);

        BigDecimal c =b.divide(a,4,RoundingMode.DOWN);
        System.out.println(c);

    }
}
