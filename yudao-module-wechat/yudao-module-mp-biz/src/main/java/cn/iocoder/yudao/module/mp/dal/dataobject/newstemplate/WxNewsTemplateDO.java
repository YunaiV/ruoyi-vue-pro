package cn.iocoder.yudao.module.mp.dal.dataobject.newstemplate;

import lombok.*;

import java.util.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 图文消息模板 DO
 *
 * @author 芋道源码
 */
@TableName("wx_news_template")
@KeySequence("wx_news_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxNewsTemplateDO extends BaseDO {

    /**
     * 主键 主键ID
     */
    @TableId
    private Integer id;
    /**
     * 模板名称
     */
    private String tplName;
    /**
     * 是否已上传微信
     */
    private String isUpload;
    /**
     * 素材ID
     */
    private String mediaId;
    /**
     * 微信账号ID
     */
    private String wxAccountId;

}
