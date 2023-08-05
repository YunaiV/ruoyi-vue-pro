package cn.iocoder.yudao.module.promotion.convert.combination;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordCreateReqDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductBaseVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationProductDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationRecordDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 拼团活动 Convert
 *
 * @author HUIHUI
 */
@Mapper
public interface CombinationActivityConvert {

    CombinationActivityConvert INSTANCE = Mappers.getMapper(CombinationActivityConvert.class);

    @Mappings({
            @Mapping(target = "startTime", expression = "java(bean.getActivityTime()[0])"),
            @Mapping(target = "endTime", expression = "java(bean.getActivityTime()[1])")
    })
    CombinationActivityDO convert(CombinationActivityCreateReqVO bean);

    @Mappings({
            @Mapping(target = "startTime", expression = "java(bean.getActivityTime()[0])"),
            @Mapping(target = "endTime", expression = "java(bean.getActivityTime()[1])")
    })
    CombinationActivityDO convert(CombinationActivityUpdateReqVO bean);

    @Named("mergeTime")
    default LocalDateTime[] mergeTime(LocalDateTime startTime, LocalDateTime endTime) {
        // TODO 有点怪第一次这样写 hh
        LocalDateTime[] localDateTime = new LocalDateTime[2];
        localDateTime[0] = startTime;
        localDateTime[1] = endTime;
        return localDateTime;
    }

    @Mappings({
            @Mapping(target = "activityTime", expression = "java(mergeTime(bean.getStartTime(),bean.getEndTime()))")
    })
    CombinationActivityRespVO convert(CombinationActivityDO bean);

    CombinationProductRespVO convert(CombinationProductDO bean);

    default CombinationActivityRespVO convert(CombinationActivityDO bean, List<CombinationProductDO> productDOs) {
        CombinationActivityRespVO respVO = convert(bean);
        respVO.setProducts(convertList2(productDOs));
        return respVO;
    }

    List<CombinationActivityRespVO> convertList(List<CombinationActivityDO> list);

    PageResult<CombinationActivityRespVO> convertPage(PageResult<CombinationActivityDO> page);

    default PageResult<CombinationActivityRespVO> convertPage(PageResult<CombinationActivityDO> page,
                                                              List<CombinationProductDO> productList,
                                                              List<ProductSpuRespDTO> spuList) {
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        PageResult<CombinationActivityRespVO> pageResult = convertPage(page);
        pageResult.getList().forEach(item -> {
            MapUtils.findAndThen(spuMap, item.getSpuId(), spu -> {
                item.setSpuName(spu.getName());
                item.setPicUrl(spu.getPicUrl());
            });
            item.setProducts(convertList2(productList));
        });
        return pageResult;
    }

    List<CombinationProductRespVO> convertList2(List<CombinationProductDO> productDOs);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "activityId", source = "activityDO.id"),
            @Mapping(target = "spuId", source = "activityDO.spuId"),
            @Mapping(target = "skuId", source = "vo.skuId"),
            @Mapping(target = "activePrice", source = "vo.activePrice"),
            @Mapping(target = "activityStartTime", source = "activityDO.startTime"),
            @Mapping(target = "activityEndTime", source = "activityDO.endTime")
    })
    CombinationProductDO convert(CombinationActivityDO activityDO, CombinationProductBaseVO vo);

    default List<CombinationProductDO> convertList(List<? extends CombinationProductBaseVO> products, CombinationActivityDO activityDO) {
        List<CombinationProductDO> list = new ArrayList<>();
        products.forEach(sku -> {
            CombinationProductDO productDO = convert(activityDO, sku);
            productDO.setActivityStatus(activityDO.getStatus());
            list.add(productDO);
        });
        return list;
    }

    default List<CombinationProductDO> convertList(List<CombinationProductUpdateReqVO> updateProductVOs,
                                                   List<CombinationProductDO> products, CombinationActivityDO activity) {
        Map<Long, Long> productMap = convertMap(products, CombinationProductDO::getSkuId, CombinationProductDO::getId);
        return CollectionUtils.convertList(updateProductVOs, updateProductVO -> convert(activity, updateProductVO)
                        .setId(productMap.get(updateProductVO.getSkuId()))
                        .setActivityStatus(activity.getStatus()));
    }

    CombinationRecordDO convert(CombinationRecordCreateReqDTO reqDTO);

}
