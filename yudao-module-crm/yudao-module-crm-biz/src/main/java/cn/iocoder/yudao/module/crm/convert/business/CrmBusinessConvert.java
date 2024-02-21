package cn.iocoder.yudao.module.crm.convert.business;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessTransferReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmUpdateFollowUpReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

    @Mapping(target = "bizId", source = "reqVO.id")
    CrmPermissionTransferReqBO convert(CrmBusinessTransferReqVO reqVO, Long userId);

    @Mapping(target = "id", source = "reqBO.bizId")
    CrmBusinessDO convert(CrmUpdateFollowUpReqBO reqBO);

    default List<CrmBusinessDO> convertList(List<CrmUpdateFollowUpReqBO> list) {
        return CollectionUtils.convertList(list, INSTANCE::convert);
    }

}
