package com.cug.myseckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cug.myseckill.pojo.Order;
import com.cug.myseckill.pojo.User;
import com.cug.myseckill.vo.GoodsVo;
import com.cug.myseckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yzx
 * @since 2023-05-28
 */
public interface IOrderService extends IService<Order> {

    Order seckill(User user, GoodsVo goodsVo);

    OrderDetailVo detail(Long orderId);

    String createPath(User user, Long goodsId);

    boolean checkPath(User user, Long goodsId, String path);

    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
