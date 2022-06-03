package cn.iocoder.yudao.module.mp.dal.dataobject.menu;

import lombok.*;

import java.util.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 微信菜单 DO
 *
 * @author 芋道源码
 */
@TableName("wx_menu")
@KeySequence("wx_menu_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxMenuDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 父ID
     */
    private String parentId;
    /**
     * 菜单名称
     */
    private String menuName;
    /**
     * 菜单类型 1文本消息；2图文消息；3网址链接；4小程序
     */
    private String menuType;
    /**
     * 菜单等级
     */
    private String menuLevel;
    /**
     * 模板ID
     */
    private String tplId;
    /**
     * 菜单URL
     */
    private String menuUrl;
    /**
     * 排序
     */
    private String menuSort;
    /**
     * 微信账号ID
     */
    private String wxAccountId;
    /**
     * 小程序appid
     */
    private String miniprogramAppid;
    /**
     * 小程序页面路径
     */
    private String miniprogramPagepath;

}
