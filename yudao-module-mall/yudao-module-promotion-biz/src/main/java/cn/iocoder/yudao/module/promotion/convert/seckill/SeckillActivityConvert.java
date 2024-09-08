package cn.iocoder.yudao.module.promotion.convert.seckill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.seckill.dto.SeckillValidateJoinRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.activity.SeckillActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductBaseVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.product.SeckillProductRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityNowRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillConfigDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

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

    PageResult<SeckillActivityRespVO> convertPage(PageResult<SeckillActivityDO> page);

    default PageResult<SeckillActivityRespVO> convertPage(PageResult<SeckillActivityDO> page,
                                                          List<SeckillProductDO> seckillProducts,
                                                          List<ProductSpuRespDTO> spuList) {
        PageResult<SeckillActivityRespVO> pageResult = convertPage(page);
        // 拼接商品
        Map<Long, ProductSpuRespDTO> spuMap = CollectionUtils.convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<SeckillProductDO>> productMap = convertMultiMap(seckillProducts, SeckillProductDO::getActivityId);
        pageResult.getList().forEach(activity -> {
            activity.setProducts(convertList2(productMap.get(activity.getId())));
            MapUtils.findAndThen(spuMap, activity.getSpuId(),
                    spu -> activity.setSpuName(spu.getName()).setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
        });
        return pageResult;
    }

    SeckillActivityDetailRespVO convert1(SeckillActivityDO activity);

    default SeckillActivityDetailRespVO convert(SeckillActivityDO activity, List<SeckillProductDO> products) {
        return convert1(activity).setProducts(convertList2(products));
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

    default List<SeckillProductDO> convertList(List<? extends SeckillProductBaseVO> products, SeckillActivityDO activity) {
        return CollectionUtils.convertList(products, item -> convert(activity, item).setActivityStatus(activity.getStatus()));
    }

    default List<SeckillActivityRespVO> convertList(List<SeckillActivityDO> list,
                                                        List<SeckillProductDO> productList,
                                                        List<ProductSpuRespDTO> spuList) {
        List<SeckillActivityRespVO> activityList = BeanUtils.toBean(list, SeckillActivityRespVO.class);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<SeckillProductDO>> productMap = convertMultiMap(productList, SeckillProductDO::getActivityId);
        return CollectionUtils.convertList(activityList, item -> {
            // 设置 product 信息
            item.setSeckillPrice(getMinValue(productMap.get(item.getId()), SeckillProductDO::getSeckillPrice));
            // 设置 SPU 信息
            findAndThen(spuMap, item.getSpuId(), spu -> item.setSpuName(spu.getName())
                    .setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
            return item;
        });
    }

    List<SeckillProductRespVO> convertList2(List<SeckillProductDO> list);

    List<AppSeckillActivityRespVO> convertList3(List<SeckillActivityDO> activityList);

    default AppSeckillActivityNowRespVO convert(SeckillConfigDO filteredConfig, List<SeckillActivityDO> activityList,
                                                List<SeckillProductDO> productList, List<ProductSpuRespDTO> spuList) {
        AppSeckillActivityNowRespVO respVO = new AppSeckillActivityNowRespVO();
        respVO.setConfig(SeckillConfigConvert.INSTANCE.convert1(filteredConfig));
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<SeckillProductDO>> productMap = convertMultiMap(productList, SeckillProductDO::getActivityId);
        respVO.setActivities(CollectionUtils.convertList(convertList3(activityList), item -> {
            // product 信息
            item.setSeckillPrice(getMinValue(productMap.get(item.getId()), SeckillProductDO::getSeckillPrice));
            // spu 信息
            findAndThen(spuMap, item.getSpuId(), spu ->
                    item.setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
            return item;
        }));
        return respVO;
    }

    PageResult<AppSeckillActivityRespVO> convertPage1(PageResult<SeckillActivityDO> pageResult);

    default PageResult<AppSeckillActivityRespVO> convertPage02(PageResult<SeckillActivityDO> pageResult, List<SeckillProductDO> productList, List<ProductSpuRespDTO> spuList) {
        PageResult<AppSeckillActivityRespVO> result = convertPage1(pageResult);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        Map<Long, List<SeckillProductDO>> productMap = convertMultiMap(productList, SeckillProductDO::getActivityId);
        List<AppSeckillActivityRespVO> list = CollectionUtils.convertList(result.getList(), item -> {
            // product 信息
            item.setSeckillPrice(getMinValue(productMap.get(item.getId()), SeckillProductDO::getSeckillPrice));
            // spu 信息
            findAndThen(spuMap, item.getSpuId(), spu -> item.setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
            return item;
        });
        result.setList(list);
        return result;
    }

    AppSeckillActivityDetailRespVO convert2(SeckillActivityDO seckillActivity);

    List<AppSeckillActivityDetailRespVO.Product> convertList1(List<SeckillProductDO> products);

    default AppSeckillActivityDetailRespVO convert3(SeckillActivityDO activity, List<SeckillProductDO> products,
                                                    LocalDateTime startTime, LocalDateTime endTime) {
        return convert2(activity)
                .setProducts(convertList1(products))
                .setStartTime(startTime).setEndTime(endTime);
    }

    SeckillValidateJoinRespDTO convert02(SeckillActivityDO activity, SeckillProductDO product);

}
