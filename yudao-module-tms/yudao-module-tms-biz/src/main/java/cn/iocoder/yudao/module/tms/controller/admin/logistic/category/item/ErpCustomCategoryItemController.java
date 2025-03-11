//package cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item;
//
//import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
//import cn.iocoder.yudao.framework.common.pojo.CommonResult;
//import cn.iocoder.yudao.framework.common.pojo.PageParam;
//import cn.iocoder.yudao.framework.common.pojo.PageResult;
//import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
//import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
//import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemPageReqVO;
//import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemRespVO;
//import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.item.vo.ErpCustomCategoryItemSaveReqVO;
//import cn.iocoder.yudao.module.tms.controller.admin.tool.Validation;
//import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item.ErpCustomCategoryItemDO;
//import cn.iocoder.yudao.module.tms.service.logistic.category.item.ErpCustomCategoryItemService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.annotation.Resource;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.validation.Valid;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.util.List;
//
//import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
//import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
//
//@Tag(name = "管理后台 - 海关分类子表")
/// /@RestController 暂不开启
//@RequestMapping("/tms/custom-rule-category-item")
//@Validated
//public class ErpCustomRuleCategoryItemController {
//
//    @Resource
//    private ErpCustomCategoryItemService customRuleCategoryItemService;
//
//    @PostMapping("/create")
//    @Operation(summary = "创建海关分类子表")
//    @PreAuthorize("@ss.hasPermission('tms:custom-rule-category-item:create')")
//    public CommonResult<Long> createCustomRuleCategoryItem(@Validated(Validation.OnCreate.class)  @RequestBody ErpCustomCategoryItemSaveReqVO createReqVO) {
//        return success(customRuleCategoryItemService.createCustomRuleCategoryItem(createReqVO));
//    }
//
//    @PutMapping("/update")
//    @Operation(summary = "更新海关分类子表")
//    @PreAuthorize("@ss.hasPermission('tms:custom-rule-category-item:update')")
//    public CommonResult<Boolean> updateCustomRuleCategoryItem(@Validated(Validation.OnUpdate.class)  @RequestBody ErpCustomCategoryItemSaveReqVO updateReqVO) {
//        customRuleCategoryItemService.updateCustomRuleCategoryItem(updateReqVO);
//        return success(true);
//    }
//
//    @DeleteMapping("/delete")
//    @Operation(summary = "删除海关分类子表")
//    @Parameter(name = "id", description = "编号", required = true)
//    @PreAuthorize("@ss.hasPermission('tms:custom-rule-category-item:delete')")
//    public CommonResult<Boolean> deleteCustomRuleCategoryItem(@RequestParam("id") Long id) {
//        customRuleCategoryItemService.deleteCustomRuleCategoryItem(id);
//        return success(true);
//    }
//
//    @GetMapping("/get")
//    @Operation(summary = "获得海关分类子表")
//    @Parameter(name = "id", description = "编号", required = true, example = "1024")
//    @PreAuthorize("@ss.hasPermission('tms:custom-rule-category-item:query')")
//    public CommonResult<ErpCustomCategoryItemRespVO> getCustomRuleCategoryItem(@RequestParam("id") Long id) {
//        ErpCustomCategoryItemDO customRuleCategoryItem = customRuleCategoryItemService.getCustomRuleCategoryItem(id);
//        return success(BeanUtils.toBean(customRuleCategoryItem, ErpCustomCategoryItemRespVO.class));
//    }
//
//    @GetMapping("/page")
//    @Operation(summary = "获得海关分类子表分页")
//    @PreAuthorize("@ss.hasPermission('tms:custom-rule-category-item:query')")
//    public CommonResult<PageResult<ErpCustomCategoryItemRespVO>> getCustomRuleCategoryItemPage(@Valid ErpCustomCategoryItemPageReqVO pageReqVO) {
//        PageResult<ErpCustomCategoryItemDO> pageResult = customRuleCategoryItemService.getCustomRuleCategoryItemPage(pageReqVO);
//        return success(BeanUtils.toBean(pageResult, ErpCustomCategoryItemRespVO.class));
//    }
//
//    @GetMapping("/export-excel")
//    @Operation(summary = "导出海关分类子表 Excel")
//    @PreAuthorize("@ss.hasPermission('tms:custom-rule-category-item:export')")
//    @ApiAccessLog(operateType = EXPORT)
//    public void exportCustomRuleCategoryItemExcel(@Valid ErpCustomCategoryItemPageReqVO pageReqVO,
//                                                  HttpServletResponse response) throws IOException {
//        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
//        List<ErpCustomCategoryItemDO> list = customRuleCategoryItemService.getCustomRuleCategoryItemPage(pageReqVO).getList();
//        // 导出 Excel
//        ExcelUtils.write(response, "海关分类子表.xls", "数据", ErpCustomCategoryItemRespVO.class,
//            BeanUtils.toBean(list, ErpCustomCategoryItemRespVO.class));
//    }
//
//}