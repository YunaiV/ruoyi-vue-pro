package cn.iocoder.yudao.module.mes.controller.admin.md.autocode;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.part.MesMdAutoCodePartRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.part.MesMdAutoCodePartSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.service.md.autocode.MesMdAutoCodePartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 编码规则组成")
@RestController
@RequestMapping("/mes/md/auto-code-part")
@Validated
public class MesMdAutoCodePartController {

    @Resource
    private MesMdAutoCodePartService partService;

    @PostMapping("/create")
    @Operation(summary = "创建规则组成")
    @PreAuthorize("@ss.hasPermission('mes:auto-code-rule:update')")
    public CommonResult<Long> createAutoCodePart(@Valid @RequestBody MesMdAutoCodePartSaveReqVO createReqVO) {
        return success(partService.createAutoCodePart(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新规则组成")
    @PreAuthorize("@ss.hasPermission('mes:auto-code-rule:update')")
    public CommonResult<Boolean> updateAutoCodePart(@Valid @RequestBody MesMdAutoCodePartSaveReqVO updateReqVO) {
        partService.updateAutoCodePart(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除规则组成")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:auto-code-rule:update')")
    public CommonResult<Boolean> deleteAutoCodePart(@RequestParam("id") Long id) {
        partService.deleteAutoCodePart(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得规则组成")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:auto-code-rule:query')")
    public CommonResult<MesMdAutoCodePartRespVO> getAutoCodePart(@RequestParam("id") Long id) {
        MesMdAutoCodePartDO part = partService.getAutoCodePart(id);
        return success(BeanUtils.toBean(part, MesMdAutoCodePartRespVO.class));
    }

    @GetMapping("/list-by-rule-id")
    @Operation(summary = "根据规则 ID 获得规则组成列表")
    @Parameter(name = "ruleId", description = "规则 ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('mes:auto-code-rule:query')")
    public CommonResult<List<MesMdAutoCodePartRespVO>> getAutoCodePartListByRuleId(@RequestParam("ruleId") Long ruleId) {
        List<MesMdAutoCodePartDO> list = partService.getAutoCodePartListByRuleId(ruleId);
        return success(BeanUtils.toBean(list, MesMdAutoCodePartRespVO.class));
    }

}
