package com.qklx.temp.entity;

import com.baomidou.mybatisplus.enums.IdType;
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
public class Strategy extends Model<Strategy> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("user_id")
    private Integer userId;
    @TableField("strategy_name")
    private String strategyName;
    @TableField("buy_amount")
    private BigDecimal buyAmount;
    /**
     * 市价买入交易额
     */
    @TableField("buy_quota_price")
    private BigDecimal buyQuotaPrice;
    @TableField("sell_amount")
    private BigDecimal sellAmount;
    @TableField("buy_price")
    private BigDecimal buyPrice;
    @TableField("sell_price")
    private BigDecimal sellPrice;
    /**
     * 0 不是全部购买
     */
    @TableField("is_all_buy")
    private Integer isAllBuy;
    /**
     * 0不是全部卖出
     */
    @TableField("is_all_sell")
    private Integer isAllSell;
    /**
     * 是否限价交易
     */
    @TableField("is_limit_price")
    private Integer isLimitPrice;
    @TableField("sell_all_weights")
    private Integer sellAllWeights;
    @TableField("buy_all_weights")
    private Integer buyAllWeights;
    /**
     * 亏损次数
     */
    private Integer profit;
    private Integer sleep;
    private String setting1;
    private String setting2;
    private String setting3;
    private String setting4;
    private String setting5;
    private String setting6;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public BigDecimal getBuyAmount() {
        return buyAmount;
    }

    public void setBuyAmount(BigDecimal buyAmount) {
        this.buyAmount = buyAmount;
    }

    public BigDecimal getBuyQuotaPrice() {
        return buyQuotaPrice;
    }

    public void setBuyQuotaPrice(BigDecimal buyQuotaPrice) {
        this.buyQuotaPrice = buyQuotaPrice;
    }

    public BigDecimal getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(BigDecimal sellAmount) {
        this.sellAmount = sellAmount;
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

    public Integer getIsAllBuy() {
        return isAllBuy;
    }

    public void setIsAllBuy(Integer isAllBuy) {
        this.isAllBuy = isAllBuy;
    }

    public Integer getIsAllSell() {
        return isAllSell;
    }

    public void setIsAllSell(Integer isAllSell) {
        this.isAllSell = isAllSell;
    }

    public Integer getIsLimitPrice() {
        return isLimitPrice;
    }

    public void setIsLimitPrice(Integer isLimitPrice) {
        this.isLimitPrice = isLimitPrice;
    }

    public Integer getSellAllWeights() {
        return sellAllWeights;
    }

    public void setSellAllWeights(Integer sellAllWeights) {
        this.sellAllWeights = sellAllWeights;
    }

    public Integer getBuyAllWeights() {
        return buyAllWeights;
    }

    public void setBuyAllWeights(Integer buyAllWeights) {
        this.buyAllWeights = buyAllWeights;
    }

    public Integer getProfit() {
        return profit;
    }

    public void setProfit(Integer profit) {
        this.profit = profit;
    }

    public Integer getSleep() {
        return sleep;
    }

    public void setSleep(Integer sleep) {
        this.sleep = sleep;
    }

    public String getSetting1() {
        return setting1;
    }

    public void setSetting1(String setting1) {
        this.setting1 = setting1;
    }

    public String getSetting2() {
        return setting2;
    }

    public void setSetting2(String setting2) {
        this.setting2 = setting2;
    }

    public String getSetting3() {
        return setting3;
    }

    public void setSetting3(String setting3) {
        this.setting3 = setting3;
    }

    public String getSetting4() {
        return setting4;
    }

    public void setSetting4(String setting4) {
        this.setting4 = setting4;
    }

    public String getSetting5() {
        return setting5;
    }

    public void setSetting5(String setting5) {
        this.setting5 = setting5;
    }

    public String getSetting6() {
        return setting6;
    }

    public void setSetting6(String setting6) {
        this.setting6 = setting6;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Strategy{" +
        ", id=" + id +
        ", userId=" + userId +
        ", strategyName=" + strategyName +
        ", buyAmount=" + buyAmount +
        ", buyQuotaPrice=" + buyQuotaPrice +
        ", sellAmount=" + sellAmount +
        ", buyPrice=" + buyPrice +
        ", sellPrice=" + sellPrice +
        ", isAllBuy=" + isAllBuy +
        ", isAllSell=" + isAllSell +
        ", isLimitPrice=" + isLimitPrice +
        ", sellAllWeights=" + sellAllWeights +
        ", buyAllWeights=" + buyAllWeights +
        ", profit=" + profit +
        ", sleep=" + sleep +
        ", setting1=" + setting1 +
        ", setting2=" + setting2 +
        ", setting3=" + setting3 +
        ", setting4=" + setting4 +
        ", setting5=" + setting5 +
        ", setting6=" + setting6 +
        "}";
    }
}
