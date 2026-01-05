package cn.iocoder.yudao.module.mp.dal.dataobject.message;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 公众号模版消息 DO
 *
 * @author dengsl
 */
@TableName("mp_message_template")
@KeySequence("mp_message_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpMessageTemplateDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 公众号账号的编号
     *
     * 关联 {@link MpAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 公众号 appId
     *
     * 冗余 {@link MpAccountDO#getAppId()}
     */
    private String appId;
    /**
     * 公众号模板 ID
     */
    private String templateId;

    /**
     * 标题
     */
    private String title;
    /**
     * 模板内容
     */
    private String content;
    /**
     * 模板示例
     */
    private String example;

    /**
     * 模板所属行业的一级行业
     */
    private String primaryIndustry;
    /**
     * 模板所属行业的二级行业
     */
    private String deputyIndustry;

}