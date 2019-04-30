package com.qklx.temp.entity;

import com.baomidou.mybatisplus.enums.IdType;
import java.util.Date;
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
public class Robot extends Model<Robot> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField("user_id")
    private Integer userId;
    @TableField("account_id")
    private Integer accountId;
    @TableField("robot_name")
    private String robotName;
    private String symbol;
    @TableField("strategy_id")
    private Integer strategyId;
    @TableField("client_address")
    private String clientAddress;
    @TableField("create_time")
    private Date createTime;
    /**
     * 0未删除 1已经删除
     */
    @TableField("is_delete")
    private Integer isDelete;
    /**
     * 0未启动 1已经启动
     */
    @TableField("is_run")
    private Integer isRun;


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

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Integer strategyId) {
        this.strategyId = strategyId;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public Integer getIsRun() {
        return isRun;
    }

    public void setIsRun(Integer isRun) {
        this.isRun = isRun;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Robot{" +
        ", id=" + id +
        ", userId=" + userId +
        ", accountId=" + accountId +
        ", robotName=" + robotName +
        ", symbol=" + symbol +
        ", strategyId=" + strategyId +
        ", clientAddress=" + clientAddress +
        ", createTime=" + createTime +
        ", isDelete=" + isDelete +
        ", isRun=" + isRun +
        "}";
    }
}
