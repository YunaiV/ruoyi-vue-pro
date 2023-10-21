package cn.iocoder.yudao.module.crm.convert.receivable;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivablePlanDO;

/**
 * 回款计划 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ReceivablePlanConvert {

    ReceivablePlanConvert INSTANCE = Mappers.getMapper(ReceivablePlanConvert.class);

    ReceivablePlanDO convert(ReceivablePlanCreateReqVO bean);

    ReceivablePlanDO convert(ReceivablePlanUpdateReqVO bean);

    ReceivablePlanRespVO convert(ReceivablePlanDO bean);

    List<ReceivablePlanRespVO> convertList(List<ReceivablePlanDO> list);

    PageResult<ReceivablePlanRespVO> convertPage(PageResult<ReceivablePlanDO> page);

    List<ReceivablePlanExcelVO> convertList02(List<ReceivablePlanDO> list);

}
