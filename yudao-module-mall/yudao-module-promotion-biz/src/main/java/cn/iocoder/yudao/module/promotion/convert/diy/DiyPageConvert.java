package cn.iocoder.yudao.module.promotion.convert.diy;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page.DiyPageCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page.DiyPageRespVO;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page.DiyPageUpdateReqVO;
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

}
