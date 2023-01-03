package cn.iocoder.yudao.module.mp.dal.dataobject.message;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.enums.message.MpAutoReplyMatchEnum;
import cn.iocoder.yudao.module.mp.enums.message.MpAutoReplyTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.api.WxConsts.XmlMsgType;

/**
 * 微信消息自动回复 DO
 *
 * @author 芋道源码
 */
@TableName(value = "mp_auto_reply", autoResultMap = true)
@KeySequence("mp_auto_reply_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpAutoReplyDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 微信公众号 ID
     *
     * 关联 {@link MpAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 微信公众号 appid
     *
     * 冗余 {@link MpAccountDO#getAppId()}
     */
    private String appId;

    /**
     * 回复类型
     *
     * 枚举 {@link MpAutoReplyTypeEnum}
     */
    private Integer type;

    // ==================== 请求消息 ====================

    /**
     * 请求的关键字
     *
     * 当 {@link #type} 为 {@link MpAutoReplyTypeEnum#KEYWORD}
     */
    private String requestKeyword;
    /**
     * 请求的关键字的匹配
     *
     * 当 {@link #type} 为 {@link MpAutoReplyTypeEnum#KEYWORD}
     *
     * 枚举 {@link MpAutoReplyMatchEnum}
     */
    private Integer requestMatch;

    /**
     * 请求的消息类型
     *
     * 当 {@link #type} 为 {@link MpAutoReplyTypeEnum#MESSAGE}
     *
     * 枚举 {@link XmlMsgType} 中的 TEXT、IMAGE、VOICE、VIDEO、SHORTVIDEO、LOCATION、LINK
     */
    private String requestMessageType;

    // ==================== 响应消息 ====================

    /**
     * 回复的消息类型
     *
     * 枚举 {@link XmlMsgType} 中的 TEXT、IMAGE、VOICE、VIDEO、NEWS
     */
    private String responseMessageType;

    /**
     * 回复的消息内容
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 TEXT
     */
    private String responseContent;

}
