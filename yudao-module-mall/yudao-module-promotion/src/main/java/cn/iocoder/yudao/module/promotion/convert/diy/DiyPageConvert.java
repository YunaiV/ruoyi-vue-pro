package cn.iocoder.yudao.module.promotion.convert.diy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page.*;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyPageDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 装修页面 Convert
 *
 * @author owen
 */
@Mapper
public interface DiyPageConvert {

    DiyPageConvert INSTANCE = Mappers.getMapper(DiyPageConvert.class);

    DiyPageDO convert(DiyPageCreateReqVO bean);

    DiyPageDO convert(DiyPageUpdateReqVO bean);

    DiyPageRespVO convert(DiyPageDO bean);

    List<DiyPageRespVO> convertList(List<DiyPageDO> list);

    PageResult<DiyPageRespVO> convertPage(PageResult<DiyPageDO> page);

    DiyPageCreateReqVO convertCreateVo(Long templateId, String name, String remark);

    DiyPagePropertyRespVO convertPropertyVo(DiyPageDO diyPage);

    DiyPageDO convert(DiyPagePropertyUpdateRequestVO updateReqVO);

}
