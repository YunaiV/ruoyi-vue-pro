package cn.iocoder.yudao.module.promotion.convert.seckill.seckillactivity;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductBaseVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckillactivity.SeckillProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 秒杀活动 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface SeckillActivityConvert {

    SeckillActivityConvert INSTANCE = Mappers.getMapper(SeckillActivityConvert.class);

    SeckillActivityDO convert(SeckillActivityCreateReqVO bean);

    SeckillActivityDO convert(SeckillActivityUpdateReqVO bean);

    SeckillActivityRespVO convert(SeckillActivityDO bean);

    List<SeckillActivityRespVO> convertList(List<SeckillActivityDO> list);

    default PageResult<SeckillActivityRespVO> convertPage(PageResult<SeckillActivityDO> page,
                                                          List<SeckillProductDO> seckillProducts,
                                                          List<ProductSpuRespDTO> spuList) {
        PageResult<SeckillActivityRespVO> pageResult = convertPage(page);
        // 拼接商品
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        pageResult.getList().forEach(item -> {
            item.setProducts(convertList2(seckillProducts));
            MapUtils.findAndThen(spuMap, item.getSpuId(),
                    spu -> item.setSpuName(spu.getName()).setPicUrl(spu.getPicUrl()));
        });
        return pageResult;
    }
    PageResult<SeckillActivityRespVO> convertPage(PageResult<SeckillActivityDO> page);

    SeckillActivityDetailRespVO convert1(SeckillActivityDO seckillActivity);

    default SeckillActivityDetailRespVO convert(SeckillActivityDO activity,
                                                List<SeckillProductDO> products) {
        return convert1(activity).setProducts(convertList2(products));
    }
    List<SeckillProductRespVO> convertList2(List<SeckillProductDO> productDOs);

    default List<SeckillProductDO> convertList(List<? extends SeckillProductBaseVO> products, SeckillActivityDO activityDO) {
        return CollectionUtils.convertList(products, item -> convert(activityDO, item).setActivityStatus(activityDO.getStatus()));
    }
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "activityId", source = "activity.id"),
            @Mapping(target = "configIds", source = "activity.configIds"),
            @Mapping(target = "spuId", source = "activity.spuId"),
            @Mapping(target = "skuId", source = "product.skuId"),
            @Mapping(target = "seckillPrice", source = "product.seckillPrice"),
            @Mapping(target = "stock", source = "product.stock"),
            @Mapping(target = "activityStartTime", source = "activity.startTime"),
            @Mapping(target = "activityEndTime", source = "activity.endTime")
    })
    SeckillProductDO convert(SeckillActivityDO activity, SeckillProductBaseVO product);

}
