package com.quant.core.response;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 17:52
 */

public class Batchcancel<T1, T2> {
    private T1 success;
    private T2 failed;

    public T1 getSuccess() {
        return success;
    }

    public void setSuccess(T1 success) {
        this.success = success;
    }

    public T2 getFailed() {
        return failed;
    }

    public void setFailed(T2 failed) {
        this.failed = failed;
    }
}
