package cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.ErpCustomProductPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.ErpCustomProductRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.ErpCustomProductSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.ErpCustomCategoryDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.ErpCustomProductDO;
import cn.iocoder.yudao.module.tms.service.logistic.category.ErpCustomCategoryService;
import cn.iocoder.yudao.module.tms.service.logistic.category.product.ErpCustomProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

@Tag(name = "管理后台 - 海关产品分类表")
@RestController
@RequestMapping("/tms/custom-product")
@Validated
public class ErpCustomProductController {
    @Resource
    private ErpCustomProductService customProductService;
    @Resource
    private ErpProductApi erpProductApi;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    ErpCustomCategoryService customCategoryService;
    @Resource
    DictDataApi dictDataApi;

    @PostMapping("/create")
    @Operation(summary = "创建海关产品分类表")
    @PreAuthorize("@ss.hasPermission('erp:custom-product:create')")
    public CommonResult<Long> createCustomProduct(@Valid @RequestBody ErpCustomProductSaveReqVO createReqVO) {
        return success(customProductService.createCustomProduct(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新海关产品分类表")
    @PreAuthorize("@ss.hasPermission('erp:custom-product:update')")
    public CommonResult<Boolean> updateCustomProduct(@Valid @RequestBody ErpCustomProductSaveReqVO updateReqVO) {
        customProductService.updateCustomProduct(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除海关产品分类表")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('erp:custom-product:delete')")
    public CommonResult<Boolean> deleteCustomProduct(@RequestParam("id") Long id) {
        customProductService.deleteCustomProduct(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得海关产品分类表")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('erp:custom-product:query')")
    public CommonResult<ErpCustomProductRespVO> getCustomProduct(@RequestParam("id") Long id) {
        ErpCustomProductDO customProduct = customProductService.getCustomProduct(id);

        return success(bingResult(Collections.singletonList(customProduct)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得海关产品分类表分页")
    @PreAuthorize("@ss.hasPermission('erp:custom-product:query')")
    public CommonResult<PageResult<ErpCustomProductRespVO>> getCustomProductPage(@Valid ErpCustomProductPageReqVO pageReqVO) {
        PageResult<ErpCustomProductDO> pageResult = customProductService.getCustomProductPage(pageReqVO);
        List<ErpCustomProductDO> list = pageResult.getList();
        List<ErpCustomProductRespVO> vos = bingResult(list);
        return success(new PageResult<>(vos, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出海关产品分类表 Excel")
    @PreAuthorize("@ss.hasPermission('erp:custom-product:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCustomProductExcel(@Valid ErpCustomProductPageReqVO pageReqVO,
                                         HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ErpCustomProductDO> list = customProductService.getCustomProductPage(pageReqVO).getList();
        List<ErpCustomProductRespVO> vos = bingResult(list);
        // 导出 Excel
        ExcelUtils.write(response, "海关产品分类表.xls", "数据", ErpCustomProductRespVO.class, vos);
    }

    private List<ErpCustomProductRespVO> bingResult(List<ErpCustomProductDO> oldList) {
        //list == null
        if (CollectionUtils.isEmpty(oldList)) {
            return Collections.emptyList();
        }
        //收集产品ids
        Set<Long> productIds = oldList.stream().map(ErpCustomProductDO::getProductId).collect(Collectors.toSet());
        //收集用户
        //1.3 获取用户信息
        Set<Long> userIds = oldList.stream()
            .flatMap(purchaseRequest -> Stream.of(
                safeParseLong(purchaseRequest.getCreator()),
                safeParseLong(purchaseRequest.getUpdater())
            ))
            .distinct()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        //海关分类ids
        Set<Long> categoryIds = oldList.stream().map(ErpCustomProductDO::getCustomCategoryId).collect(Collectors.toSet());
        //map
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, ErpProductDTO> productMap = erpProductApi.getProductMap(productIds);
        Map<Long, ErpCustomCategoryDO> categoryMap = customCategoryService.getCustomRuleCategoryMap(categoryIds);
        List<DictDataRespDTO> dtoList = dictDataApi.getDictDataList(PRODUCT_MATERIAL.getName());
        Map<String, String> materialMap = dtoList.stream().collect(Collectors.toMap(DictDataRespDTO::getValue, DictDataRespDTO::getLabel));

        return BeanUtils.toBean(oldList, ErpCustomProductRespVO.class, vo -> {
            MapUtils.findAndThen(productMap, vo.getProductId(), vo::setProduct);
            //创建者、更新者、审核人、申请人填充
            MapUtils.findAndThen(userMap, safeParseLong(vo.getCreator()), user -> vo.setCreator(user.getNickname()));
            MapUtils.findAndThen(userMap, safeParseLong(vo.getUpdater()), user -> vo.setUpdater(user.getNickname()));
            MapUtils.findAndThen(categoryMap, vo.getCustomCategoryId(), vo::setCustomCategory);
            vo.setCombinedValue(materialMap.get(vo.getCustomCategory().getMaterial().toString()) + vo.getCustomCategory().getDeclaredType());//组合值
        });
    }

    /**
     * 安全转换id为 Long
     *
     * @param value String类型的id
     * @return id
     */
    private Long safeParseLong(String value) {
        try {
            return Optional.ofNullable(value).map(Long::parseLong).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}