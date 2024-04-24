package cn.iocoder.yudao.module.ai.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * chat message
 *
 * @author fansili
 * @time 2024/4/24 17:22
 * @since 1.0
 */
@Tag(name = "A3-聊天-对话")
@RestController
@RequestMapping("/ai/chat/conversation")
@Slf4j
@AllArgsConstructor
public class ChatMessageController {
}
