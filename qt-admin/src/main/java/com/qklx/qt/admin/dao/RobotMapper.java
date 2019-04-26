package com.qklx.qt.admin.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.qklx.qt.admin.model.RobotListModel;
import com.qklx.qt.admin.entity.Robot;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author yang
 * @since 2019-04-17
 */

public interface RobotMapper extends BaseMapper<Robot> {


    List<RobotListModel> getRobotList(String uid);


}
