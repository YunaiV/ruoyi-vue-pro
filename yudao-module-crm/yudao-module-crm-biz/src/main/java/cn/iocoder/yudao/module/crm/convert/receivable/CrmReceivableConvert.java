package cn.iocoder.yudao.module.crm.convert.receivable;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivableDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;

/**
 * 回款管理 Convert
 *
 * @author 赤焰
 */
@Mapper
public interface CrmReceivableConvert {

    CrmReceivableConvert INSTANCE = Mappers.getMapper(CrmReceivableConvert.class);

    CrmReceivableDO convert(CrmReceivableCreateReqVO bean);

    CrmReceivableDO convert(CrmReceivableUpdateReqVO bean);

    CrmReceivableRespVO convert(CrmReceivableDO bean);

    List<CrmReceivableRespVO> convertList(List<CrmReceivableDO> list);

    PageResult<CrmReceivableRespVO> convertPage(PageResult<CrmReceivableDO> page);

    List<CrmReceivableExcelVO> convertList02(List<CrmReceivableDO> list);

}
