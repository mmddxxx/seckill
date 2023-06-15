package com.cug.myseckill.rabbitmq;

import com.cug.myseckill.mapper.GoodsMapper;
import com.cug.myseckill.pojo.Order;
import com.cug.myseckill.pojo.SeckillMessage;
import com.cug.myseckill.pojo.SeckillOrder;
import com.cug.myseckill.pojo.User;
import com.cug.myseckill.service.IGoodsService;
import com.cug.myseckill.service.IOrderService;
import com.cug.myseckill.utils.JsonUtil;
import com.cug.myseckill.vo.GoodsVo;
import com.cug.myseckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 下单操作
     * @param msg
     */
    @RabbitListener(queues = "seckillQueue")
    public void receiveSeckill(String msg) {
        log.info("接收消息：" + msg);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(msg, SeckillMessage.class);
        User user = seckillMessage.getUser();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(seckillMessage.getGoodsId());
        if (goodsVo.getStockCount() < 1) {
            return;
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsVo.getId());
        if (seckillOrder != null) {  //不为null!!!
            return;
        }

        Order order = orderService.seckill(seckillMessage.getUser(), goodsVo);
    }

//    @RabbitListener(queues = "queue")
//    public void receive(Object msg) {
//        log.info("接收消息：" + msg);
//    }
//
//    @RabbitListener(queues = "queue_fanout01")
//    public void receive01(Object msg) {
//        log.info("QUEUE01接收消息：" + msg);
//    }
//
//    @RabbitListener(queues = "queue_fanout02")
//    public void receive02(Object msg) {
//        log.info("QUEUE02接收消息：" + msg);
//    }
//
//    @RabbitListener(queues = "queue_topic01")
//    public void receive03(Object msg) {
//        log.info("QUEUE01接收消息：" + msg);
//    }
//
//    @RabbitListener(queues = "queue_topic02")
//    public void receive04(Object msg) {
//        log.info("QUEUE02接收消息：" + msg);
//    }


}
