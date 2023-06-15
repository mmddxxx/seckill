package com.cug.myseckill.exception;

import com.cug.myseckill.vo.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 全局异常
 */
@Data
@NoArgsConstructor  //生成一个无参构造方法
@AllArgsConstructor  //生成一个全参构造方法
public class GlobalException extends RuntimeException{
    private RespBeanEnum respBeanEnum;
}
