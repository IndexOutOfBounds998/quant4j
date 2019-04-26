package com.qklx.qt.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ComputeUtil {

    /**
     * 根据已有平均值、总量、新增值递推新平均值的公共函数, 计算公式： newAvg = (oldAvg * oldCount)/(oldCount+1) + newVal/(oldCount+1);
     * 另外由于目前的需求是保留2位小数，所以当oldCount大到一定程度时，oldCount/(oldCount+1) 趋近于 1 且 1/(oldCount+1) 趋近于 0，就不必执行某些实际计算以减少不必要操作
     *
     * @param oldAvg   老平均值
     * @param oldCount 老的总量
     * @param newVal   新值
     * @return 新平均值
     */
    public static BigDecimal proceedAvg(BigDecimal oldAvg, long oldCount, long newVal) {
        if (oldAvg == null) return new BigDecimal(newVal).setScale(2);
        // 要保留2位小数, 即需要舍入第三位小数，则若'+'前半部分计算对结果造成的影响小于0.0001, 则不必计算, 即： oldAvg - oldAvg * (oldCount/(oldCount+1)) < 0.0001
        // => oldAvg < 0.0001 * (oldCount+1)
        // 同理，后半部分若满足 newVal * (1/(oldCount+1)) < 0.0001 也不用算了, 即： newVal < (oldCount+1) * 0.0001
        // 且由于：newAvg = oldAvg - oldAvg/(oldCount+1) + newVal/(oldCount+1),
        // 那么，若: |newVal - oldAvg|/(oldCount+1) < 0.0001 整个式子就不用算了
        double delta = 0.0001 * (oldCount + 1);
        if (Math.abs(newVal - oldAvg.doubleValue()) < delta) return oldAvg;
        BigDecimal firstPart = null;
        if (oldAvg.doubleValue() < delta) {
            firstPart = oldAvg;
        } else {
            firstPart = oldAvg.multiply(new BigDecimal(oldCount)).divide(new BigDecimal(oldCount + 1), 4,
                    RoundingMode.HALF_UP);
        }

        BigDecimal secondPart = null;
        if (newVal < delta) {
            secondPart = BigDecimal.ZERO;
        } else {
            secondPart = new BigDecimal(newVal).divide(new BigDecimal(oldCount + 1), 4, RoundingMode.HALF_UP);
        }
        return firstPart.add(secondPart).setScale(2, RoundingMode.HALF_UP);
    }
}
