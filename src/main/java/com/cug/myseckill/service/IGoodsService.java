package com.cug.myseckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cug.myseckill.pojo.Goods;
import com.cug.myseckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yzx
 * @since 2023-05-28
 */
public interface IGoodsService extends IService<Goods> {

    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
