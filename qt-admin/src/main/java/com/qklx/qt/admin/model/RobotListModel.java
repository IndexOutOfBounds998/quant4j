package com.qklx.qt.admin.model;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class RobotListModel {
    private static final long serialVersionUID = 1L;


    private Integer id;

    private String robotName;

    private String symbol;

    private String userId;

    private Integer strategyId;

    private String clientAddress;

    private Date createTime;

    private Integer isDelete;

    private Integer isRun;

    private String strategyName;


}
