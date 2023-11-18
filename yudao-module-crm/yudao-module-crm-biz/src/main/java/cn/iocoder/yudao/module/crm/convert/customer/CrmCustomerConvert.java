package cn.iocoder.yudao.module.crm.convert.customer;

import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerPoolConfigDO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmTransferPermissionReqBO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

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

    default PageResult<CrmCustomerRespVO> convertPage(PageResult<CrmCustomerDO> page, Map<Long, AdminUserRespDTO> userMap, Map<Long, DeptRespDTO> deptMap) {
        PageResult<CrmCustomerRespVO> result = convertPage(page);
        result.getList().forEach(customerRespVO -> {
            customerRespVO.setAreaName(AreaUtils.format(customerRespVO.getAreaId()));
            MapUtils.findAndThen(userMap, NumberUtil.parseLong(customerRespVO.getCreator()), creator ->
                    customerRespVO.setCreatorName(creator.getNickname()));
            MapUtils.findAndThen(userMap, customerRespVO.getOwnerUserId(), ownerUser -> {
                customerRespVO.setOwnerUserName(ownerUser.getNickname());
                MapUtils.findAndThen(deptMap, ownerUser.getDeptId(), dept ->
                        customerRespVO.setOwnerUserDept(dept.getName()));
            });
        });
        return result;
    }

    List<CrmCustomerExcelVO> convertList02(List<CrmCustomerDO> list);

    @Mappings({
            @Mapping(target = "bizId", source = "reqVO.id"),
            @Mapping(target = "newOwnerUserId", source = "reqVO.id")
    })
    CrmTransferPermissionReqBO convert(CrmCustomerTransferReqVO reqVO, Long userId);

    CrmCustomerPoolConfigRespVO convert(CrmCustomerPoolConfigDO customerPoolConfig);

    CrmCustomerPoolConfigDO convert(CrmCustomerPoolConfigUpdateReqVO updateReqVO);

}
