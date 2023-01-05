package cn.iocoder.yudao.module.mp.dal.dataobject.menu;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.message.MpMessageDO;
import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import lombok.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.bean.menu.WxMenuButton;

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
    @TableField(typeHandler = WxMenuButtonTypeHandler.class)
    private List<WxMenuButton> buttons;
    /**
     * 同步状态
     *
     * true - 已同步
     * false - 未同步
     */
    private Boolean syncStatus;

    // TODO @芋艿：可以找一些新的思路
    public static class WxMenuButtonTypeHandler extends AbstractJsonTypeHandler<List<WxMenuButton>> {

        @Override
        protected List<WxMenuButton> parse(String json) {
            return JsonUtils.parseArray(json, WxMenuButton.class);
        }

        @Override
        protected String toJson(List<WxMenuButton> obj) {
            return JsonUtils.toJsonString(obj);
        }

    }
}
