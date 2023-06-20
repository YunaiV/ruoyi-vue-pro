package cn.iocoder.yudao.module.promotion.convert.seckill.seckillactivity;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductCreateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillProductDO;
import org.mapstruct.Mapper;
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

    SeckillProductDO convert(SeckillProductCreateReqVO product);

    SeckillActivityDO convert(SeckillActivityCreateReqVO bean);

    SeckillActivityDO convert(SeckillActivityUpdateReqVO bean);

    SeckillActivityRespVO convert(SeckillActivityDO bean);

    List<SeckillActivityRespVO> convertList(List<SeckillActivityDO> list);

    PageResult<SeckillActivityRespVO> convertPage(PageResult<SeckillActivityDO> page);

    SeckillActivityDetailRespVO convert(SeckillActivityDO seckillActivity, List<SeckillProductDO> seckillProducts);


    /**
     * 比较两个秒杀商品对象是否相等
     *
     * @param productDO 数据库中的商品
     * @param productVO 前端传入的商品
     * @return 是否匹配
     */
    default boolean isEquals(SeckillProductDO productDO, SeckillProductCreateReqVO productVO) {
        return ObjectUtil.equals(productDO.getSpuId(), productVO.getSpuId())
                && ObjectUtil.equals(productDO.getSkuId(), productVO.getSkuId())
                && ObjectUtil.equals(productDO.getSeckillPrice(), productVO.getSeckillPrice());
        //&& ObjectUtil.equals(productDO.getQuota(), productVO.getQuota())
        //&& ObjectUtil.equals(productDO.getLimitCount(), productVO.getLimitCount());
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
                && ObjectUtil.equals(productDO.getSeckillPrice(), productVO.getSeckillPrice());
                //&& ObjectUtil.equals(productDO.getQuota(), productVO.getQuota())
                //&& ObjectUtil.equals(productDO.getLimitCount(), productVO.getLimitCount());

    }

}
