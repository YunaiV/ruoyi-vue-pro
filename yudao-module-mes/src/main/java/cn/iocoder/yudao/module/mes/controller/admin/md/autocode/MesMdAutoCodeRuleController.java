package cn.iocoder.yudao.module.mes.controller.admin.md.autocode;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.rule.MesMdAutoCodeRulePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.rule.MesMdAutoCodeRuleRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.rule.MesMdAutoCodeRuleSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodeRuleDO;
import cn.iocoder.yudao.module.mes.service.md.autocode.MesMdAutoCodeRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - MES 编码规则")
@RestController
@RequestMapping("/mes/md/auto-code-rule")
@Validated
public class MesMdAutoCodeRuleController {

    @Resource
    private MesMdAutoCodeRuleService ruleService;

    @PostMapping("/create")
    @Operation(summary = "创建编码规则")
    @PreAuthorize("@ss.hasPermission('mes:auto-code-rule:create')")
    public CommonResult<Long> createAutoCodeRule(@Valid @RequestBody MesMdAutoCodeRuleSaveReqVO createReqVO) {
        return success(ruleService.createAutoCodeRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新编码规则")
    @PreAuthorize("@ss.hasPermission('mes:auto-code-rule:update')")
    public CommonResult<Boolean> updateAutoCodeRule(@Valid @RequestBody MesMdAutoCodeRuleSaveReqVO updateReqVO) {
        ruleService.updateAutoCodeRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除编码规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:auto-code-rule:delete')")
    public CommonResult<Boolean> deleteAutoCodeRule(@RequestParam("id") Long id) {
        ruleService.deleteAutoCodeRule(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得编码规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:auto-code-rule:query')")
    public CommonResult<MesMdAutoCodeRuleRespVO> getAutoCodeRule(@RequestParam("id") Long id) {
        MesMdAutoCodeRuleDO rule = ruleService.getAutoCodeRule(id);
        return success(BeanUtils.toBean(rule, MesMdAutoCodeRuleRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得编码规则分页")
    @PreAuthorize("@ss.hasPermission('mes:auto-code-rule:query')")
    public CommonResult<PageResult<MesMdAutoCodeRuleRespVO>> getAutoCodeRulePage(@Valid MesMdAutoCodeRulePageReqVO pageReqVO) {
        PageResult<MesMdAutoCodeRuleDO> pageResult = ruleService.getAutoCodeRulePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, MesMdAutoCodeRuleRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得编码规则精简列表", description = "只包含被开启的编码规则，主要用于前端的下拉选项")
    public CommonResult<List<MesMdAutoCodeRuleRespVO>> getAutoCodeRuleSimpleList() {
        List<MesMdAutoCodeRuleDO> list = ruleService.getAutoCodeRuleListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, rule -> new MesMdAutoCodeRuleRespVO()
                .setId(rule.getId()).setName(rule.getName()).setCode(rule.getCode())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出编码规则 Excel")
    @PreAuthorize("@ss.hasPermission('mes:auto-code-rule:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportAutoCodeRuleExcel(@Valid MesMdAutoCodeRulePageReqVO pageReqVO,
                                 HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesMdAutoCodeRuleDO> list = ruleService.getAutoCodeRulePage(pageReqVO).getList();
        List<MesMdAutoCodeRuleRespVO> data = BeanUtils.toBean(list, MesMdAutoCodeRuleRespVO.class);
        ExcelUtils.write(response, "编码规则.xls", "数据", MesMdAutoCodeRuleRespVO.class, data);
    }

}
