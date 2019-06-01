package com.quant.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.quant.common.enums.Status;
import com.quant.core.api.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/node")
public class NodeController extends BaseController {
    private static final String clientStr = "qt-client";
    @Qualifier("eurekaClient")
    @Autowired
    EurekaClient eurekaClient;

    @GetMapping("/nodes")
    public ApiResult nodes() {

        Applications applications = eurekaClient.getApplications();
        List<Application> apps = applications.getRegisteredApplications();
        List<JSONObject> list = new ArrayList<>();
        int i = 0;
        for (Application application : apps) {
            if (application.getName().toLowerCase().equals(clientStr)) {
                try {
                    List<InstanceInfo> instanceInfos = new CopyOnWriteArrayList<>(application.getInstances());
                    for (InstanceInfo instanceInfo : instanceInfos) {
                        JSONObject obj = new JSONObject();
                        obj.put("ipo", instanceInfo.getIPAddr() + ":" + instanceInfo.getPort());
                        obj.put("id", i);
                        list.add(obj);
                        i++;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return new ApiResult(Status.ERROR);
                }
            }

        }
        return new ApiResult(Status.SUCCESS, list);
    }
}
