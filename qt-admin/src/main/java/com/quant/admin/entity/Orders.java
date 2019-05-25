package com.quant.admin.entity;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.quant.common.utils.DateUtils;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * <p>
 * </p>
 *
 * @author yang
 * @since 2019-04-28
 */
public class Orders extends Model<Orders> {

    private static final long serialVersionUID = 1L;

    @TableId("order_id")
    private Long orderId;
    private String symbol;
    @TableField("robot_id")
    private Integer robotId;
    @TableField("account_id")
    private Long accountId;
    private BigDecimal amount;
    private BigDecimal price;
    @TableField("order_state")
    private String orderState;
    @TableField("order_type")
    private String orderType;
    @TableField("create_time")
    private Long createTime;
    @TableField("finished_time")
    private Long finishedTime;
    @TableField("field_fees")
    private BigDecimal fieldFees;
    @TableField("field_amount")
    private BigDecimal fieldAmount;
    @TableField("field_cash_amount")
    private BigDecimal fieldCashAmount;


    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getRobotId() {
        return robotId;
    }

    public void setRobotId(Integer robotId) {
        this.robotId = robotId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getOrderState() {
        return orderState;
    }

    public void setOrderState(String orderState) {
        this.orderState = orderState;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getFinishedTime() {
        return finishedTime;
    }

    public void setFinishedTime(Long finishedTime) {
        this.finishedTime = finishedTime;
    }

    public BigDecimal getFieldFees() {
        return fieldFees;
    }

    public void setFieldFees(BigDecimal fieldFees) {
        this.fieldFees = fieldFees;
    }

    public BigDecimal getFieldAmount() {
        return fieldAmount;
    }

    public void setFieldAmount(BigDecimal fieldAmount) {
        this.fieldAmount = fieldAmount;
    }

    public BigDecimal getFieldCashAmount() {
        return fieldCashAmount;
    }

    public void setFieldCashAmount(BigDecimal fieldCashAmount) {
        this.fieldCashAmount = fieldCashAmount;
    }

    @Override
    protected Serializable pkVal() {
        return this.orderId;
    }

    @Override
    public String toString() {
        return "订单信息 ======【" +
                "订单id=" + orderId +
                ", 交易对=" + symbol +
                ", 机器人id=" + robotId +
                ", 交易账号id=" + accountId +
                ", 数量=" + amount +
                ", 价格=" + price +
                ", 订单状态=" + orderState +
                ", 订单类型=" + orderType +
                ", 订单创建时间=" + DateUtils.formateDate(DateUtils.parseTimeMillisToDate(createTime),null) +
                ", 订单完成时间=" + DateUtils.formateDate(DateUtils.parseTimeMillisToDate(finishedTime),null) +
                ", 交易手续费=" + fieldFees +
                ", 已成交数量=" + fieldAmount +
                ", 已成交总金额=" + fieldCashAmount +
                "】";
    }
}
