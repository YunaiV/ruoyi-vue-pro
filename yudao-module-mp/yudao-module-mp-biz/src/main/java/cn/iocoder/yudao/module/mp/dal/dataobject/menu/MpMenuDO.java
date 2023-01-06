package cn.iocoder.yudao.module.mp.dal.dataobject.menu;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.api.WxConsts.MenuButtonType;

import java.util.List;

/**
 * 微信菜单 DO
 *
 * 一个公众号，只有一个 MpMenuDO 记录。一个公众号的多个菜单，对应到就是 {@link #buttons} 多个按钮
 *
 * @author 芋道源码
 */
@TableName(value = "mp_menu", autoResultMap = true)
@KeySequence("mp_menu_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MpMenuDO extends BaseDO {

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
     * 按钮列表
     */
    @TableField(typeHandler = ButtonTypeHandler.class)
    private List<Button> buttons;
    /**
     * 同步状态
     *
     * true - 已同步
     * false - 未同步
     */
    private Boolean syncStatus;

    /**
     * 按钮
     */
    @Data
    public static class Button {

        /**
         * 类型
         *
         * 枚举 {@link MenuButtonType}
         */
        private String type;
        /**
         * 消息类型
         *
         * 当 {@link #type} 为 CLICK、SCANCODE_WAITMSG
         *
         * 枚举 {@link WxConsts.XmlMsgType} 中的 TEXT、IMAGE、VOICE、VIDEO、NEWS
         */
        private String messageType;
        /**
         * 名称
         */
        private String name;
        /**
         * 标识
         */
        private String key;
        /**
         * 二级菜单列表
         */
        private List<Button> subButtons;
        /**
         * 网页链接
         *
         * 用户点击菜单可打开链接，不超过 1024 字节
         *
         * 类型为 {@link WxConsts.XmlMsgType} 的 VIEW、MINIPROGRAM
         */
        private String url;

        /**
         * 小程序的 appId
         *
         * 类型为 {@link WxConsts.XmlMsgType} 的 MINIPROGRAM
         */
        private String appId;

        /**
         * 小程序的页面路径
         *
         * 类型为 {@link WxConsts.XmlMsgType} 的 MINIPROGRAM
         */
        private String pagePath;

        /**
         * 消息内容
         *
         * 消息类型为 {@link WxConsts.XmlMsgType} 的 TEXT
         */
        private String content;

        /**
         * 媒体 id
         *
         * 消息类型为 {@link WxConsts.XmlMsgType} 的 IMAGE、VOICE、VIDEO
         */
        private String mediaId;
        /**
         * 媒体 URL
         *
         * 消息类型为 {@link WxConsts.XmlMsgType} 的 IMAGE、VOICE、VIDEO
         */
        private String mediaUrl;

        /**
         * 回复的标题
         *
         * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO
         */
        private String title;
        /**
         * 回复的描述
         *
         * 消息类型为 {@link WxConsts.XmlMsgType} 的 VIDEO
         */
        private String description;

        /**
         * 图文消息
         *
         * 消息类型为 {@link WxConsts.XmlMsgType} 的 NEWS
         */
        private MpMessageDO.Article article;

    }

    // TODO @芋艿：可以找一些新的思路
    public static class ButtonTypeHandler extends AbstractJsonTypeHandler<List<Button>> {

        @Override
        protected List<Button> parse(String json) {
            return JsonUtils.parseArray(json, Button.class);
        }

        @Override
        protected String toJson(List<Button> obj) {
            return JsonUtils.toJsonString(obj);
        }

    }
}
