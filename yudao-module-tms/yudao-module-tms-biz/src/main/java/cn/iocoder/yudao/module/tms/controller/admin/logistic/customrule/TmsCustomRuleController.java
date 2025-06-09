package cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.system.api.utils.Validation;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo.*;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.customrule.TmsCustomRuleDO;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.TmsCustomRuleService;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.bo.TmsCustomRuleBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - ERP 海关规则")
@RestController
@RequestMapping("/tms/custom-rule")
@Validated
public class TmsCustomRuleController {

    @Resource
    private TmsCustomRuleService customRuleService;
    @Resource
    private ErpProductApi erpProductApi;

    @PostMapping("/create")
    @Operation(summary = "创建ERP 海关规则")
    @PreAuthorize("@ss.hasPermission('tms:custom-rule:create')")
    @Idempotent
    public CommonResult<Long> createCustomRule(@Validated(Validation.OnCreate.class) @RequestBody TmsCustomRuleSaveReqVO createReqVO) {
        return success(customRuleService.createCustomRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新ERP 海关规则")
    @PreAuthorize("@ss.hasPermission('tms:custom-rule:update')")
    public CommonResult<Boolean> updateCustomRule(@Validated(Validation.OnUpdate.class) @RequestBody TmsCustomRuleSaveReqVO updateReqVO) {
        customRuleService.updateCustomRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除ERP 海关规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('tms:custom-rule:delete')")
    public CommonResult<Boolean> deleteCustomRule(@NotNull @RequestParam("id") Long id) {
        customRuleService.deleteCustomRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得ERP 海关规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('tms:custom-rule:query')")
    public CommonResult<TmsCustomRuleRespVO> getCustomRule(@NotNull @RequestParam("id") Long id) {
        TmsCustomRuleBO customRule = customRuleService.getCustomRuleBOById(id);
        if (customRule == null) {
            return success(null);
        }
        List<TmsCustomRuleRespVO> voList = bindBOList(List.of(customRule));
        if (CollUtil.isEmpty(voList)) {
            return success(null);
        }
        TmsCustomRuleRespVO vo = voList.get(0);
        return success(vo);
    }

    @GetMapping("/page")
    @Operation(summary = "获得ERP 海关规则分页")
    @PreAuthorize("@ss.hasPermission('tms:custom-rule:query')")
    public CommonResult<PageResult<TmsCustomRuleRespVO>> getCustomRulePage(@Valid TmsCustomRulePageReqVO pageReqVO) {
        PageResult<TmsCustomRuleBO> page = customRuleService.getCustomRuleBOPage(pageReqVO);
        List<TmsCustomRuleRespVO> voList = bindBOList(page.getList());
        return success(new PageResult<>(voList, page.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出ERP 海关规则 Excel")
    @PreAuthorize("@ss.hasPermission('tms:custom-rule:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCustomRuleExcel(@Valid TmsCustomRulePageReqVO pageReqVO,
                                      HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<TmsCustomRuleBO> page = customRuleService.getCustomRuleBOPage(pageReqVO);
        List<TmsCustomRuleRespVO> voList = bindBOList(page.getList());
        // 导出 Excel
        ExcelUtils.write(response, "ERP 海关规则.xls", "数据", TmsCustomRuleRespVO.class, voList);
    }

    @PostMapping("/list-by-country-product")
    @Operation(summary = "根据国别和产品ID集合获得海关规则")
    public CommonResult<List<TmsCustomRuleListRespVO>> getCustomRuleListByCountryAndProducts(@Valid @RequestBody TmsCustomRuleListReqVO reqVO) {
        List<TmsCustomRuleDO> list = customRuleService.getCustomRuleListByCountryAndProducts(reqVO.getCountryCode(), reqVO.getProductIds());
        return success(BeanUtils.toBean(list, TmsCustomRuleListRespVO.class));
    }

    private List<TmsCustomRuleRespVO> bindBOList(List<TmsCustomRuleBO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, ErpProductDTO> map = erpProductApi.getProductMap(convertSet(list, TmsCustomRuleDO::getProductId));
        //2开始拼接
        return BeanUtils.toBean(list, TmsCustomRuleRespVO.class, vo -> {
            MapUtils.findAndThen(map, vo.getProductId(), vo::setProduct);//设置产品vo
        });
    }

}