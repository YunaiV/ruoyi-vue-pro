package cn.iocoder.yudao.framework.ai.core.model.xinghuo.api;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * author: fansili
 * time: 2024/3/11 10:20
 */
@Data
@Accessors(chain = true)
public class XingHuoChatCompletion {
    private Header header;
    private Payload payload;

    @Data
    @Accessors(chain = true)
    public static class Header {
        private int code;
        private String message;
        private String sid;
        private int status;
    }

    @Data
    @Accessors(chain = true)
    public static class Payload {
        private Choices choices;
    }

    @Data
    @Accessors(chain = true)
    public static class Choices {
        private int status;
        private int seq;
        private List<Text> text;
    }

    @Data
    @Accessors(chain = true)
    public static class Text {
        private String content;
        private String role;
        private int index;
    }
}
