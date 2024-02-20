package cn.iocoder.yudao.module.crm.convert.contact;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactTransferReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmUpdateFollowUpReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * CRM 联系人 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmContactConvert {

    CrmContactConvert INSTANCE = Mappers.getMapper(CrmContactConvert.class);

    @Mapping(target = "bizId", source = "reqVO.id")
    CrmPermissionTransferReqBO convert(CrmContactTransferReqVO reqVO, Long userId);

    @Mapping(target = "id", source = "reqBO.bizId")
    CrmContactDO convert(CrmUpdateFollowUpReqBO reqBO);

    default List<CrmContactDO> convertList(List<CrmUpdateFollowUpReqBO> list) {
        return CollectionUtils.convertList(list, INSTANCE::convert);
    }

}
