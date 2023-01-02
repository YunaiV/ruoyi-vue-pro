package cn.iocoder.yudao.module.mp.dal.dataobject.message;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.user.MpUserDO;
import cn.iocoder.yudao.module.mp.enums.message.MpMessageSendFrom;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import me.chanjar.weixin.common.api.WxConsts;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 微信消息 DO
 *
 * @author 芋道源码
 */
@TableName(value = "mp_message", autoResultMap = true)
@KeySequence("mp_message_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpMessageDO extends BaseDO {

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
     * 微信用户编号
     *
     * 关联 {@link MpUserDO#getId()}
     */
    private Long userId;
    /**
     * 用户标识
     *
     * 冗余 {@link MpUserDO#getId()}
     */
    private String openId;
//    /**
//     * 昵称
//     */
//    private String nickname;
//    /**
//     * 头像
//     */
//    private String headImageUrl;

    /**
     * 消息类型
     *
     * 枚举 {@link WxConsts.XmlMsgType}
     */
    private String type;
    /**
     * 消息来源
     *
     * 枚举 {@link MpMessageSendFrom}
     */
    private Integer sendFrom;

    // ========= 消息内容 ==========

    /**
     * 消息内容
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 TEXT
     */
    private String content;

    /**
     * 通过素材管理中的接口上传多媒体文件，得到的 id
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 IMAGE、VOICE、VIDEO
     */
    private String mediaId;
    /**
     * 标题
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO、MUSIC
     */
    private String title;
    /**
     * 描述
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO、MUSIC
     */
    private String description;

    /**
     * 音乐链接
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC
     */
    private String musicURL;
    /**
     * 高质量音乐链接，WIFI 环境优先使用该链接播放音乐
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC
     */
    private String hqMusicUrl;
    /**
     * 缩略图的媒体 id，通过素材管理中的接口上传多媒体文件，得到的 id
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 MUSIC
     */
    private String thumbMediaId;

    /**
     * 图文消息个数，限制为 10 条以内
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 NEWS
     */
    private Integer articleCount;
    /**
     * 图文消息数组
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 NEWS
     */
    @TableField(typeHandler = ArticleTypeHandler.class)
    private List<Article> articles;

    /**
     * 地理位置维度
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 LOCATION
     */
    private Double locationX;
    /**
     * 地理位置经度
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 LOCATION
     */
    private Double locationY;
    /**
     * 地图缩放大小
     *
     * 消息类型为 {@link WxConsts.XmlMsgType} 的 LOCATION
     */
    private Double scale;

    /**
     * 文章
     */
    @Data
    public static class Article implements Serializable {

        /**
         * 图文消息标题
         */
        private String title;
        /**
         * 图文消息描述
         */
        private String description;
        /**
         * 图片链接
         *
         * 支持JPG、PNG格式，较好的效果为大图 360*200，小图 200*200
         */
        private String picUrl;
        /**
         * 点击图文消息跳转链接
         */
        private String url;

    }

    // TODO @芋艿：可以找一些新的思路
    public static class ArticleTypeHandler extends AbstractJsonTypeHandler<List<Article>> {

        @Override
        protected List<Article> parse(String json) {
            return JsonUtils.parseArray(json, Article.class);
        }

        @Override
        protected String toJson(List<Article> obj) {
            return JsonUtils.toJsonString(obj);
        }

    }

}
