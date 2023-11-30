package cn.iocoder.yudao.module.crm.convert.contact;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

// TODO 芋艿：convert 后面在梳理下，略微有点乱
/**
 * CRM 联系人 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ContactConvert {

    ContactConvert INSTANCE = Mappers.getMapper(ContactConvert.class);

    CrmContactDO convert(CrmContactCreateReqVO bean);

    CrmContactDO convert(CrmContactUpdateReqVO bean);

    CrmContactRespVO convert(CrmContactDO bean);

    List<CrmContactRespVO> convertList(List<CrmContactDO> list);

    PageResult<CrmContactRespVO> convertPage(PageResult<CrmContactDO> page);

    default PageResult<CrmContactRespVO> convertPage(PageResult<CrmContactDO> pageResult, Map<Long, AdminUserRespDTO> userMap,
                                                     List<CrmCustomerDO> customerList, List<CrmContactDO> parentContactList) {
        List<CrmContactRespVO> list = converList(pageResult.getList(), userMap, customerList, parentContactList);
        return convertPage(pageResult).setList(list);
    }

    List<CrmContactSimpleRespVO> convertAllList(List<CrmContactDO> list);

    @Mappings({
            @Mapping(target = "bizId", source = "reqVO.id"),
            @Mapping(target = "newOwnerUserId", source = "reqVO.id")
    })
    CrmPermissionTransferReqBO convert(CrmContactTransferReqVO reqVO, Long userId);

    /**
     * 转换详情信息
     *
     * @param contactDO         联系人
     * @param userMap           用户列表
     * @param crmCustomerDOList 客户
     * @return ContactRespVO
     */
    default CrmContactRespVO convert(CrmContactDO contactDO, Map<Long, AdminUserRespDTO> userMap, List<CrmCustomerDO> crmCustomerDOList,
                                     List<CrmContactDO> contactList) {
        CrmContactRespVO contactVO = convert(contactDO);
        setUserInfo(contactVO, userMap);
        Map<Long, CrmCustomerDO> ustomerMap = crmCustomerDOList.stream().collect(Collectors.toMap(CrmCustomerDO::getId, v -> v));
        Map<Long, CrmContactDO> contactMap = contactList.stream().collect(Collectors.toMap(CrmContactDO::getId, v -> v));
        findAndThen(ustomerMap, contactDO.getCustomerId(), customer -> contactVO.setCustomerName(customer.getName()));
        findAndThen(contactMap, contactDO.getParentId(), contact -> contactVO.setParentName(contact.getName()));
        return contactVO;
    }

    default List<CrmContactRespVO> converList(List<CrmContactDO> contactList, Map<Long, AdminUserRespDTO> userMap,
                                              List<CrmCustomerDO> customerList, List<CrmContactDO> parentContactList) {
        List<CrmContactRespVO> result = convertList(contactList);
        Map<Long, CrmContactDO> parentContactMap = convertMap(parentContactList, CrmContactDO::getId);
        Map<Long, CrmCustomerDO> customerMap = convertMap(customerList, CrmCustomerDO::getId);
        result.forEach(item -> {
            setUserInfo(item, userMap);
            findAndThen(customerMap, item.getCustomerId(), customer -> { // TODO @zyna：这里的 { 可以去掉
                item.setCustomerName(customer.getName());
            });
            findAndThen(parentContactMap, item.getParentId(), contactDO -> {  // TODO @zyna：这里的 { 可以去掉
                item.setParentName(contactDO.getName());
            });
        });
        return result;
    }

    /**
     * 设置用户信息
     *
     * @param contactRespVO 联系人Response VO
     * @param userMap       用户信息 map
     */
    static void setUserInfo(CrmContactRespVO contactRespVO, Map<Long, AdminUserRespDTO> userMap) {
        contactRespVO.setAreaName(AreaUtils.format(contactRespVO.getAreaId()));
        findAndThen(userMap, contactRespVO.getOwnerUserId(), user -> {
            contactRespVO.setOwnerUserName(user == null ? "" : user.getNickname());
        });
        findAndThen(userMap, Long.parseLong(contactRespVO.getCreator()), user -> contactRespVO.setCreatorName(user.getNickname()));
    }

}
