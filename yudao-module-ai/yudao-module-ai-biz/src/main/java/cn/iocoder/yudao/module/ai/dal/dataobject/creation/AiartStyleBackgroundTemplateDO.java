package cn.iocoder.yudao.module.ai.dal.dataobject.creation;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI art商品背景风格 DO
 *
 * @author 芋道源码
 */
@TableName("ai_backgroundtemplate")
@KeySequence("ai_chat_conversation_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiartStyleBackgroundTemplateDO extends BaseDO {
    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 名称
     */
    private String levelFirst;

    /**
     * 名称
     */
    private String levelSecond;
    /**
     * 模版名称
     */
    private String name;
    /**
     * 模版提示词
     */
    private String prompt;
    /**
     * 示例商品
     */
    private String demoProduct;

    /**
     * 示例效果
     */
    private String demoUrl;

    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
