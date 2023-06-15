package com.cug.myseckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cug.myseckill.config.AccessLimit;
import com.cug.myseckill.exception.GlobalException;
import com.cug.myseckill.pojo.Order;
import com.cug.myseckill.pojo.SeckillMessage;
import com.cug.myseckill.pojo.SeckillOrder;
import com.cug.myseckill.pojo.User;
import com.cug.myseckill.rabbitmq.MQSender;
import com.cug.myseckill.service.IGoodsService;
import com.cug.myseckill.service.IOrderService;
import com.cug.myseckill.service.ISeckillOrderService;
import com.cug.myseckill.utils.JsonUtil;
import com.cug.myseckill.vo.GoodsVo;
import com.cug.myseckill.vo.RespBean;
import com.cug.myseckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀
 */
@Slf4j
@Controller
@RequestMapping("/seckill")
//凡是继承该接口的类，在bean的属性初始化后都会执行该方法
public class SeckillController implements InitializingBean {
    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private RedisScript<Long> redisScript;

    //内存标记
    private Map<Long, Boolean> emptyStockMap = new HashMap<>();


//    @RequestMapping(value = "/doSeckill", method = RequestMethod.POST)
//    public RespBean doSecKill(Model model, User user, Long goodsId) {
//        if (user == null) {  //这里导致了死循环
//            return RespBean.error(RespBeanEnum.SESSION_ERROR);
//        }
//        model.addAttribute("user", user);
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//        //判断库存
//        if (goodsVo.getStockCount() < 1) {
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
//        //判断订单
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
//        if (seckillOrder != null) {  //不为null!!!
//            model.addAttribute("errmsg", RespBeanEnum.REPEATE_STOCK.getMessage());
//            return RespBean.error(RespBeanEnum.REPEATE_STOCK);
//        }
//        Order order = orderService.seckill(user, goodsVo);
//        model.addAttribute("order", order);
//        model.addAttribute("goods", goodsVo);
//        return RespBean.success(order);
//    }

    /**
     * 秒杀接口
     * redis优化后:2100qps
     * rabbitmq优化后：2550qps
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping("/doSeckill")
    public String doSecKill(Model model, @PathVariable String path, User user, Long goodsId) {
        if (user == null) {  //这里导致了死循环
            return "login";
        }
        model.addAttribute("user", user);
//        boolean checkPath = orderService.checkPath(user, goodsId, path);
//        if (!checkPath) {
//            return "secKillFail";
//        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //判断订单是否重复抢购(从redis里获取，redis是单线程，但是redis的查询和执行不是原子性)
        SeckillOrder seckillOrder = (SeckillOrder) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {  //不为null!!!
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_STOCK.getMessage());
            return "secKillFail";
        }
        if (emptyStockMap.get(goodsId)) {
            return "secKillFail";
        }
        //预减库存
//        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);//原子性
        long stock = (Long) redisTemplate.execute(redisScript, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);//singletonList只存放一个元素
        if (stock < 0) {
            emptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return "secKillFail";
        }
        //发送消息
        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendseckillMessage(JsonUtil.object2JsonStr(seckillMessage));  //MQ传对象必须序列化，也可以转json
        return "orderDetail";

//        model.addAttribute("user", user);
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//        //判断库存
//        if (goodsVo.getStockCount() < 1) {
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return "secKillFail";
//        }
//        //判断订单是否重复抢购(从redis里获取)
////        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
//        //因为redis是单线程的所以不会出现两个线程进入该代码块?
//        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
//        //但是会同时进入该代码块，但是数据库里添加了唯一索引，也就是说如果有多个相同的值被插入到同一列中，唯一索引将阻止插入操作并抛出异常。
//        if (seckillOrder != null) {  //不为null!!!
//            model.addAttribute("errmsg", RespBeanEnum.REPEATE_STOCK.getMessage());
//            return "secKillFail";
//        }
//        Order order = orderService.seckill(user, goodsVo);
//        model.addAttribute("order", order);
//        model.addAttribute("goods", goodsVo);
//        return "orderDetail";
    }

    @AccessLimit(second = 5, maxCount = 5, needLogin = true)  //自定义注解实现AOP
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha, HttpServletRequest request) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        String path = orderService.createPath(user, goodsId);
        return RespBean.success(path);
    }
    /**
     * 系统初始化，把商品库存数量加载到redis中
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {  //断集合为空(List为null或size()==0)
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            emptyStockMap.put(goodsVo.getId(), false);
        });
    }

    /**
     *
     * @param user
     * @param goodsId
     * @return orderId：成功，-1：秒杀失败，0：排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 验证码
     * @param user
     * @param goodsId
     * @param response
     */
    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
        if (user == null || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        response.setContentType("image/jpg");
        response.setHeader("pargam", "No-cache");
        response.setHeader("Cache-Control", "No-cache");
        response.setDateHeader("Expires", 0);
        //生成验证码，将结果放入redis
        ArithmeticCaptcha arithmeticCaptcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, arithmeticCaptcha.text(), 300, TimeUnit.SECONDS);
        try {
            arithmeticCaptcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
         }
    }
}
