package cn.iocoder.yudao.module.crm.convert.businessstatus;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.businessstatus.CrmBusinessStatusDO;

/**
 * 商机状态 Convert
 *
 * @author ljlleo
 */
@Mapper
public interface CrmBusinessStatusConvert {

    CrmBusinessStatusConvert INSTANCE = Mappers.getMapper(CrmBusinessStatusConvert.class);

    CrmBusinessStatusDO convert(CrmBusinessStatusCreateReqVO bean);

    CrmBusinessStatusDO convert(CrmBusinessStatusUpdateReqVO bean);

    CrmBusinessStatusRespVO convert(CrmBusinessStatusDO bean);

    List<CrmBusinessStatusRespVO> convertList(List<CrmBusinessStatusDO> list);

    PageResult<CrmBusinessStatusRespVO> convertPage(PageResult<CrmBusinessStatusDO> page);

    List<CrmBusinessStatusExcelVO> convertList02(List<CrmBusinessStatusDO> list);

}
