package cn.iocoder.yudao.module.crm.convert.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 客户 Convert
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerConvert {

    CrmCustomerConvert INSTANCE = Mappers.getMapper(CrmCustomerConvert.class);

    CrmCustomerDO convert(CrmCustomerCreateReqVO bean);

    CrmCustomerDO convert(CrmCustomerUpdateReqVO bean);

    CrmCustomerRespVO convert(CrmCustomerDO bean);

    PageResult<CrmCustomerRespVO> convertPage(PageResult<CrmCustomerDO> page);

    List<CrmCustomerExcelVO> convertList02(List<CrmCustomerDO> list);

    @Mappings({
            @Mapping(target = "bizId", source = "reqVO.id"),
            @Mapping(target = "newOwnerUserId", source = "reqVO.id")
    })
    CrmPermissionTransferReqBO convert(CrmCustomerTransferReqVO reqVO, Long userId);

}
