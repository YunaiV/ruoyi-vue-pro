package cn.iocoder.yudao.module.crm.convert.businessstatustype;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.businessstatustype.CrmBusinessStatusTypeDO;

/**
 * 商机状态类型 Convert
 *
 * @author ljlleo
 */
@Mapper
public interface CrmBusinessStatusTypeConvert {

    CrmBusinessStatusTypeConvert INSTANCE = Mappers.getMapper(CrmBusinessStatusTypeConvert.class);

    CrmBusinessStatusTypeDO convert(CrmBusinessStatusTypeCreateReqVO bean);

    CrmBusinessStatusTypeDO convert(CrmBusinessStatusTypeUpdateReqVO bean);

    CrmBusinessStatusTypeRespVO convert(CrmBusinessStatusTypeDO bean);

    List<CrmBusinessStatusTypeRespVO> convertList(List<CrmBusinessStatusTypeDO> list);

    PageResult<CrmBusinessStatusTypeRespVO> convertPage(PageResult<CrmBusinessStatusTypeDO> page);

    List<CrmBusinessStatusTypeExcelVO> convertList02(List<CrmBusinessStatusTypeDO> list);

}
