package cn.iocoder.yudao.module.crm.convert.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionPageReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 商机 Convert
 *
 * @author ljlleo
 */
@Mapper
public interface CrmBusinessConvert {

    CrmBusinessConvert INSTANCE = Mappers.getMapper(CrmBusinessConvert.class);

    CrmBusinessDO convert(CrmBusinessCreateReqVO bean);

    CrmBusinessDO convert(CrmBusinessUpdateReqVO bean);

    CrmBusinessRespVO convert(CrmBusinessDO bean);

    PageResult<CrmBusinessRespVO> convertPage(PageResult<CrmBusinessDO> page);

    List<CrmBusinessExcelVO> convertList02(List<CrmBusinessDO> list);

    @Mappings({
            @Mapping(target = "bizId", source = "reqVO.id"),
            @Mapping(target = "newOwnerUserId", source = "reqVO.id")
    })
    CrmPermissionTransferReqBO convert(CrmBusinessTransferReqVO reqVO, Long userId);

    CrmPermissionPageReqBO convert(CrmBusinessPageReqVO pageReqVO);

}
