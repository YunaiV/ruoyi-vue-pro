package cn.iocoder.yudao.module.promotion.convert.combination;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityExcelVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.activity.CombinationActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.product.CombinationProductCreateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.combinationactivity.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.combinationactivity.CombinationProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

/**
 * 拼团活动 Convert
 *
 * @author HUIHUI
 */
@Mapper
public interface CombinationActivityConvert {

    CombinationActivityConvert INSTANCE = Mappers.getMapper(CombinationActivityConvert.class);

    @Mapping(target = "startTime", expression = "java(bean.getActivityTime()[0])")
    @Mapping(target = "endTime", expression = "java(bean.getActivityTime()[1])")
    CombinationActivityDO convert(CombinationActivityCreateReqVO bean);

    CombinationActivityDO convert(CombinationActivityUpdateReqVO bean);

    CombinationActivityRespVO convert(CombinationActivityDO bean);

    List<CombinationActivityRespVO> convertList(List<CombinationActivityDO> list);

    PageResult<CombinationActivityRespVO> convertPage(PageResult<CombinationActivityDO> page);

    List<CombinationActivityExcelVO> convertList02(List<CombinationActivityDO> list);

    default List<CombinationProductDO> convertList(CombinationActivityDO activityDO, List<CombinationProductCreateReqVO> products) {
        ArrayList<CombinationProductDO> productDOs = new ArrayList<>();
        products.forEach(item -> {
            CombinationProductDO productDO = new CombinationProductDO();
            productDO.setActivityId(activityDO.getId());
            productDO.setSpuId(item.getSpuId());
            productDO.setSkuId(item.getSkuId());
            productDO.setActivityStatus(0); // TODO 拼团状态枚举未定义不确定有些什么状态
            productDO.setActivityStartTime(activityDO.getStartTime());
            productDO.setActivityEndTime(activityDO.getEndTime());
            productDO.setActivePrice(item.getActivePrice());
            productDOs.add(productDO);
        });
        return productDOs;
    }
}
