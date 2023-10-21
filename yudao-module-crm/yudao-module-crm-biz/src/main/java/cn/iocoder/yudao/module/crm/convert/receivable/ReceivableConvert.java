package cn.iocoder.yudao.module.crm.convert.receivable;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivableDO;

/**
 * 回款管理 Convert
 *
 * @author 赤焰
 */
@Mapper
public interface ReceivableConvert {

    ReceivableConvert INSTANCE = Mappers.getMapper(ReceivableConvert.class);

    ReceivableDO convert(ReceivableCreateReqVO bean);

    ReceivableDO convert(ReceivableUpdateReqVO bean);

    ReceivableRespVO convert(ReceivableDO bean);

    List<ReceivableRespVO> convertList(List<ReceivableDO> list);

    PageResult<ReceivableRespVO> convertPage(PageResult<ReceivableDO> page);

    List<ReceivableExcelVO> convertList02(List<ReceivableDO> list);

}
