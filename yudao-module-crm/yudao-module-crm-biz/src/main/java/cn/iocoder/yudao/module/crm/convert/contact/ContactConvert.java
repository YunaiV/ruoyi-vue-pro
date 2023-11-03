package cn.iocoder.yudao.module.crm.convert.contact;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;
import cn.iocoder.yudao.module.crm.service.permission.bo.TransferCrmPermissionBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * crm 联系人 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ContactConvert {

    ContactConvert INSTANCE = Mappers.getMapper(ContactConvert.class);

    ContactDO convert(ContactCreateReqVO bean);

    ContactDO convert(ContactUpdateReqVO bean);

    ContactRespVO convert(ContactDO bean);

    List<ContactRespVO> convertList(List<ContactDO> list);

    PageResult<ContactRespVO> convertPage(PageResult<ContactDO> page);

    List<ContactExcelVO> convertList02(List<ContactDO> list);

    @Mappings({
            @Mapping(target = "userId", source = "userId"),
            @Mapping(target = "crmDataId", source = "reqVO.id")
    })
    TransferCrmPermissionBO convert(CrmTransferContactReqVO reqVO, Long userId);

}
