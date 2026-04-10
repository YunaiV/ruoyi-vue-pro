package cn.iocoder.yudao.module.mes.controller.admin.wm.packages;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.MesWmPackagePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.MesWmPackageRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.MesWmPackageSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.client.MesMdClientDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.unitmeasure.MesMdUnitMeasureDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.packages.MesWmPackageDO;
import cn.iocoder.yudao.module.mes.service.md.client.MesMdClientService;
import cn.iocoder.yudao.module.mes.service.md.unitmeasure.MesMdUnitMeasureService;
import cn.iocoder.yudao.module.mes.service.wm.packages.MesWmPackageService;
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

import java.util.*;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

// DONE @AI：检查下 java 类，是不是 author 没加。
/**
 * MES 装箱单 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - MES 装箱单")
@RestController
@RequestMapping("/mes/wm/package")
@Validated
public class MesWmPackageController {

    @Resource
    private MesWmPackageService packageService;
    @Resource
    private MesMdClientService clientService;
    @Resource
    private MesMdUnitMeasureService unitMeasureService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建装箱单")
    @PreAuthorize("@ss.hasPermission('mes:wm-package:create')")
    public CommonResult<Long> createPackage(@Valid @RequestBody MesWmPackageSaveReqVO createReqVO) {
        return success(packageService.createPackage(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改装箱单")
    @PreAuthorize("@ss.hasPermission('mes:wm-package:update')")
    public CommonResult<Boolean> updatePackage(@Valid @RequestBody MesWmPackageSaveReqVO updateReqVO) {
        packageService.updatePackage(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除装箱单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-package:delete')")
    public CommonResult<Boolean> deletePackage(@RequestParam("id") Long id) {
        packageService.deletePackage(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得装箱单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:wm-package:query')")
    public CommonResult<MesWmPackageRespVO> getPackage(@RequestParam("id") Long id) {
        MesWmPackageDO packageDO = packageService.getPackage(id);
        if (packageDO == null) {
            return success(null);
        }
        return success(buildRespVOList(Collections.singletonList(packageDO)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得装箱单分页")
    @PreAuthorize("@ss.hasPermission('mes:wm-package:query')")
    public CommonResult<PageResult<MesWmPackageRespVO>> getPackagePage(
            @Valid MesWmPackagePageReqVO pageReqVO) {
        PageResult<MesWmPackageDO> pageResult = packageService.getPackagePage(pageReqVO);
        return success(new PageResult<>(buildRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @PutMapping("/finish")
    @Operation(summary = "完成装箱单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-package:update')")
    public CommonResult<Boolean> finishPackage(@RequestParam("id") Long id) {
        packageService.finishPackage(id);
        return success(true);
    }

    @PutMapping("/add-child-package")
    @Operation(summary = "添加子箱")
    @PreAuthorize("@ss.hasPermission('mes:wm-package:update')")
    public CommonResult<Boolean> addChildPackage(@RequestParam("parentId") Long parentId,
            @RequestParam("childId") Long childId) {
        packageService.addChildPackage(parentId, childId);
        return success(true);
    }

    @PutMapping("/remove-child-package")
    @Operation(summary = "移除子箱")
    @Parameter(name = "childId", description = "子箱编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:wm-package:update')")
    public CommonResult<Boolean> removeChildPackage(@RequestParam("childId") Long childId) {
        packageService.removeChildPackage(childId);
        return success(true);
    }

    @GetMapping("/childable-simple-list")
    @Operation(summary = "可添加为子箱的装箱单精简列表")
    @PreAuthorize("@ss.hasPermission('mes:wm-package:query')")
    public CommonResult<List<MesWmPackageRespVO>> getChildablePackageSimpleList() {
        List<MesWmPackageDO> list = packageService.getChildablePackageList();
        return success(buildRespVOList(list));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得装箱单精简列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<MesWmPackageRespVO>> getPackageSimpleList() {
        List<MesWmPackageDO> list = packageService.getPackageSimpleList();
        return success(buildRespVOList(list));
    }

    // ========== 私有方法 ==========

    private List<MesWmPackageRespVO> buildRespVOList(List<MesWmPackageDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 批量查询客户
        Map<Long, MesMdClientDO> clientMap = clientService.getClientMap(
                convertSet(list, MesWmPackageDO::getClientId));
        // 批量查询计量单位（尺寸 + 重量）
        Map<Long, MesMdUnitMeasureDO> unitMeasureMap = unitMeasureService.getUnitMeasureMap(
                convertSetByFlatMap(list, p -> Stream.of(p.getSizeUnitId(), p.getWeightUnitId())));
        // 批量查询检查员
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(list, MesWmPackageDO::getInspectorUserId));
        // 构建返回结果
        return BeanUtils.toBean(list, MesWmPackageRespVO.class, vo -> {
            MapUtils.findAndThen(clientMap, vo.getClientId(),
                    client -> vo.setClientCode(client.getCode()).setClientName(client.getName())
                            .setClientNickname(client.getNickname()));
            MapUtils.findAndThen(unitMeasureMap, vo.getSizeUnitId(),
                    unit -> vo.setSizeUnitName(unit.getName()));
            MapUtils.findAndThen(unitMeasureMap, vo.getWeightUnitId(),
                    unit -> vo.setWeightUnitName(unit.getName()));
            MapUtils.findAndThen(userMap, vo.getInspectorUserId(),
                    user -> vo.setInspectorName(user.getNickname()));
        });
    }

}
