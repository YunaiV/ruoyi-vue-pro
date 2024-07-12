package cn.iocoder.yudao.framework.ai.core.model.chatglm.api;

import com.zhipu.oapi.service.v4.image.ImageApiResponse;
import org.springframework.ai.image.ImageResponseMetadata;

import java.util.HashMap;

public class ChatGlmResponseMetadata extends HashMap<String, Object> implements ImageResponseMetadata {

    private Long created;

    public ChatGlmResponseMetadata(ImageApiResponse result) {
        created = result.getData().getCreated();
    }

    @Override
    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
