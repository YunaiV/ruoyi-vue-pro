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

    default BargainActivityRespVO convert(BargainActivityDO activity,
                                          List<BargainProductDO> products) {
        return convert(activity).setProducts(convertList2(products));
    }

    List<BargainActivityRespVO> convertList(List<BargainActivityDO> list);

    PageResult<BargainActivityRespVO> convertPage(PageResult<BargainActivityDO> page);

    default PageResult<BargainActivityRespVO> convertPage(PageResult<BargainActivityDO> page,
                                                          List<BargainProductDO> productList,
                                                          List<ProductSpuRespDTO> spuList) {
        PageResult<BargainActivityRespVO> pageResult = convertPage(page);
        // 拼接商品
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        pageResult.getList().forEach(item -> {
            item.setProducts(convertList2(productList));
            MapUtils.findAndThen(spuMap, item.getSpuId(),
                    spu -> item.setSpuName(spu.getName()).setPicUrl(spu.getPicUrl()));
        });
        return pageResult;
    }

    List<BargainProductRespVO> convertList2(List<BargainProductDO> productDOs);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "activityId", source = "activity.id"),
            @Mapping(target = "spuId", source = "activity.spuId"),
            @Mapping(target = "skuId", source = "product.skuId"),
            @Mapping(target = "bargainFirstPrice", source = "product.bargainFirstPrice"),
            @Mapping(target = "bargainPrice", source = "product.bargainPrice"),
            @Mapping(target = "stock", source = "product.stock"),
            @Mapping(target = "activityStartTime", source = "activity.startTime"),
            @Mapping(target = "activityEndTime", source = "activity.endTime")
    })
    BargainProductDO convert(BargainActivityDO activity, BargainProductBaseVO product);

    default List<BargainProductDO> convertList(List<? extends BargainProductBaseVO> products, BargainActivityDO activityDO) {
        return CollectionUtils.convertList(products, item -> convert(activityDO, item).setActivityStatus(activityDO.getStatus()));
    }

    default List<BargainProductDO> convertList(List<BargainProductBaseVO> updateProductVOs,
                                               List<BargainProductDO> products, BargainActivityDO activity) {
        Map<Long, Long> productMap = convertMap(products, BargainProductDO::getSkuId, BargainProductDO::getId);
        return CollectionUtils.convertList(updateProductVOs, updateProductVO -> convert(activity, updateProductVO)
                .setId(productMap.get(updateProductVO.getSkuId()))
                .setActivityStatus(activity.getStatus()));
    }

}
