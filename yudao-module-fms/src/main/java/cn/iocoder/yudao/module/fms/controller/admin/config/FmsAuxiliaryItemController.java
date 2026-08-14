package cn.iocoder.yudao.module.fms.controller.admin.config;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemImportExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemImportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemStatusReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryTypeDO;
import cn.iocoder.yudao.module.fms.enums.config.FmsAuxiliaryTypeEnum;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryItemService;
import cn.iocoder.yudao.module.fms.service.config.FmsAuxiliaryTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - FMS 辅助核算项目")
@RestController
@RequestMapping("/fms/config/auxiliary-item")
@Validated
public class FmsAuxiliaryItemController {

    @Resource
    private FmsAuxiliaryItemService auxiliaryItemService;
    @Resource
    private FmsAuxiliaryTypeService auxiliaryTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建辅助核算项目")
    @PreAuthorize("@ss.hasPermission('fms:config:auxiliary:create')")
    public CommonResult<Long> createAuxiliaryItem(@Valid @RequestBody FmsAuxiliaryItemSaveReqVO createReqVO) {
        return success(auxiliaryItemService.createAuxiliaryItem(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新辅助核算项目")
    @PreAuthorize("@ss.hasPermission('fms:config:auxiliary:update')")
    public CommonResult<Boolean> updateAuxiliaryItem(
            @Valid @RequestBody FmsAuxiliaryItemSaveReqVO updateReqVO) {
        auxiliaryItemService.updateAuxiliaryItem(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "删除辅助核算项目")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024"),
            @Parameter(name = "ids", description = "辅助核算项目编号数组", required = true, example = "[1024]")
    })
    @PreAuthorize("@ss.hasPermission('fms:config:auxiliary:delete')")
    public CommonResult<Boolean> deleteAuxiliaryItemList(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("ids") @NotEmpty List<Long> ids) {
        auxiliaryItemService.deleteAuxiliaryItemList(accountSetId, ids, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新辅助核算项目状态")
    @PreAuthorize("@ss.hasPermission('fms:config:auxiliary:update')")
    public CommonResult<Boolean> updateAuxiliaryItemStatus(
            @Valid @RequestBody FmsAuxiliaryItemStatusReqVO reqVO) {
        auxiliaryItemService.updateAuxiliaryItemStatus(
                reqVO.getAccountSetId(), reqVO.getId(), reqVO.getStatus(), getLoginUserId());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得辅助核算项目")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024"),
            @Parameter(name = "id", description = "辅助核算项目编号", required = true, example = "1024")
    })
    @PreAuthorize("@ss.hasPermission('fms:config:auxiliary:query')")
    public CommonResult<FmsAuxiliaryItemRespVO> getAuxiliaryItem(@RequestParam("accountSetId") @NotNull Long accountSetId,
                                                                 @RequestParam("id") @NotNull Long id) {
        FmsAuxiliaryItemDO item = auxiliaryItemService.getAuxiliaryItem(accountSetId, id, getLoginUserId());
        return success(BeanUtils.toBean(item, FmsAuxiliaryItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得辅助核算项目分页")
    @PreAuthorize("@ss.hasAnyPermissions('fms:config:auxiliary:query', 'fms:voucher:create', "
            + "'fms:voucher:update')")
    public CommonResult<PageResult<FmsAuxiliaryItemRespVO>> getAuxiliaryItemPage(
            @Valid FmsAuxiliaryItemPageReqVO pageReqVO) {
        PageResult<FmsAuxiliaryItemDO> pageResult = auxiliaryItemService
                .getAuxiliaryItemPage(pageReqVO, getLoginUserId());
        return success(BeanUtils.toBean(pageResult, FmsAuxiliaryItemRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得辅助核算项目精简列表", description = "只包含启用的项目，主要用于前端下拉选项")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024"),
            @Parameter(name = "auxiliaryTypeId", description = "辅助核算类别编号", required = true,
                    example = "1024")
    })
    public CommonResult<List<FmsAuxiliaryItemRespVO>> getAuxiliaryItemSimpleList(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("auxiliaryTypeId") @NotNull Long auxiliaryTypeId) {
        List<FmsAuxiliaryItemDO> list = auxiliaryItemService
                .getAuxiliaryItemListByAccountSetIdAndAuxiliaryTypeIdAndStatus(
                        accountSetId, auxiliaryTypeId, CommonStatusEnum.ENABLE.getStatus(), getLoginUserId());
        return success(convertList(list, item -> new FmsAuxiliaryItemRespVO()
                .setId(item.getId()).setCode(item.getCode()).setName(item.getName())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出辅助核算项目")
    @PreAuthorize("@ss.hasPermission('fms:config:auxiliary:export')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void exportAuxiliaryItemExcel(
            @Valid FmsAuxiliaryItemPageReqVO pageReqVO, HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<FmsAuxiliaryItemDO> list = auxiliaryItemService
                .getAuxiliaryItemPage(pageReqVO, getLoginUserId()).getList();
        FmsAuxiliaryTypeDO auxiliaryType = auxiliaryTypeService.getAuxiliaryType(
                pageReqVO.getAccountSetId(), pageReqVO.getAuxiliaryTypeId(), getLoginUserId());
        ExcelUtils.write(response, auxiliaryType.getName() + ".xlsx", "辅助核算项目",
                buildAuxiliaryItemExcelHead(auxiliaryType.getType()), buildAuxiliaryItemExcelData(list,
                        auxiliaryType.getType()));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得辅助核算项目导入模板")
    @Parameter(name = "type", description = "辅助核算类别类型", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('fms:config:auxiliary:import')")
    @ApiAccessLog(operateType = OperateTypeEnum.EXPORT)
    public void getAuxiliaryItemImportTemplate(
            @RequestParam("type") @InEnum(FmsAuxiliaryTypeEnum.class) Integer type,
            HttpServletResponse response) throws IOException {
        FmsAuxiliaryItemImportExcelVO example = new FmsAuxiliaryItemImportExcelVO()
                .setCode("001").setName("示例项目").setRemark("示例备注");
        if (FmsAuxiliaryTypeEnum.INVENTORY.getType().equals(type)) {
            example.setSpecification("标准版").setUnit("个");
        }
        ExcelUtils.write(response, "辅助核算项目导入模板.xlsx", "辅助核算项目",
                FmsAuxiliaryItemImportExcelVO.class, Collections.singletonList(example));
    }

    @PostMapping("/import")
    @Operation(summary = "导入辅助核算项目")
    @Parameters({
            @Parameter(name = "accountSetId", description = "账套编号", required = true),
            @Parameter(name = "auxiliaryTypeId", description = "辅助核算类别编号", required = true),
            @Parameter(name = "file", description = "Excel 文件", required = true)
    })
    @PreAuthorize("@ss.hasPermission('fms:config:auxiliary:import')")
    @ApiAccessLog(operateType = OperateTypeEnum.IMPORT)
    public CommonResult<FmsAuxiliaryItemImportRespVO> importAuxiliaryItem(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("auxiliaryTypeId") @NotNull Long auxiliaryTypeId,
            @RequestParam("file") MultipartFile file) throws Exception {
        List<FmsAuxiliaryItemImportExcelVO> importItems = ExcelUtils.read(
                file, FmsAuxiliaryItemImportExcelVO.class);
        return success(auxiliaryItemService.importAuxiliaryItemList(
                accountSetId, auxiliaryTypeId, importItems, getLoginUserId()));
    }

    /** 构建辅助核算项目导出的动态表头 */
    private List<List<String>> buildAuxiliaryItemExcelHead(Integer type) {
        if (FmsAuxiliaryTypeEnum.CUSTOMER.getType().equals(type)
                || FmsAuxiliaryTypeEnum.SUPPLIER.getType().equals(type)) {
            return Arrays.asList(Collections.singletonList("编码"), Collections.singletonList("名称"),
                    Collections.singletonList("备注"));
        }
        if (FmsAuxiliaryTypeEnum.INVENTORY.getType().equals(type)) {
            return Arrays.asList(Collections.singletonList("编码"), Collections.singletonList("名称"),
                    Collections.singletonList("规格"), Collections.singletonList("单位"));
        }
        return Arrays.asList(Collections.singletonList("编码"), Collections.singletonList("名称"));
    }

    /** 构建辅助核算项目导出的动态数据 */
    private List<List<Object>> buildAuxiliaryItemExcelData(List<FmsAuxiliaryItemDO> items, Integer type) {
        List<List<Object>> rows = new ArrayList<>(items.size());
        for (FmsAuxiliaryItemDO item : items) {
            if (FmsAuxiliaryTypeEnum.CUSTOMER.getType().equals(type)
                    || FmsAuxiliaryTypeEnum.SUPPLIER.getType().equals(type)) {
                rows.add(Arrays.asList(item.getCode(), item.getName(), item.getRemark()));
            } else if (FmsAuxiliaryTypeEnum.INVENTORY.getType().equals(type)) {
                rows.add(Arrays.asList(item.getCode(), item.getName(), item.getSpecification(), item.getUnit()));
            } else {
                rows.add(Arrays.asList(item.getCode(), item.getName()));
            }
        }
        return rows;
    }

}
