package cn.iocoder.yudao.module.promotion.convert.bargain;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.dict.core.util.DictFrameworkUtils;
import cn.iocoder.yudao.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.iocoder.yudao.module.product.enums.DictTypeConstants;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityBaseVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.BargainActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.activity.AppBargainActivityDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.activity.AppBargainActivityRespVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

/**
 * 拼团活动 Convert
 *
 * @author HUIHUI
 */
@Mapper
public interface BargainActivityConvert {

    BargainActivityConvert INSTANCE = Mappers.getMapper(BargainActivityConvert.class);

    BargainActivityDO convert(BargainActivityBaseVO bean);

    BargainActivityDO convert(BargainActivityUpdateReqVO bean);

    BargainActivityRespVO convert(BargainActivityDO bean);

    List<BargainActivityRespVO> convertList(List<BargainActivityDO> list);

    PageResult<BargainActivityRespVO> convertPage(PageResult<BargainActivityDO> page);

    default PageResult<BargainActivityRespVO> convertPage(PageResult<BargainActivityDO> page, List<ProductSpuRespDTO> spuList) {
        PageResult<BargainActivityRespVO> result = convertPage(page);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        List<BargainActivityRespVO> list = CollectionUtils.convertList(result.getList(), item -> {
            findAndThen(spuMap, item.getSpuId(), spu -> {
                item.setPicUrl(spu.getPicUrl());
                item.setSpuName(spu.getName());
            });
            return item;
        });
        result.setList(list);
        return result;
    }

    AppBargainActivityDetailRespVO convert1(BargainActivityDO bean);

    default AppBargainActivityDetailRespVO convert1(BargainActivityDO bean, ProductSpuRespDTO spu) {
        AppBargainActivityDetailRespVO detail = convert1(bean);
        if (spu != null) {
            detail.setPicUrl(spu.getPicUrl());
            detail.setMarketPrice(spu.getMarketPrice());
            detail.setUnitName(DictFrameworkUtils.getDictDataLabel(DictTypeConstants.PRODUCT_UNIT, spu.getUnit()));
        }
        return detail;
    }

    PageResult<AppBargainActivityRespVO> convertAppPage(PageResult<BargainActivityDO> page);

    default PageResult<AppBargainActivityRespVO> convertAppPage(PageResult<BargainActivityDO> page, List<ProductSpuRespDTO> spuList) {
        PageResult<AppBargainActivityRespVO> result = convertAppPage(page);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        List<AppBargainActivityRespVO> list = CollectionUtils.convertList(result.getList(), item -> {
            findAndThen(spuMap, item.getSpuId(), spu -> {
                item.setPicUrl(spu.getPicUrl());
                item.setMarketPrice(spu.getMarketPrice());
            });
            return item;
        });
        result.setList(list);
        return result;
    }

    List<AppBargainActivityRespVO> convertAppList(List<BargainActivityDO> list);

    default List<AppBargainActivityRespVO> convertAppList(List<BargainActivityDO> list, List<ProductSpuRespDTO> spuList) {
        List<AppBargainActivityRespVO> activityList = convertAppList(list);
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spuList, ProductSpuRespDTO::getId);
        return CollectionUtils.convertList(activityList, item -> {
            findAndThen(spuMap, item.getSpuId(), spu -> {
                item.setPicUrl(spu.getPicUrl());
                item.setMarketPrice(spu.getMarketPrice());
            });
            return item;
        });
    }

}
