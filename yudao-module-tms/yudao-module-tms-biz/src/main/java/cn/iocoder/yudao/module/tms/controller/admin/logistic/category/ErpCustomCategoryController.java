package cn.iocoder.yudao.module.tms.controller.admin.logistic.category;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.api.utils.Validation;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemSimpleRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.ErpCustomCategoryPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.ErpCustomCategoryRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.vo.ErpCustomCategorySaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.ErpCustomCategoryDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item.ErpCustomCategoryItemDO;
import cn.iocoder.yudao.module.tms.service.logistic.category.ErpCustomCategoryService;
import cn.iocoder.yudao.module.tms.service.logistic.category.bo.ErpCustomCategoryBO;
import cn.iocoder.yudao.module.tms.service.logistic.category.item.ErpCustomCategoryItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.module.tms.enums.DictValue.PRODUCT_MATERIAL;

@Tag(name = "管理后台 - 海关分类")
@RestController
@RequestMapping("/tms/custom-category")
@Validated
public class ErpCustomCategoryController {

    @Resource
    DictDataApi dictDataApi;
    @Resource
    AdminUserApi adminUserApi;
    @Resource
    private ErpCustomCategoryService customRuleCategoryService;
    @Resource
    private ErpCustomCategoryItemService customRuleCategoryItemService;

