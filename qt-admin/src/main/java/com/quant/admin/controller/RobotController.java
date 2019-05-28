package com.quant.admin.controller;


import com.quant.admin.service.RobotService;
import com.quant.core.api.ApiResult;
import com.quant.common.domain.vo.RobotVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author yang
 * @since 2019-04-17
 */
@RestController
@RequestMapping("/robot")
public class RobotController extends BaseController {
    @Autowired
    RobotService robotService;

    @PostMapping("/addOrUpdateRobot")
    public ApiResult addOrUpdateRobot(@RequestBody RobotVo vo, HttpServletRequest request) {
        @NotBlank
        String uid = getUid(request);
        vo.setUserId(uid);
        return robotService.addOrUpdateRobot(vo);
    }


    @GetMapping("/getRobotById")
    public ApiResult getRobotById(int id) {
        return robotService.getRobotById(id);
    }

    /**
     * 获取我的机器人
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult list(HttpServletRequest request) {
        @NotBlank
        String uid = getUid(request);
        return robotService.list(uid);
    }

    /**
     * 删除我的机器人
     *
     * @return
     */
    @GetMapping("/deleteRobot")
    public ApiResult deleteRobot(HttpServletRequest request, int id) {
        @NotBlank
        String uid = getUid(request);
        return robotService.deleteRobot(uid, id);
    }


    /**
     * 启动或者关闭机器人
     *
     * @param id 机器人编号
     * @return
     */
    @GetMapping("/operatingRobot")
    public ApiResult operatingRobot(@NotNull Integer id, @NotBlank Integer state, HttpServletRequest request) {
        @NotBlank
        String uid = getUid(request);
        return robotService.operatingRobot(id, state, uid);
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return null;
    }
}

