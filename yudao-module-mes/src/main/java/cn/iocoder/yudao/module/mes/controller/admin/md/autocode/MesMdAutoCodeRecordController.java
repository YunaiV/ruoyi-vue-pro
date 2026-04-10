package cn.iocoder.yudao.module.mes.controller.admin.md.autocode;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.record.MesMdAutoCodeGenerateReqVO;
import cn.iocoder.yudao.module.mes.service.md.autocode.MesMdAutoCodeRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 编码生成记录")
@RestController
@RequestMapping("/mes/md/auto-code-record")
@Validated
public class MesMdAutoCodeRecordController {

    @Resource
    private MesMdAutoCodeRecordService autoCodeRecordService;

    @PostMapping("/generate")
    @Operation(summary = "生成编码")
    @PreAuthorize("@ss.hasPermission('mes:auto-code-rule:query')")
    public CommonResult<String> generateAutoCode(@Valid @RequestBody MesMdAutoCodeGenerateReqVO generateReqVO) {
        String code = autoCodeRecordService.generateAutoCode(generateReqVO.getRuleCode(), generateReqVO.getInputChar());
        return success(code);
    }

}
