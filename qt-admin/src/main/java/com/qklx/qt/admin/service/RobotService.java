package com.qklx.qt.admin.service;

import com.baomidou.mybatisplus.service.IService;
import com.qklx.qt.admin.entity.Robot;
import com.qklx.qt.core.api.ApiResult;
import com.qklx.qt.core.vo.RobotVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yang
 * @since 2019-04-17
 */
public interface RobotService extends IService<Robot> {

    ApiResult addRobot(RobotVo vo);

    ApiResult list(String uid);

    ApiResult operatingRobot(Integer id,Integer state, String uid);

    ApiResult deleteRobot(String uid,int id);

    boolean editRobotRunState(int runState);

}
