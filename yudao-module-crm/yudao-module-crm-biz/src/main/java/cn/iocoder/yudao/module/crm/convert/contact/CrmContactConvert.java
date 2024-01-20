package cn.iocoder.yudao.module.crm.convert.contact;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.CrmContactTransferReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmUpdateFollowUpReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

/**
 * CRM 联系人 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmContactConvert {

    CrmContactConvert INSTANCE = Mappers.getMapper(CrmContactConvert.class);

    CrmContactRespVO convert(CrmContactDO bean);

    @Mapping(target = "bizId", source = "reqVO.id")
    CrmPermissionTransferReqBO convert(CrmContactTransferReqVO reqVO, Long userId);

    default PageResult<CrmContactRespVO> convertPage(PageResult<CrmContactDO> pageResult, Map<Long, AdminUserRespDTO> userMap,
                                                     List<CrmCustomerDO> customerList, List<CrmContactDO> parentContactList) {
        PageResult<CrmContactRespVO> voPageResult = BeanUtils.toBean(pageResult, CrmContactRespVO.class);
        // 拼接关联字段
        Map<Long, CrmContactDO> parentContactMap = convertMap(parentContactList, CrmContactDO::getId);
        Map<Long, CrmCustomerDO> customerMap = convertMap(customerList, CrmCustomerDO::getId);
        voPageResult.getList().forEach(item -> {
            setUserInfo(item, userMap);
            findAndThen(customerMap, item.getCustomerId(), customer -> item.setCustomerName(customer.getName()));
            findAndThen(parentContactMap, item.getParentId(), contactDO -> item.setParentName(contactDO.getName()));
        });
        return voPageResult;
    }

    default CrmContactRespVO convert(CrmContactDO contactDO, Map<Long, AdminUserRespDTO> userMap,
                                     List<CrmCustomerDO> customerList, List<CrmContactDO> parentContactList) {
        CrmContactRespVO contactVO = convert(contactDO);
        setUserInfo(contactVO, userMap);
        Map<Long, CrmCustomerDO> customerMap = CollectionUtils.convertMap(customerList, CrmCustomerDO::getId);
        Map<Long, CrmContactDO> contactMap = CollectionUtils.convertMap(parentContactList, CrmContactDO::getId);
        findAndThen(customerMap, contactDO.getCustomerId(), customer -> contactVO.setCustomerName(customer.getName()));
        findAndThen(contactMap, contactDO.getParentId(), contact -> contactVO.setParentName(contact.getName()));
        return contactVO;
    }

    static void setUserInfo(CrmContactRespVO contactRespVO, Map<Long, AdminUserRespDTO> userMap) {
        contactRespVO.setAreaName(AreaUtils.format(contactRespVO.getAreaId()));
        findAndThen(userMap, contactRespVO.getOwnerUserId(), user -> contactRespVO.setOwnerUserName(user.getNickname()));
        findAndThen(userMap, Long.parseLong(contactRespVO.getCreator()), user -> contactRespVO.setCreatorName(user.getNickname()));
    }

    @Mapping(target = "id", source = "reqBO.bizId")
    CrmContactDO convert(CrmUpdateFollowUpReqBO reqBO);

    default List<CrmContactDO> convertList(List<CrmUpdateFollowUpReqBO> list) {
        return CollectionUtils.convertList(list, INSTANCE::convert);
    }

}
