package com.mc.create.common;

import lombok.Data;

/**
 * 统一 API 返回结构：{ code, msg, data }
 * code = 0 成功；非 0 失败（msg 为错误原因）。
 */
@Data
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "ok", data);
    }

    public static <T> Result<T> ok() {
        return new Result<>(0, "ok", null);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(1, msg, null);
    }
}
