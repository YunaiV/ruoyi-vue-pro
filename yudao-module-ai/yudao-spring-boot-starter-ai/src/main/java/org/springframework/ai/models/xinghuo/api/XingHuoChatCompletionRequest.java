package org.springframework.ai.models.xinghuo.api;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 讯飞星火 request
 *
 * author: fansili
 * time: 2024/3/11 10:20
 */
@Data
@Accessors(chain = true)
public class XingHuoChatCompletionRequest {

    private Header header;
    private Parameter parameter;
    private Payload payload;

    @Data
    @Accessors(chain = true)
    public static class Header {
        private String app_id;
        private String uid;
    }

    @Data
    @Accessors(chain = true)
    public static class Parameter {
        private Chat chat;

        @Data
        @Accessors(chain = true)
        public static class Chat {
            /**
             * https://www.xfyun.cn/doc/spark/Web.html#_1-%E6%8E%A5%E5%8F%A3%E8%AF%B4%E6%98%8E
             *
             * 指定访问的领域:
             * general指向V1.5版本;
             * generalv2指向V2版本;
             * generalv3指向V3版本;
             * generalv3.5指向V3.5版本;
             * 注意：不同的取值对应的url也不一样！
             */
            private String domain = "generalv3.5";
            /**
             * 取值范围 (0，1] ，默认值0.5
             */
            private Float temperature;
            /**
             * V1.5取值为[1,4096]
             * V2.0、V3.0和V3.5取值为[1,8192]，默认为2048。
             */
            private Integer max_tokens;
            /**
             * 取值为[1，6],默认为4
             */
            private Integer top_k;
            /**
             * 	需要保障用户下的唯一性，用于关联用户会话
             */
            private String chat_id;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class Payload {
        private Message message;

        @Data
        @Accessors(chain = true)
        public static class Message {
            private List<Text> text;


            @Data
            @Accessors(chain = true)
            public static class Text {
                /**
                 * 角色
                 */
                private String role;
                /**
                 * 消息内容
                 */
                private String content;
                private Integer index;

                @Getter
                public static enum Role {
                    SYSTEM("system"),
                    USER("user"),
                    ASSISTANT("assistant");
                    private String name;

                    private Role(String name) {
                        this.name = name;
                    }
                }
            }
        }
    }
}