    @PostMapping("/create")
    @Operation(summary = "创建海关分类")
    @PreAuthorize("@ss.hasPermission('tms:custom-category:create')")
    @Idempotent
    public CommonResult<Long> createCustomRuleCategory(@Validated(Validation.OnCreate.class) @RequestBody ErpCustomCategorySaveReqVO createReqVO) {
        return success(customRuleCategoryService.createCustomRuleCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新海关分类")
    @PreAuthorize("@ss.hasPermission('tms:custom-category:update')")
    public CommonResult<Boolean> updateCustomRuleCategory(@Validated(Validation.OnUpdate.class) @RequestBody ErpCustomCategorySaveReqVO updateReqVO) {
        customRuleCategoryService.updateCustomRuleCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除海关分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('tms:custom-category:delete')")
    public CommonResult<Boolean> deleteCustomRuleCategory(@NotNull(message = "id不能为null") @RequestParam("id") Long id) {
        customRuleCategoryService.deleteCustomRuleCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得海关分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('tms:custom-category:query')")
    public CommonResult<ErpCustomCategoryRespVO> getCustomRuleCategory(@NotNull(message = "id不能为null") @RequestParam("id") Long id) {
        ErpCustomCategoryBO customRuleCategoryBO = customRuleCategoryService.getCustomRuleCategoryBO(id);
        List<ErpCustomCategoryRespVO> vos = null;
        if (customRuleCategoryBO != null) {
            vos = BindingResult(List.of(customRuleCategoryBO));
        }
        return success(vos != null ? vos.get(0) : null);
    }

    @GetMapping("/page")
    @Operation(summary = "获得海关分类分页")
    @PreAuthorize("@ss.hasPermission('tms:custom-category:query')")
    public CommonResult<PageResult<ErpCustomCategoryRespVO>> getCustomRuleCategoryPage(@Valid ErpCustomCategoryPageReqVO pageReqVO) {
        PageResult<ErpCustomCategoryBO> pageResult = customRuleCategoryService.getCustomRuleCategoryPageBO(pageReqVO);
        return success(new PageResult<>(BindingResult(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出海关分类 Excel")
    @PreAuthorize("@ss.hasPermission('tms:custom-category:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCustomRuleCategoryExcel(@Valid ErpCustomCategoryPageReqVO pageReqVO,
                                              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpCustomCategoryDO> list = customRuleCategoryService.getCustomRuleCategoryPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "海关分类.xls", "数据", ErpCustomCategoryRespVO.class,
            BeanUtils.toBean(BindingResult(list), ErpCustomCategoryRespVO.class));
    }

    // ==================== 子表（海关分类子表） ====================

    @GetMapping("/custom-category-item/list-by-category-id")
    @Operation(summary = "获得海关分类子表列表")
    @Parameter(name = "categoryId", description = "分类表id")
    @PreAuthorize("@ss.hasPermission('tms:custom-category:query')")
    public CommonResult<List<ErpCustomCategoryItemRespVO>> getCustomRuleCategoryItemListByCategoryId(@NotNull(message = "主表id不能为null") @RequestParam("categoryId") Integer categoryId) {
        List<ErpCustomCategoryItemDO> itemDOList = customRuleCategoryService.getCustomRuleCategoryItemListByCategoryId(categoryId);
        if (CollectionUtils.isEmpty(itemDOList)) {
            return success(Collections.emptyList());
        }
        // 获取用户信息
        Set<Long> userIds = itemDOList.stream()
            .flatMap(item -> Stream.of(
                Optional.ofNullable(item.getCreator()).map(Long::parseLong),
                Optional.ofNullable(item.getUpdater()).map(Long::parseLong)
            ).filter(Optional::isPresent).map(Optional::get))
            .collect(Collectors.toSet());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        itemDOList.forEach(item -> {
            item.setCreator(userMap.get(Long.parseLong(item.getCreator())).getNickname());
            item.setUpdater(userMap.get(Long.parseLong(item.getUpdater())).getNickname());
        });
        return success(BeanUtils.toBean(itemDOList, ErpCustomCategoryItemRespVO.class));
    }

    //公共方法-查询组合字段+id
    @GetMapping({"/simple-list"})
    @Operation(summary = "获得海关分类组合值精简列表", description = "材料+报关品名")
    public CommonResult<List<ErpCustomCategoryItemSimpleRespVO>> getCustomRuleCategoryItemList() {
        List<ErpCustomCategoryDO> list = customRuleCategoryService.getCustomRuleCategoryList(null);
        List<ErpCustomCategoryRespVO> respVOS = BindingResult(list);
        return success(respVOS.stream().map(vo -> new ErpCustomCategoryItemSimpleRespVO()
            .setCustomCategoryId(vo.getId())
            .setCombinedValue(vo.getCombinedValue())).toList());
    }


    private List<ErpCustomCategoryRespVO> BindingResult(List<? extends ErpCustomCategoryDO> listDOs) {
        List<Long> ids = listDOs.stream().map(ErpCustomCategoryDO::getId).toList();
        Map<Long, List<ErpCustomCategoryItemDO>> itemMap = customRuleCategoryItemService.getCustomRuleCategoryItemMap(ids);
        //1 材料ids
        List<DictDataRespDTO> dtoList = dictDataApi.getDictDataList(PRODUCT_MATERIAL.getName());
        //1.1 构造map  字典的value:字典的label
        Map<String, String> materialMap = dtoList.stream().collect(Collectors.toMap(DictDataRespDTO::getValue, DictDataRespDTO::getLabel));
        // 1.2 构造人员map
        Set<Long> userIds = listDOs.stream()
            .flatMap(categoryDO -> Stream.of(
                Optional.ofNullable(categoryDO.getCreator()).map(Long::parseLong),
                Optional.ofNullable(categoryDO.getUpdater()).map(Long::parseLong)
            ).filter(Optional::isPresent).map(Optional::get))
            .collect(Collectors.toSet());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);

        //2 渲染
        return BeanUtils.toBean(listDOs, ErpCustomCategoryRespVO.class, vo -> {
            vo.setCombinedValue(materialMap.get(vo.getMaterial().toString()) + vo.getDeclaredType());//组合值
            List<ErpCustomCategoryItemDO> items = itemMap.get(vo.getId());
            Optional.ofNullable(vo.getUpdater()).ifPresent(updater -> vo.setUpdater(userMap.get(Long.parseLong(updater)).getNickname()));//创建人
            Optional.ofNullable(vo.getCreator()).ifPresent(creator -> vo.setCreator(userMap.get(Long.parseLong(creator)).getNickname()));//更新人

            vo.setCustomRuleCategoryItems(BeanUtils.toBean(items, ErpCustomCategoryItemRespVO.class, item -> {
                Optional.ofNullable(item.getUpdater()).ifPresent(updater -> item.setUpdater(userMap.get(Long.parseLong(updater)).getNickname()));//创建人
                Optional.ofNullable(item.getCreator()).ifPresent(creator -> item.setCreator(userMap.get(Long.parseLong(creator)).getNickname()));//更新人
            }));//子表

        });
    }
}