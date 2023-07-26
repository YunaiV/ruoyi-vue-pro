package cn.iocoder.yudao.module.promotion.convert.combination;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.api.combination.dto.CombinationRecordReqDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityExcelVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductBaseVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.combinationactivity.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.combinationactivity.CombinationProductDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.combinationactivity.CombinationRecordDO;
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
        // TODO @puhui999：c -> c 可以去掉哈
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId, c -> c);
        PageResult<CombinationActivityRespVO> pageResult = convertPage(page);
        pageResult.getList().forEach(item -> {
            // TODO @puhui999：最好 MapUtils.findAndThen，万一没找到呢，啊哈哈。
            item.setSpuName(spuMap.get(item.getSpuId()).getName());
            item.setPicUrl(spuMap.get(item.getSpuId()).getPicUrl());
            item.setProducts(convertList2(productList));
        });
        return pageResult;
    }

    List<CombinationProductRespVO> convertList2(List<CombinationProductDO> productDOs);

    List<CombinationActivityExcelVO> convertList02(List<CombinationActivityDO> list);

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

    default List<CombinationProductDO> convertList(CombinationActivityDO activityDO, List<? extends CombinationProductBaseVO> products) {
        List<CombinationProductDO> list = new ArrayList<>();
        products.forEach(sku -> {
            CombinationProductDO productDO = convert(activityDO, sku);
            // TODO 状态设置
            productDO.setActivityStatus(CommonStatusEnum.ENABLE.getStatus());
            list.add(productDO);
        });
        return list;
    }

    // TODO @puhui999：这个方法的参数，调整成 productDOs、vos、activityDO；因为 productDOs 是主角；
    // 然后，这个方法，感觉不是为了 convert，而是为了补全；
    default List<CombinationProductDO> convertList1(CombinationActivityDO activityDO,
                                                    List<CombinationProductUpdateReqVO> vos,
                                                    List<CombinationProductDO> productDOs) {
        Map<Long, Long> longMap = convertMap(productDOs, CombinationProductDO::getSkuId, CombinationProductDO::getId);
        List<CombinationProductDO> list = new ArrayList<>();
        vos.forEach(sku -> {
            CombinationProductDO productDO = convert(activityDO, sku);
            productDO.setId(longMap.get(sku.getSkuId()));
            // TODO @puhui999：是是不是用 activityDO 的状态；
            productDO.setActivityStatus(CommonStatusEnum.ENABLE.getStatus());
            list.add(productDO);
        });
        return list;
    }

    CombinationRecordDO convert(CombinationRecordReqDTO reqDTO);

}
