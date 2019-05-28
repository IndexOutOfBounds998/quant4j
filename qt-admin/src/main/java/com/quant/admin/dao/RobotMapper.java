package com.quant.admin.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.quant.common.domain.bo.RobotBo;
import com.quant.common.domain.entity.Robot;

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
    List<RobotBo> getRobotList(String uid);
}
