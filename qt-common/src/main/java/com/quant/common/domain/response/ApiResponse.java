package com.quant.common.domain.response;

import com.quant.common.exception.ApiException;
import lombok.Data;

@Data
public class ApiResponse<T> {

    public String status;
    public String errCode;
    public String errMsg;
    public T data;

    public T checkAndReturn() {
        if ("ok".equals(status)) {
            return data;
        }
        throw new ApiException(errCode, errMsg);
    }
}
