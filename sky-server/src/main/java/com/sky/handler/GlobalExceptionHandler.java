package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import static io.lettuce.core.GeoArgs.Unit.m;
import static io.netty.util.internal.SystemPropertyUtil.contains;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }


    //处理sql异常
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        if (message.contains("Duplicate entry"))
        //如果异常中包含键值对重复
        {
            //  Duplicate entry 'zhangsan' for key 'idx_username'
            String username = message.split(" ")[2];
            //把异常语句分成几个部分提取信息

            String msg = username + MessageConstant.ALREADY_EXISTS;
            //输出已存在异常

            return Result.error(msg);
        } else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
            //输出未知错误
        }
    }
}
