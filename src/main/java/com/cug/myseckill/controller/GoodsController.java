package com.cug.myseckill.controller;

import com.cug.myseckill.pojo.User;
import com.cug.myseckill.service.IGoodsService;
import com.cug.myseckill.service.IUserService;
import com.cug.myseckill.vo.DetailVo;
import com.cug.myseckill.vo.GoodsVo;
import com.cug.myseckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@CrossOrigin
@Controller
@RequestMapping("/goods")  //名字打错了！！！
public class GoodsController {

    @Autowired
    IUserService userService;

    @Autowired
    IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /**
     * 跳转商品列表
     * windows压测(5000*10优化前4000qps)
     * @param model
     * @return
     */
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody  //不能加这个因为是get接口,加了这个就会把整个页面返回去
    public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
//        if (StringUtils.isEmpty(ticket)) {
//            return "login";
//        }
////        User user = (User) session.getAttribute(ticket);  //验证前端传的session用户信息正确，这里可以改成从redis获取
//        User user = userService.getUserByCookie(ticket, request, response);
//        if (null == user) {
//            return "login";
//        }
        model.addAttribute("user", user);  //不懂什么意思
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        //        return "goodsList";  //字符串必须同名，好像是因为

        //页面缓存
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html;
    }

    /**
     * 跳转商品详情页
     * @return
     */
//    @RequestMapping(value = "/toDetail/{goodsId}", method = RequestMethod.GET)  //又是名字打错了!!!!
//    @ResponseBody
//    //通过 @PathVariable 可以将 URL 中占位符参数绑定到控制器处理方法的入参中:URL 中的 {xxx} 占位符可以通过
//    public RespBean toDetail(User user, @PathVariable Long goodsId) {
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//        Date startDate = goodsVo.getStartDate();
//        Date endDate = goodsVo.getEndDate();
//        Date currentDate = new Date(System.currentTimeMillis());
//        int secKillStatus = 1;
//        int remainSeconds = 0;
//        if (currentDate.before(startDate)) {
//            secKillStatus = 0;
//            remainSeconds = (int) ((startDate.getTime() - currentDate.getTime()) / 1000);
//        } else if (currentDate.after(endDate)) {
//            secKillStatus = 2;
//            remainSeconds = -1;
//        }
//        DetailVo detailVo = new DetailVo();
//        detailVo.setUser(user);
//        detailVo.setGoodsVo(goodsVo);
//        detailVo.setSecKillStatus(remainSeconds);
//        detailVo.setRemainSeconds(secKillStatus);
//
//        return RespBean.success(detailVo);
//    }

    /**
     * 跳转商品详情页
     * @return
     */
    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")  //又是名字打错了!!!!
    @ResponseBody
    //通过 @PathVariable 可以将 URL 中占位符参数绑定到控制器处理方法的入参中:URL 中的 {xxx} 占位符可以通过
    public String toDetail2(Model model, User user, @PathVariable Long goodsId, HttpServletRequest request, HttpServletResponse response) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date currentDate = new Date(System.currentTimeMillis());
        int secKillStatus = 1;
        int remainSeconds = 0;
        if (currentDate.before(startDate)) {
            secKillStatus = 0;
            remainSeconds = (int) ((startDate.getTime() - currentDate.getTime()) / 1000);
        } else if (currentDate.after(endDate)) {
            secKillStatus = 2;
            remainSeconds = -1;
        }
        model.addAttribute("secKillStatus", secKillStatus);
        //秒杀倒计时(应该放在前端，放在后端传输浪费时间)
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("goods", goodsVo);

        //页面缓存
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
        }
        return html;
    }
}
