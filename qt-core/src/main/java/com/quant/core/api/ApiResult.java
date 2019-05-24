package com.quant.core.api;

import com.quant.common.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 返回的数据结构
 */
@Data
@AllArgsConstructor
public class ApiResult {

    int code;

    String message;

    Object data;

    public ApiResult() {
    }

    public ApiResult(Status status, Object res) {
        this.data = res;
        this.message = status.getMsg();
        this.code = status.getCode();
    }

    public ApiResult(Status status) {
        this.message = status.getMsg();
        this.code = status.getCode();
    }
}
