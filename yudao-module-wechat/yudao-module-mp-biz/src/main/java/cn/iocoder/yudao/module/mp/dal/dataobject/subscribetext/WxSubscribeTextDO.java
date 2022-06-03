package cn.iocoder.yudao.module.mp.dal.dataobject.subscribetext;

import lombok.*;

import java.util.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 关注欢迎语 DO
 *
 * @author 芋道源码
 */
@TableName("wx_subscribe_text")
@KeySequence("wx_subscribe_text_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxSubscribeTextDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 消息类型 1文本消息；2图文消息；
     */
    private String msgType;
    /**
     * 模板ID
     */
    private String tplId;
    /**
     * 微信账号ID
     */
    private String wxAccountId;

}
