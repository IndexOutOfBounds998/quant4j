package com.qklx.qt.core.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AccountVo {
    Integer id;
    @NotBlank(message = "名称 不能为空")
    String name;

    @NotBlank(message = "accessKey 不能为空")
    String accessKey;

    @NotBlank(message = "secretKey 不能为空")
    String secretKey;

    @NotBlank(message = "用户id 不能为空")
    int userId;

    String info;
}
