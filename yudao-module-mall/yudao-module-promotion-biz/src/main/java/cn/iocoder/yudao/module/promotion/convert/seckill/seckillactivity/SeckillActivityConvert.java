package cn.iocoder.yudao.module.promotion.convert.seckill.seckillactivity;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.*;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 秒杀活动 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface SeckillActivityConvert {

    SeckillActivityConvert INSTANCE = Mappers.getMapper(SeckillActivityConvert.class);

    SeckillProductDO convert(SeckillActivityBaseVO.Product product);


    SeckillActivityDO convert(SeckillActivityCreateReqVO bean);

    default String map(Long[] value) {
        return value.toString();
    }

    SeckillActivityDO convert(SeckillActivityUpdateReqVO bean);

    SeckillActivityRespVO convert(SeckillActivityDO bean);

    List<SeckillActivityRespVO> convertList(List<SeckillActivityDO> list);

    PageResult<SeckillActivityRespVO> convertPage(PageResult<SeckillActivityDO> page);

    @Mappings({@Mapping(target = "products", source = "seckillProducts")})
    SeckillActivityDetailRespVO convert(SeckillActivityDO seckillActivity, List<SeckillProductDO> seckillProducts);


    /**
     * 比较两个秒杀商品对象是否相等
     *
     * @param productDO 数据库中的商品
     * @param productVO 前端传入的商品
     * @return 是否匹配
     */
    default boolean isEquals(SeckillProductDO productDO, SeckillActivityBaseVO.Product productVO) {
        return ObjectUtil.equals(productDO.getSpuId(), productVO.getSpuId())
                && ObjectUtil.equals(productDO.getSkuId(), productVO.getSkuId())
                && ObjectUtil.equals(productDO.getSeckillPrice(), productVO.getSeckillPrice())
                && ObjectUtil.equals(productDO.getStock(), productVO.getStock())
                && ObjectUtil.equals(productDO.getLimitBuyCount(), productVO.getLimitBuyCount());
    }

    /**
     * 比较两个秒杀商品对象是否相等
     *
     * @param productDO 商品1
     * @param productVO 商品2
     * @return 是否匹配
     */
    default boolean isEquals(SeckillProductDO productDO, SeckillProductDO productVO) {
        return ObjectUtil.equals(productDO.getSpuId(), productVO.getSpuId())
                && ObjectUtil.equals(productDO.getSkuId(), productVO.getSkuId())
                && ObjectUtil.equals(productDO.getSeckillPrice(), productVO.getSeckillPrice())
                && ObjectUtil.equals(productDO.getStock(), productVO.getStock())
                && ObjectUtil.equals(productDO.getLimitBuyCount(), productVO.getLimitBuyCount());

    }

    default List<SeckillProductDO> convertList(List<SeckillActivityBaseVO.Product> products, SeckillActivityDO seckillActivity) {
        return CollectionUtils.convertList(products, product -> convert(product)
                .setActivityId(seckillActivity.getId()).setTimeIds(seckillActivity.getTimeIds()));
    }

}
