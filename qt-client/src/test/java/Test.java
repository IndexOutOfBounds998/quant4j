import com.alibaba.fastjson.JSON;
import com.qklx.qt.core.enums.OrderType;
import com.qklx.qt.core.strategy.impl.HuoBiStrategyImpl;

public class Test {

    public static void main(String[] args) {

        HuoBiStrategyImpl.OrderState orderState=new HuoBiStrategyImpl.OrderState();
        orderState.setId(31862982431l);
        orderState.setOrderType(OrderType.BUY_MARKET);
        orderState.setType(com.qklx.qt.core.trading.OrderType.BUY);


        String s = JSON.toJSONString(orderState);
        System.out.println(s);
    }
}
