package com.cug.myseckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cug.myseckill.pojo.SeckillOrder;
import com.cug.myseckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yzx
 * @since 2023-05-28
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User user, Long goodsId);
}
