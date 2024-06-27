package cn.iocoder.yudao.module.ai.controller.admin.write;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.service.write.AiWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "管理后台 - AI 写作")
@RestController
@RequestMapping("/ai/write")
public class AiWriteController {

    @Resource
    private AiWriteService writeService;

    @PostMapping(value = "/generate-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PermitAll
    @Operation(summary = "作文生产（流式）", description = "流式返回，响应较快")
    public Flux<CommonResult<String>> generateComposition() {
        return writeService.generateComposition();
    }
}
