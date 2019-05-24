package com.quant.common.enums;


/**
 * 返回骂
 */
public enum Status {

    SUCCESS(20000, "请求成功"),
    USER_NOT_EXIST(1001, "用户不存在"),
    KEYS_NOT_Available(1002, "accesskey和秘钥 不可用"),
    Account_maybe_exist(1003, "该api已经存在或者数据服务器异常"),
    getRobotListError(1004, "获取机器人列表发生错误"),
    startRobotError(1005, "启动client端机器人失败"),
    Login_out(50014, "登录过期"),
    ERROR(1000, "请求异常 (服务器或数据库异常)");

    int code;
    String msg;

    Status(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }}
