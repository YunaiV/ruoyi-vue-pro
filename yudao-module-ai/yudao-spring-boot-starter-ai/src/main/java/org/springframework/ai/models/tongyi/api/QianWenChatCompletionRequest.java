package org.springframework.ai.models.tongyi.api;

import com.alibaba.dashscope.aigc.generation.models.QwenParam;

/**
 * 千问
 *
 * author: fansili
 * time: 2024/3/13 21:07
 */
public class QianWenChatCompletionRequest extends QwenParam {

    protected QianWenChatCompletionRequest(QwenParamBuilder<?, ?> b) {
        super(b);
    }
}
