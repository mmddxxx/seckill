package com.cug.myseckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cug.myseckill.pojo.Goods;
import com.cug.myseckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yzx
 * @since 2023-05-28
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 获取商品列表
     * @return
     */
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
