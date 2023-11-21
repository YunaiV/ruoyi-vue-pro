package cn.iocoder.yudao.module.crm.convert.clue;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;

/**
 * 线索 Convert
 *
 * @author Wanwan
 */
@Mapper
public interface CrmClueConvert {

    CrmClueConvert INSTANCE = Mappers.getMapper(CrmClueConvert.class);

    CrmClueDO convert(CrmClueCreateReqVO bean);

    CrmClueDO convert(CrmClueUpdateReqVO bean);

    CrmClueRespVO convert(CrmClueDO bean);

    PageResult<CrmClueRespVO> convertPage(PageResult<CrmClueDO> page);

    List<CrmClueExcelVO> convertList02(List<CrmClueDO> list);

}
