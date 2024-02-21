package cn.iocoder.yudao.module.crm.convert.business;

import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessTransferReqVO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 商机 Convert
 *
 * @author ljlleo
 */
@Mapper
public interface CrmBusinessConvert {

    CrmBusinessConvert INSTANCE = Mappers.getMapper(CrmBusinessConvert.class);

    @Mapping(target = "bizId", source = "reqVO.id")
    CrmPermissionTransferReqBO convert(CrmBusinessTransferReqVO reqVO, Long userId);

}
