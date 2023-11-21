package cn.iocoder.yudao.module.crm.convert.receivable;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;

/**
 * 回款计划 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmReceivablePlanConvert {

    CrmReceivablePlanConvert INSTANCE = Mappers.getMapper(CrmReceivablePlanConvert.class);

    CrmReceivablePlanDO convert(CrmReceivablePlanCreateReqVO bean);

    CrmReceivablePlanDO convert(CrmReceivablePlanUpdateReqVO bean);

    CrmReceivablePlanRespVO convert(CrmReceivablePlanDO bean);

    List<CrmReceivablePlanRespVO> convertList(List<CrmReceivablePlanDO> list);

    PageResult<CrmReceivablePlanRespVO> convertPage(PageResult<CrmReceivablePlanDO> page);

    List<CrmReceivablePlanExcelVO> convertList02(List<CrmReceivablePlanDO> list);

}
