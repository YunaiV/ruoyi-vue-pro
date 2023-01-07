package cn.iocoder.yudao.framework.websocket.core;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class WebSocketMessageDO {
    /**
     * 接收消息的seesion
     */
    private List<Object> seesionKeyList;
    /**
     * 发送消息
     */
    private String msgText;

    public static WebSocketMessageDO build(List<Object> seesionKeyList, String msgText) {
        return new WebSocketMessageDO().setMsgText(msgText).setSeesionKeyList(seesionKeyList);
    }

}
