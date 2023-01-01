package cn.iocoder.yudao.module.mp.dal.dataobject.fansmsg;

import lombok.*;

import java.util.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 粉丝消息表  DO
 *
 * @author 芋道源码
 */
@TableName("wx_fans_msg")
@KeySequence("wx_fans_msg_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxFansMsgDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 用户标识
     */
    private String openid;
    /**
     * 昵称
     */
    private byte[] nickname;
    /**
     * 头像地址
     */
    private String headimgUrl;
    /**
     * 微信账号ID
     */
    private String wxAccountId;
    /**
     * 消息类型
     */
    private String msgType;
    /**
     * 内容
     */
    private String content;
    /**
     * 最近一条回复内容
     */
    private String resContent;
    /**
     * 是否已回复
     */
    private String isRes;
    /**
     * 微信素材ID
     */
    private String mediaId;
    /**
     * 微信图片URL
     */
    private String picUrl;
    /**
     * 本地图片路径
     */
    private String picPath;

}
