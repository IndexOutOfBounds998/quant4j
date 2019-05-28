package com.quant.core.trading.impl;

import com.quant.common.domain.response.OrdersDetail;
import com.quant.core.trading.OpenOrder;
import com.quant.common.utils.DateUtils;

import java.math.BigDecimal;
import java.util.Date;

public class HuoBiOpenOrderImpl implements OpenOrder {

    private OrdersDetail ordersDetail;

    public HuoBiOpenOrderImpl(OrdersDetail ordersDetail) {
        this.ordersDetail = ordersDetail;
    }

    @Override
    public String getId() {
        return String.valueOf(ordersDetail.getId());
    }

    @Override
    public Date getCreationDate() {

        return DateUtils.parseTimeMillisToDate(ordersDetail.getCreatedAt());
    }

    @Override
    public String getMarketId() {
        return ordersDetail.getSymbol();
    }

    @Override
    public String getType() {
        return ordersDetail.getType();
    }

    @Override
    public BigDecimal getPrice() {
        return new BigDecimal(ordersDetail.getPrice());
    }

    @Override
    public String getFilledAmount() {
        return ordersDetail.getFieldAmount();
    }

    @Override
    public String getFilledCashAmount() {
        return ordersDetail.getFieldCashAmount();
    }

    @Override
    public String getFilledFees() {
        return ordersDetail.getFieldFees();
    }

    @Override
    public String getSource() {
        return ordersDetail.getSource();
    }

    @Override
    public String getState() {
        return ordersDetail.getState();
    }
}
