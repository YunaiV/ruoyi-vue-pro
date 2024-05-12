package org.springframework.ai.chat;

/**
 * 聊天异常
 *
 * author: fansili
 * time: 2024/3/15 20:45
 */
public class ChatException extends RuntimeException {

    public ChatException(String message) {
        super(message);
    }

}
