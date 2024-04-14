package cn.iocoder.yudao.module.ai.controller.admin.chat;

import cn.iocoder.yudao.framework.ai.chat.ChatResponse;
import cn.iocoder.yudao.framework.ai.chat.prompt.Prompt;
import cn.iocoder.yudao.framework.ai.config.AiClient;
import cn.iocoder.yudao.framework.ai.config.YudaoAiClient;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author fansili
 * @since 1.0
 * @time 2024/4/13 17:44
 */
@Tag(name = "AI模块")
@RestController
@RequestMapping("/admin-api/ai")
@Slf4j
@AllArgsConstructor
public class ChatController {

    private final AiClient aiClient;

    @PostMapping("/chat")
    public CommonResult<String> chat(@RequestParam("prompt") String prompt) {
        ChatResponse callRes = aiClient.call(new Prompt(prompt), "qianWen");
        return CommonResult.success(callRes.getResult().getOutput().getContent());
    }

}
