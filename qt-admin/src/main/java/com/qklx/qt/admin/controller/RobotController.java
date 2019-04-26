package com.qklx.qt.admin.controller;


import com.qklx.qt.admin.service.RobotService;
import com.qklx.qt.core.api.ApiResult;
import com.qklx.qt.core.vo.RobotVo;
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

    @PostMapping("/addRobot")
    public ApiResult addRobot(@RequestBody RobotVo vo, HttpServletRequest request) {
        @NotBlank
        String uid = getUid(request);
        vo.setUserId(uid);
        return robotService.addRobot(vo);
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
    public ApiResult deleteRobot(HttpServletRequest request,int id) {
        @NotBlank
        String uid = getUid(request);
        return robotService.deleteRobot(uid,id);
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

