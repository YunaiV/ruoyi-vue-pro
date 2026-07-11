package cn.iocoder.yudao.module.crm.controller.admin.performance;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.config.CrmPerformanceConfigPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.config.CrmPerformanceConfigRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.performance.vo.config.CrmPerformanceConfigSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.performance.CrmPerformanceConfigDO;
import cn.iocoder.yudao.module.crm.enums.performance.CrmPerformanceConfigObjectTypeEnum;
import cn.iocoder.yudao.module.crm.service.performance.CrmPerformanceConfigService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Tag(name = "管理后台 - CRM 业绩目标设置")
@RestController
@RequestMapping("/crm/performance-config")
@Validated
public class CrmPerformanceConfigController {

    @Resource
    private CrmPerformanceConfigService performanceConfigService;

    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建业绩目标")
    @PreAuthorize("@ss.hasPermission('crm:performance-config:create')")
    public CommonResult<Long> createPerformanceConfig(@Valid @RequestBody CrmPerformanceConfigSaveReqVO createReqVO) {
        return success(performanceConfigService.createPerformanceConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新业绩目标")
    @PreAuthorize("@ss.hasPermission('crm:performance-config:update')")
    public CommonResult<Boolean> updatePerformanceConfig(@Valid @RequestBody CrmPerformanceConfigSaveReqVO updateReqVO) {
        performanceConfigService.updatePerformanceConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除业绩目标")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:performance-config:delete')")
    public CommonResult<Boolean> deletePerformanceConfig(@RequestParam("id") Long id) {
        performanceConfigService.deletePerformanceConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得业绩目标")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:performance-config:query')")
    public CommonResult<CrmPerformanceConfigRespVO> getPerformanceConfig(@RequestParam("id") Long id) {
        // 1. 获取业绩目标
        CrmPerformanceConfigDO performanceConfig = performanceConfigService.getPerformanceConfig(id);
        // 2. 拼接数据
        return success(buildPerformanceConfigDetail(performanceConfig));
    }

    private CrmPerformanceConfigRespVO buildPerformanceConfigDetail(CrmPerformanceConfigDO performanceConfig) {
        if (performanceConfig == null) {
            return null;
        }
        // 1. 转换成 VO
        CrmPerformanceConfigRespVO respVO = BeanUtils.toBean(performanceConfig, CrmPerformanceConfigRespVO.class);
        // 2. 拼接目标对象名称
        return buildPerformanceConfigObjectName(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得业绩目标分页")
    @PreAuthorize("@ss.hasPermission('crm:performance-config:query')")
    public CommonResult<PageResult<CrmPerformanceConfigRespVO>> getPerformanceConfigPage(@Valid CrmPerformanceConfigPageReqVO pageReqVO) {
        // 1. 查询业绩目标分页
        PageResult<CrmPerformanceConfigDO> pageResult = performanceConfigService.getPerformanceConfigPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 2. 拼接数据
        return success(BeanUtils.toBean(pageResult, CrmPerformanceConfigRespVO.class)
                .setList(buildPerformanceConfigDetailList(pageResult.getList())));
    }

    private List<CrmPerformanceConfigRespVO> buildPerformanceConfigDetailList(List<CrmPerformanceConfigDO> list) {
        if (CollUtil.isEmpty(list)) {
            return emptyList();
        }
        // 1. 转换成 VO
        List<CrmPerformanceConfigRespVO> voList = BeanUtils.toBean(list, CrmPerformanceConfigRespVO.class);
        // 2. 拼接目标对象名称
        return buildPerformanceConfigObjectNameList(voList);
    }

    // ========== VO 拼装 ==========

    private CrmPerformanceConfigRespVO buildPerformanceConfigObjectName(CrmPerformanceConfigRespVO config) {
        if (config == null) {
            return null;
        }
        return buildPerformanceConfigObjectNameList(singletonList(config)).get(0);
    }

    private List<CrmPerformanceConfigRespVO> buildPerformanceConfigObjectNameList(List<CrmPerformanceConfigRespVO> list) {
        if (CollUtil.isEmpty(list)) {
            return emptyList();
        }
        // 1.1 获取部门列表
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(list, CrmPerformanceConfigRespVO::getObjectId,
                item -> Objects.equals(item.getObjectType(), CrmPerformanceConfigObjectTypeEnum.DEPT.getObjectType())));
        // 1.2 获取员工列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(list, CrmPerformanceConfigRespVO::getObjectId,
                item -> Objects.equals(item.getObjectType(), CrmPerformanceConfigObjectTypeEnum.USER.getObjectType())));
        // 2. 设置目标对象名称
        list.forEach(item -> {
            if (Objects.equals(item.getObjectType(), CrmPerformanceConfigObjectTypeEnum.DEPT.getObjectType())) {
                findAndThen(deptMap, item.getObjectId(), dept -> item.setObjectName(dept.getName()));
            } else if (Objects.equals(item.getObjectType(), CrmPerformanceConfigObjectTypeEnum.USER.getObjectType())) {
                findAndThen(userMap, item.getObjectId(), user -> item.setObjectName(user.getNickname()));
            }
        });
        return list;
    }

}
