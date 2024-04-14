package cn.iocoder.yudao.module.crm.controller.admin.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticBusinessEndStatusRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticFunnelRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticsBusinessSummaryByDateRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticsFunnelReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessStatusService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.statistics.CrmStatisticsFunnelService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertListByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - CRM 销售漏斗")
@RestController
@RequestMapping("/crm/statistics-funnel")
@Validated
public class CrmStatisticsFunnelController {

    @Resource
    private CrmStatisticsFunnelService crmStatisticsFunnelService;
    @Resource
    private CrmBusinessService businessService;
    @Resource
    private CrmCustomerService customerService;
    @Resource
    private CrmBusinessStatusService businessStatusTypeService;
    @Resource
    private CrmBusinessStatusService businessStatusService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/get-funnel-summary")
    @Operation(summary = "获取销售漏斗统计数据", description = "用于【销售漏斗】页面")
    @PreAuthorize("@ss.hasPermission('crm:statistics-funnel:query')")
    public CommonResult<CrmStatisticFunnelRespVO> getFunnelSummary(@Valid CrmStatisticsFunnelReqVO reqVO) {
        return success(crmStatisticsFunnelService.getFunnelSummary(reqVO));
    }

    @GetMapping("/get-business-end-status-summary")
    @Operation(summary = "获取商机结束状态统计", description = "用于【销售漏斗】页面")
    @PreAuthorize("@ss.hasPermission('crm:statistics-funnel:query')")
    public CommonResult<List<CrmStatisticBusinessEndStatusRespVO>> getBusinessEndStatusSummary(@Valid CrmStatisticsFunnelReqVO reqVO) {
        return success(crmStatisticsFunnelService.getBusinessEndStatusSummary(reqVO));
    }

    @GetMapping("/get-business-summary-by-date")
    @Operation(summary = "获取新增商机分析(按日期)", description = "用于【销售漏斗】页面")
    @PreAuthorize("@ss.hasPermission('crm:statistics-funnel:query')")
    public CommonResult<List<CrmStatisticsBusinessSummaryByDateRespVO>> getBusinessSummaryByDate(@Valid CrmStatisticsFunnelReqVO reqVO) {
        return success(crmStatisticsFunnelService.getBusinessSummaryByDate(reqVO));
    }

    @GetMapping("/get-business-page-by-date")
    @Operation(summary = "获得商机分页(按日期)", description = "用于【销售漏斗】页面")
    @PreAuthorize("@ss.hasPermission('crm:business:query')")
    public CommonResult<PageResult<CrmBusinessRespVO>> getBusinessPageByDate(@Valid CrmStatisticsFunnelReqVO pageVO) {
        PageResult<CrmBusinessDO> pageResult = crmStatisticsFunnelService.getBusinessPageByDate(pageVO);
        return success(new PageResult<>(buildBusinessDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    private List<CrmBusinessRespVO> buildBusinessDetailList(List<CrmBusinessDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1.1 获取客户列表
        Map<Long, CrmCustomerDO> customerMap = customerService.getCustomerMap(
                convertSet(list, CrmBusinessDO::getCustomerId));
        // 1.2 获取创建人、负责人列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(list,
                contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        // 1.3 获得商机状态组
        Map<Long, CrmBusinessStatusTypeDO> statusTypeMap = businessStatusTypeService.getBusinessStatusTypeMap(
                convertSet(list, CrmBusinessDO::getStatusTypeId));
        Map<Long, CrmBusinessStatusDO> statusMap = businessStatusService.getBusinessStatusMap(
                convertSet(list, CrmBusinessDO::getStatusId));
        // 2. 拼接数据
        return BeanUtils.toBean(list, CrmBusinessRespVO.class, businessVO -> {
            // 2.1 设置客户名称
            MapUtils.findAndThen(customerMap, businessVO.getCustomerId(), customer -> businessVO.setCustomerName(customer.getName()));
            // 2.2 设置创建人、负责人名称
            MapUtils.findAndThen(userMap, NumberUtils.parseLong(businessVO.getCreator()),
                    user -> businessVO.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, businessVO.getOwnerUserId(), user -> {
                businessVO.setOwnerUserName(user.getNickname());
                MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> businessVO.setOwnerUserDeptName(dept.getName()));
            });
            // 2.3 设置商机状态
            MapUtils.findAndThen(statusTypeMap, businessVO.getStatusTypeId(), statusType -> businessVO.setStatusTypeName(statusType.getName()));
            MapUtils.findAndThen(statusMap, businessVO.getStatusId(), status -> businessVO.setStatusName(
                    businessService.getBusinessStatusName(businessVO.getEndStatus(), status)));
        });
    }

}
