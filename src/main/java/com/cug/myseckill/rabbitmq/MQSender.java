package com.cug.myseckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendseckillMessage(String msg) {
        log.info("发送消息：" + msg);
        rabbitTemplate.convertAndSend("seckillExchange", "seckill.message", msg);  //exchange交换机key, routingKey路由key
    }

//    public void send(Object msg) {
//        log.info("发送消息：" + msg);
//        rabbitTemplate.convertAndSend("fanoutExchange", "", msg);  //exchange交换机key, routingKey路由key
//    }
//
//    public void send01(Object msg) {
//        log.info("发送red消息：" + msg);
//        rabbitTemplate.convertAndSend("directExchange", "queue.red", msg);  //exchange交换机key, routingKey路由key
//    }
//
//    public void send02(Object msg) {
//        log.info("发送green消息：" + msg);
//        rabbitTemplate.convertAndSend("directExchange", "queue.green", msg);  //exchange交换机key, routingKey路由key
//    }
//
//    public void send03(Object msg) {
//        log.info("发送red消息两个收到：" + msg);
//        rabbitTemplate.convertAndSend("topicExchange", "red.queue.red", msg);  //exchange交换机key, routingKey路由key
//    }
//
//    public void send04(Object msg) {
//        log.info("发送green消息一个收到：" + msg);
//        rabbitTemplate.convertAndSend("topicExchange", "red.red.queue.green", msg);  //exchange交换机key, routingKey路由key
//    }
}
