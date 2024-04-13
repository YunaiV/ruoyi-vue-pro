package cn.iocoder.yudao.module.ai.controller.admin;

import cn.iocoder.yudao.framework.ai.chatqianwen.QianWenChatClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fansili
 * @since 1.0
 * @time 2024/4/13 17:44
 */
@Tag(name = "AI模块")
@RestController
@RequestMapping("/ai-api")
@Slf4j
@AllArgsConstructor
public class ChatController {

//
//    @Autowired
//    private QianWenChatClient qianWenChatClient;
//
//    @GetMapping("/chat")
//    public String chat(@RequestParam("prompt") String prompt) {
//        return qianWenChatClient.call(prompt);
//    }

}
