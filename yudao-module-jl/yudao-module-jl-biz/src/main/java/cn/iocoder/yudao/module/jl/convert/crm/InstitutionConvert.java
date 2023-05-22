package cn.iocoder.yudao.module.jl.convert.crm;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.InstitutionDO;

/**
 * CRM 模块的机构/公司 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface InstitutionConvert {

    InstitutionConvert INSTANCE = Mappers.getMapper(InstitutionConvert.class);

    InstitutionDO convert(InstitutionCreateReqVO bean);

    InstitutionDO convert(InstitutionUpdateReqVO bean);

    InstitutionRespVO convert(InstitutionDO bean);

    List<InstitutionRespVO> convertList(List<InstitutionDO> list);

    PageResult<InstitutionRespVO> convertPage(PageResult<InstitutionDO> page);

    List<InstitutionExcelVO> convertList02(List<InstitutionDO> list);

}
