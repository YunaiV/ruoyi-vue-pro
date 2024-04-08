package cn.iocoder.yudao.module.ai.controller.admin;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.controller.admin.vo.AiChatReqVO;
import cn.iocoder.yudao.module.ai.enums.AiModelEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Scanner;
import java.util.function.Consumer;

// TODO @fansili：有了 swagger 注释，就不用类注释了
/**
 * AI模块
 *
 * author: fansili
 * time: 2024/3/3 20:28
 */
@Tag(name = "AI模块")
@RestController
@RequestMapping("/ai-api")
@Slf4j
public class ChatController {

    @Autowired
    private ApplicationContext applicationContext;

    @PostMapping("/chat")
    @Operation(summary = "对话聊天", description = "简单的ai聊天")
    public CommonResult chat(@RequestBody @Validated AiChatReqVO reqVO) {
        ChatClient chatClient = getChatClient(reqVO.getAiModel());
        String res;
        try {
            res = chatClient.call(reqVO.getInputText());
        } catch (Exception e) {
            res = e.getMessage();
        }
        return CommonResult.success(res);
    }

    @PostMapping("/chatStream")
    @Operation(summary = "对话聊天chatStream", description = "简单的ai聊天")
    public CommonResult chatStream(HttpServletResponse response, @RequestBody @Validated AiChatReqVO reqVO) throws InterruptedException {
        OpenAiChatClient chatClient = applicationContext.getBean(OpenAiChatClient.class);
        Flux<ChatResponse> chatResponse = chatClient.stream(new Prompt(reqVO.getInputText()));
        chatResponse.subscribe(new Consumer<ChatResponse>() {
            @Override
            public void accept(ChatResponse chatResponse) {
                System.err.println(chatResponse.getResults().get(0).getOutput().getContent());
            }
        });
        return CommonResult.success("1");
    }

    public static void main(String[] args) {
        OpenAiChatClient openAiChatClient = new OpenAiChatClient(new OpenAiApi("openkey"));
        Flux<ChatResponse> responseFlux = openAiChatClient.stream(new Prompt("最好的编程语言!"));
        long now = System.currentTimeMillis();
        responseFlux.subscribe(new Consumer<ChatResponse>() {
            @Override
            public void accept(ChatResponse chatResponse) {
                if (chatResponse.getResults().get(0).getOutput() == null) {
                    return;
                }
                System.err.println(chatResponse.getResults().get(0).getOutput().getContent());
            }
        });

        // 阻止退出
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }

    /**
     * 根据 ai模型 获取对于的 模型实现类
     *
     * @param aiModelEnum
     * @return
     */
    private ChatClient getChatClient(AiModelEnum aiModelEnum) {
        if (AiModelEnum.OPEN_AI_GPT_3_5 == aiModelEnum) {
            return applicationContext.getBean(OpenAiChatClient.class);
        }
        // AI模型暂不支持
        throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MODULE_NOT_SUPPORTED);
    }
}
