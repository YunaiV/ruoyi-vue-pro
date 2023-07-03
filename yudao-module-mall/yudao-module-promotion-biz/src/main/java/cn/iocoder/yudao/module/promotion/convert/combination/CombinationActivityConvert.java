package cn.iocoder.yudao.module.promotion.convert.combination;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.CombinationActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.CombinationActivityExcelVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.CombinationActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.CombinationActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 拼团活动 Convert
 *
 * @author HUIHUI
 */
@Mapper
public interface CombinationActivityConvert {

    CombinationActivityConvert INSTANCE = Mappers.getMapper(CombinationActivityConvert.class);

    CombinationActivityDO convert(CombinationActivityCreateReqVO bean);

    CombinationActivityDO convert(CombinationActivityUpdateReqVO bean);

    CombinationActivityRespVO convert(CombinationActivityDO bean);

    List<CombinationActivityRespVO> convertList(List<CombinationActivityDO> list);

    PageResult<CombinationActivityRespVO> convertPage(PageResult<CombinationActivityDO> page);

    List<CombinationActivityExcelVO> convertList02(List<CombinationActivityDO> list);

}
