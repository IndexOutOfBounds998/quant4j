package com.qklx.qt.core;

import com.alibaba.fastjson.JSON;
import com.qklx.qt.common.config.VpnProxyConfig;
import com.qklx.qt.core.api.ApiClient;
import com.qklx.qt.core.request.DepthRequest;
import com.qklx.qt.core.response.Depth;
import com.qklx.qt.core.response.DepthResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by yang on 2019/5/20.
 */
public class Main {
    public static void main(String[] args) {
        long currentTimeMillis = System.currentTimeMillis();
        long min20 = currentTimeMillis + 20 * 60 * 1000;
        HashMap<String, Depth> depthHashMap = new HashMap<>();
        VpnProxyConfig vpnProxyConfig = new VpnProxyConfig();
        vpnProxyConfig.setEnable(true);
        vpnProxyConfig.setIp("127.0.0.1");
        vpnProxyConfig.setPort(1088);
        ApiClient apiClient = new ApiClient(vpnProxyConfig);
        DepthRequest depthRequest = new DepthRequest();
        depthRequest.setSymbol("ethusdt");
        depthRequest.setType("step1");
        while (System.currentTimeMillis() < min20) {
            DepthResponse depth = apiClient.depth(depthRequest);
            if (depth.getStatus().equals("ok")) {
                depthHashMap.put(depth.getTs(), depth.getTick());
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        File file = new File("d:\\dept_ethusdt.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 根据文件创建文件的输出流
        try (OutputStream os = new FileOutputStream(file)) {
            String str = JSON.toJSONString(depthHashMap);
            // 把内容转换成字节数组
            byte[] data = str.getBytes();
            // 向文件写入内容
            os.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
