package cn.iocoder.yudao.module.im.dal.dataobject.message.content;

import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 引用消息内容
 * <p>
 * 客户端在 content.quote 里写入 messageId 表达"引用了哪条消息"，服务端反查原消息后重算 QuoteMessage 覆盖客户端写入，
 * 避免伪造发送人 / 类型 / 摘要
 *
 * @see TextMessage 等等消息内容里 quote 字段
 */
@Data
@Accessors(chain = true)
public class QuoteMessage {

    /**
     * content JSON 里 quote 字段名
     */
    public static final String FIELD_NAME = "quote";

    /**
     * QuoteMessage 里 messageId 字段名
     */
    public static final String FIELD_MESSAGE_ID = "messageId";

    /**
     * 被引用消息编号
     */
    private Long messageId;
    /**
     * 被引用消息发送人编号
     * <p>
     * 关联 AdminUserDO 的 id 字段
     */
    private Long senderId;
    /**
     * 被引用消息类型
     * <p>
     * 枚举 {@link ImContentTypeEnum}
     */
    private Integer type;
    /**
     * 原消息完整 content(JSON);服务端复制时会 removeQuote 防嵌套
     */
    private String content;

}
