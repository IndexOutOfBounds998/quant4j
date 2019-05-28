package com.quant.admin.service;

import com.baomidou.mybatisplus.service.IService;
import com.quant.admin.entity.Robot;
import com.quant.core.api.ApiResult;
import com.quant.common.domain.vo.RobotVo;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yang
 * @since 2019-04-17
 */
public interface RobotService extends IService<Robot> {

    ApiResult addOrUpdateRobot(RobotVo vo);

    ApiResult list(String uid);

    ApiResult operatingRobot(Integer id,Integer state, String uid);

    ApiResult deleteRobot(String uid,int id);

    boolean editRobotRunState(int runState);

    ApiResult getRobotById(@NotBlank int uid);
}
