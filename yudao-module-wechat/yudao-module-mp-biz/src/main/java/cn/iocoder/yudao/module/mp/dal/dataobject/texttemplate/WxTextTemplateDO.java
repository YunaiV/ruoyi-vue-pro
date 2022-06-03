package cn.iocoder.yudao.module.mp.dal.dataobject.texttemplate;

import lombok.*;

import java.util.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 文本模板 DO
 *
 * @author 芋道源码
 */
@TableName("wx_text_template")
@KeySequence("wx_text_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxTextTemplateDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 模板名字
     */
    private String tplName;
    /**
     * 模板内容
     */
    private String content;

}
