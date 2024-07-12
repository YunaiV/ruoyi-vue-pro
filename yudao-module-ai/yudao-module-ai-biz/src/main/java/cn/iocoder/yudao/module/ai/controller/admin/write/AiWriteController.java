package cn.iocoder.yudao.module.ai.controller.admin.write;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWritePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.write.AiWriteDO;
import cn.iocoder.yudao.module.ai.service.write.AiWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 写作")
@RestController
@RequestMapping("/ai/write")
public class AiWriteController {

    @Resource
    private AiWriteService writeService;

    @PostMapping(value = "/generate-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "写作生成（流式）", description = "流式返回，响应较快")
    @PermitAll  // 解决 SSE 最终响应的时候，会被 Access Denied 拦截的问题
    public Flux<CommonResult<String>> generateWriteContent(@RequestBody @Valid AiWriteGenerateReqVO generateReqVO) {
        return writeService.generateWriteContent(generateReqVO, getLoginUserId());
    }

    // ================ 写作管理 ================

    @DeleteMapping("/delete")
    @Operation(summary = "删除写作")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:write:delete')")
    public CommonResult<Boolean> deleteWrite(@RequestParam("id") Long id) {
        writeService.deleteWrite(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得写作分页")
    @PreAuthorize("@ss.hasPermission('ai:write:query')")
    public CommonResult<PageResult<AiWriteRespVO>> getWritePage(@Valid AiWritePageReqVO pageReqVO) {
        PageResult<AiWriteDO> pageResult = writeService.getWritePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiWriteRespVO.class));
    }

}
