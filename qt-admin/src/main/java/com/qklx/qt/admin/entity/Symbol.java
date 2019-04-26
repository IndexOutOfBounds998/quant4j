package com.qklx.qt.admin.entity;

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
 * @since 2019-04-17
 */
public class Symbol extends Model<Symbol> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;
    @TableField("base_currency")
    private String baseCurrency;
    @TableField("quote_currency")
    private String quoteCurrency;
    @TableField("price_precision")
    private Integer pricePrecision;
    @TableField("amount_precision")
    private Integer amountPrecision;
    private String symbol;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public void setQuoteCurrency(String quoteCurrency) {
        this.quoteCurrency = quoteCurrency;
    }

    public Integer getPricePrecision() {
        return pricePrecision;
    }

    public void setPricePrecision(Integer pricePrecision) {
        this.pricePrecision = pricePrecision;
    }

    public Integer getAmountPrecision() {
        return amountPrecision;
    }

    public void setAmountPrecision(Integer amountPrecision) {
        this.amountPrecision = amountPrecision;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Symbol{" +
        ", id=" + id +
        ", baseCurrency=" + baseCurrency +
        ", quoteCurrency=" + quoteCurrency +
        ", pricePrecision=" + pricePrecision +
        ", amountPrecision=" + amountPrecision +
        ", symbol=" + symbol +
        "}";
    }
}
