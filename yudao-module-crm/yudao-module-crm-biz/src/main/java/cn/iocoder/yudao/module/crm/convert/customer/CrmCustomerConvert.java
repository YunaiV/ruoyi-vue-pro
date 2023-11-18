package cn.iocoder.yudao.module.crm.convert.customer;

import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerPoolConfigDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
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

    CrmCustomerDO convert(CrmCustomerCreateReqVO bean);

    CrmCustomerDO convert(CrmCustomerUpdateReqVO bean);

    CrmCustomerRespVO convert(CrmCustomerDO bean);

    default CrmCustomerRespVO convert(CrmCustomerDO customer, Map<Long, CrmPermissionDO> ownerMap,
                                      Map<Long, AdminUserRespDTO> userMap, Map<Long, DeptRespDTO> deptMap) {
        CrmCustomerRespVO customerResp = convert(customer);
        findAndThen(ownerMap, customerResp.getId(), owner -> {
            customerResp.setOwnerUserId(owner.getUserId());
            customerResp.setAreaName(AreaUtils.format(customerResp.getAreaId()));
            findAndThen(userMap, owner.getUserId(), user -> {
                customerResp.setOwnerUserName(user.getNickname());
            });
            findAndThen(userMap, Long.parseLong(customerResp.getCreator()), user -> {
                customerResp.setCreatorName(user.getNickname());
            });
            findAndThen(deptMap, customerResp.getOwnerUserId(), dept -> {
                customerResp.setOwnerUserDeptName(dept.getName());
            });
        });
        return customerResp;
    }

    default PageResult<CrmCustomerRespVO> convertPage(PageResult<CrmCustomerDO> page, Map<Long, AdminUserRespDTO> userMap, Map<Long, DeptRespDTO> deptMap) {
        PageResult<CrmCustomerRespVO> result = convertPage(page);
        result.getList().forEach(customerRespVO -> {
            customerRespVO.setAreaName(AreaUtils.format(customerRespVO.getAreaId()));
            MapUtils.findAndThen(userMap, NumberUtil.parseLong(customerRespVO.getCreator()), creator ->
                    customerRespVO.setCreatorName(creator.getNickname()));
            MapUtils.findAndThen(userMap, customerRespVO.getOwnerUserId(), ownerUser -> {
                customerRespVO.setOwnerUserName(ownerUser.getNickname());
                MapUtils.findAndThen(deptMap, ownerUser.getDeptId(), dept ->
                        customerRespVO.setOwnerUserDeptName(dept.getName()));
            });
        });
        return result;
    }

    List<CrmCustomerExcelVO> convertList02(List<CrmCustomerDO> list);

    @Mappings({
            @Mapping(target = "bizId", source = "reqVO.id"),
            @Mapping(target = "newOwnerUserId", source = "reqVO.id")
    })
    CrmPermissionTransferReqBO convert(CrmCustomerTransferReqVO reqVO, Long userId);

    PageResult<CrmCustomerRespVO> convertPage(PageResult<CrmCustomerDO> page);

    // TODO @puhui999：两个 convertPage 的逻辑，合并下；
    default PageResult<CrmCustomerRespVO> convertPage(PageResult<CrmCustomerDO> pageResult, Map<Long, CrmPermissionDO> ownerMap,
                                                      Map<Long, AdminUserRespDTO> userMap, Map<Long, DeptRespDTO> deptMap) {
        PageResult<CrmCustomerRespVO> result = convertPage(pageResult);
        result.getList().forEach(item -> {
            findAndThen(ownerMap, item.getId(), owner -> {
                item.setOwnerUserId(owner.getUserId());
                item.setAreaName(AreaUtils.format(item.getAreaId()));
                findAndThen(userMap, owner.getUserId(), user -> {
                    item.setOwnerUserName(user.getNickname());
                });
                findAndThen(userMap, Long.parseLong(item.getCreator()), user -> {
                    item.setCreatorName(user.getNickname());
                });
                findAndThen(deptMap, item.getOwnerUserId(), dept -> {
                    item.setOwnerUserDeptName(dept.getName());
                });
            });
        });
        return result;
    }

    CrmCustomerPoolConfigRespVO convert(CrmCustomerPoolConfigDO customerPoolConfig);

    CrmCustomerPoolConfigDO convert(CrmCustomerPoolConfigUpdateReqVO updateReqVO);

}
