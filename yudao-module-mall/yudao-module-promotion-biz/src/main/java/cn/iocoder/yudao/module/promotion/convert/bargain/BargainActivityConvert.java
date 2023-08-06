package cn.iocoder.yudao.module.promotion.convert.bargain;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.activity.BargainActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.product.BargainProductBaseVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.product.BargainProductRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.product.BargainProductUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 拼团活动 Convert
 *
 * @author HUIHUI
 */
@Mapper
public interface BargainActivityConvert {

    BargainActivityConvert INSTANCE = Mappers.getMapper(BargainActivityConvert.class);

    BargainActivityDO convert(BargainActivityCreateReqVO bean);

    BargainActivityDO convert(BargainActivityUpdateReqVO bean);

    BargainActivityRespVO convert(BargainActivityDO bean);

    BargainProductRespVO convert(BargainProductDO bean);

    default BargainActivityRespVO convert(BargainActivityDO bean, List<BargainProductDO> productDOs) {
        return convert(bean).setProducts(convertList2(productDOs));
    }

    List<BargainActivityRespVO> convertList(List<BargainActivityDO> list);

    PageResult<BargainActivityRespVO> convertPage(PageResult<BargainActivityDO> page);

    default PageResult<BargainActivityRespVO> convertPage(PageResult<BargainActivityDO> page,
                                                          List<BargainProductDO> productList,
                                                          List<ProductSpuRespDTO> spuList) {
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        PageResult<BargainActivityRespVO> pageResult = convertPage(page);
        pageResult.getList().forEach(item -> {
            MapUtils.findAndThen(spuMap, item.getSpuId(), spu -> {
                item.setSpuName(spu.getName());
                item.setPicUrl(spu.getPicUrl());
            });
            item.setProducts(convertList2(productList));
        });
        return pageResult;
    }

    List<BargainProductRespVO> convertList2(List<BargainProductDO> productDOs);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "activityId", source = "activityDO.id"),
            @Mapping(target = "spuId", source = "activityDO.spuId"),
            @Mapping(target = "skuId", source = "vo.skuId"),
            @Mapping(target = "bargainFirstPrice", source = "vo.bargainFirstPrice"),
            @Mapping(target = "bargainPrice", source = "vo.bargainPrice"),
            @Mapping(target = "stock", source = "vo.stock"),
            @Mapping(target = "activityStartTime", source = "activityDO.startTime"),
            @Mapping(target = "activityEndTime", source = "activityDO.endTime")
    })
    BargainProductDO convert(BargainActivityDO activityDO, BargainProductBaseVO vo);

    default List<BargainProductDO> convertList(List<? extends BargainProductBaseVO> products, BargainActivityDO activityDO) {
        return CollectionUtils.convertList(products, item -> convert(activityDO, item).setActivityStatus(activityDO.getStatus()));
    }

    default List<BargainProductDO> convertList(List<BargainProductUpdateReqVO> updateProductVOs,
                                               List<BargainProductDO> products, BargainActivityDO activity) {
        Map<Long, Long> productMap = convertMap(products, BargainProductDO::getSkuId, BargainProductDO::getId);
        return CollectionUtils.convertList(updateProductVOs, updateProductVO -> convert(activity, updateProductVO)
                .setId(productMap.get(updateProductVO.getSkuId()))
                .setActivityStatus(activity.getStatus()));
    }

    //BargainRecordDO convert(BargainRecordCreateReqDTO reqDTO);

}
