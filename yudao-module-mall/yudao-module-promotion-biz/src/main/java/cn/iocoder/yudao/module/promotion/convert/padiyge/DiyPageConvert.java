package cn.iocoder.yudao.module.promotion.convert.padiyge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.padiyge.vo.DiyPageCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.padiyge.vo.DiyPageRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.padiyge.vo.DiyPageUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.padiyge.DiyPageDO;
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

}
