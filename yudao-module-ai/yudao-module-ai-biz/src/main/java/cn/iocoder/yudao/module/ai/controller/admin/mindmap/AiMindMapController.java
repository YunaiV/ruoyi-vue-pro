package cn.iocoder.yudao.module.ai.controller.admin.mindmap;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.mindmap.vo.AiMindMapGenerateReqVO;
import cn.iocoder.yudao.module.ai.service.mindmap.AiMindMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 思维导图")
@RestController
@RequestMapping("/ai/mind-map")
public class AiMindMapController {

    @Resource
    private AiMindMapService mindMapService;

    @PostMapping(value = "/generate-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "脑图生成（流式）", description = "流式返回，响应较快")
    @PermitAll  // 解决 SSE 最终响应的时候，会被 Access Denied 拦截的问题
    public Flux<CommonResult<String>> generateMindMap(@RequestBody @Valid AiMindMapGenerateReqVO generateReqVO) {
        return mindMapService.generateMindMap(generateReqVO, getLoginUserId());
    }

}
