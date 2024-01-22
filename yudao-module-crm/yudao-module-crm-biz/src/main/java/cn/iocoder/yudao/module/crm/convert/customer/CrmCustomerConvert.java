package cn.iocoder.yudao.module.crm.convert.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerTransferReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

/**
 * 客户 Convert
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerConvert {

    CrmCustomerConvert INSTANCE = Mappers.getMapper(CrmCustomerConvert.class);

    default CrmCustomerRespVO convert(CrmCustomerDO customer, Map<Long, AdminUserRespDTO> userMap,
                                      Map<Long, DeptRespDTO> deptMap) {
        CrmCustomerRespVO customerResp = BeanUtils.toBean(customer, CrmCustomerRespVO.class);
        setUserInfo(customerResp, userMap, deptMap);
        return customerResp;
    }

    default PageResult<CrmCustomerRespVO> convertPage(PageResult<CrmCustomerDO> pageResult, Map<Long, AdminUserRespDTO> userMap,
                                                      Map<Long, DeptRespDTO> deptMap, Map<Long, Long> poolDayMap) {
        PageResult<CrmCustomerRespVO> result = BeanUtils.toBean(pageResult, CrmCustomerRespVO.class);
        result.getList().forEach(item -> {
            setUserInfo(item, userMap, deptMap);
            findAndThen(poolDayMap, item.getId(), item::setPoolDay);
        });
        return result;
    }

    /**
     * 设置用户信息
     *
     * @param customer CRM 客户 Response VO
     * @param userMap  用户信息 map
     * @param deptMap  用户部门信息 map
     */
    static void setUserInfo(CrmCustomerRespVO customer, Map<Long, AdminUserRespDTO> userMap, Map<Long, DeptRespDTO> deptMap) {
        customer.setAreaName(AreaUtils.format(customer.getAreaId()));
        findAndThen(userMap, customer.getOwnerUserId(), user -> {
            customer.setOwnerUserName(user.getNickname());
            findAndThen(deptMap, user.getDeptId(), dept -> customer.setOwnerUserDeptName(dept.getName()));
        });
        findAndThen(userMap, Long.parseLong(customer.getCreator()), user -> customer.setCreatorName(user.getNickname()));
    }

    @Mapping(target = "bizId", source = "reqVO.id")
    CrmPermissionTransferReqBO convert(CrmCustomerTransferReqVO reqVO, Long userId);

}
