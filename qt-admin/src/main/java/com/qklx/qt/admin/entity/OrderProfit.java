package com.qklx.qt.admin.entity;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @author yang
 * @since 2019-04-26
 */
@TableName("order_profit")
public class OrderProfit extends Model<OrderProfit> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("sell_order_id")
    private Long sellOrderId;
    @TableField("buy_order_id")
    private Long buyOrderId;
    @TableField("buy_price")
    private BigDecimal buyPrice;
    @TableField("sell_price")
    private BigDecimal sellPrice;
    @TableField("buy_amount")
    private BigDecimal buyAmount;
    @TableField("sell_amount")
    private BigDecimal sellAmount;
    @TableField("is_profit")
    private Integer isProfit;
    private BigDecimal diff;
    private BigDecimal divide;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getSellOrderId() {
        return sellOrderId;
    }

    public void setSellOrderId(Long sellOrderId) {
        this.sellOrderId = sellOrderId;
    }

    public Long getBuyOrderId() {
        return buyOrderId;
    }

    public void setBuyOrderId(Long buyOrderId) {
        this.buyOrderId = buyOrderId;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    public BigDecimal getBuyAmount() {
        return buyAmount;
    }

    public void setBuyAmount(BigDecimal buyAmount) {
        this.buyAmount = buyAmount;
    }

    public BigDecimal getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(BigDecimal sellAmount) {
        this.sellAmount = sellAmount;
    }

    public Integer getIsProfit() {
        return isProfit;
    }

    public void setIsProfit(Integer isProfit) {
        this.isProfit = isProfit;
    }

    public BigDecimal getDiff() {
        return diff;
    }

    public void setDiff(BigDecimal diff) {
        this.diff = diff;
    }

    public BigDecimal getDivide() {
        return divide;
    }

    public void setDivide(BigDecimal divide) {
        this.divide = divide;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "OrderProfit{" +
        ", id=" + id +
        ", sellOrderId=" + sellOrderId +
        ", buyOrderId=" + buyOrderId +
        ", buyPrice=" + buyPrice +
        ", sellPrice=" + sellPrice +
        ", buyAmount=" + buyAmount +
        ", sellAmount=" + sellAmount +
        ", isProfit=" + isProfit +
        ", diff=" + diff +
        ", divide=" + divide +
        "}";
    }
}
