package cn.iocoder.yudao.module.ai1.enums.session;

import cn.hutool.core.util.ObjUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI1 会话流事件的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum Ai1SessionStreamEventEnum {

    STREAM("stream", "结果流标识"), // 连接建立时下发，data 为助手消息编号
    THINKING("thinking", "思考过程增量"),
    MESSAGE("message", "回复内容增量"),
    PING("ping", "空闲心跳"),
    DONE("done", "生成结束"),
    ERROR("error", "生成失败"); // data 为错误提示

    /**
     * 事件
     */
    private final String event;
    /**
     * 名称
     */
    private final String name;

    public static boolean isTerminal(String event) {
        return ObjUtil.equal(DONE.event, event) || ObjUtil.equal(ERROR.event, event);
    }

}
