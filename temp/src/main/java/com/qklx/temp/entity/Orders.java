package com.qklx.temp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.activerecord.Model;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author yang
 * @since 2019-04-30
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
        return "Orders{" +
        ", orderId=" + orderId +
        ", symbol=" + symbol +
        ", robotId=" + robotId +
        ", accountId=" + accountId +
        ", amount=" + amount +
        ", price=" + price +
        ", orderState=" + orderState +
        ", orderType=" + orderType +
        ", createTime=" + createTime +
        ", finishedTime=" + finishedTime +
        ", fieldFees=" + fieldFees +
        ", fieldAmount=" + fieldAmount +
        ", fieldCashAmount=" + fieldCashAmount +
        "}";
    }
}
