package com.qklx.temp.entity;

import com.baomidou.mybatisplus.enums.IdType;
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
 * @since 2019-04-26
 */
public class Balance extends Model<Balance> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("account_id")
    private Integer accountId;
    /**
     * 账户类型
     */
    private String type;
    /**
     * 币种
     */
    private String currency;
    /**
     * 余额
     */
    @TableField("trade_balance")
    private String tradeBalance;
    /**
     * trade: 交易余额，frozen: 冻结余额
     */
    @TableField("frozen_balance")
    private String frozenBalance;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTradeBalance() {
        return tradeBalance;
    }

    public void setTradeBalance(String tradeBalance) {
        this.tradeBalance = tradeBalance;
    }

    public String getFrozenBalance() {
        return frozenBalance;
    }

    public void setFrozenBalance(String frozenBalance) {
        this.frozenBalance = frozenBalance;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Balance{" +
        ", id=" + id +
        ", accountId=" + accountId +
        ", type=" + type +
        ", currency=" + currency +
        ", tradeBalance=" + tradeBalance +
        ", frozenBalance=" + frozenBalance +
        "}";
    }
}
