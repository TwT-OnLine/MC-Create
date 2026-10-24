package com.mc.create.common;

/**
 * 业务异常：service 层抛出，由全局异常处理转为 Result.fail。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}
