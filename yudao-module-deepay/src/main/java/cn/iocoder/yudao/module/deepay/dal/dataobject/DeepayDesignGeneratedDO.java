package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 生成原创设计图记录表 — DesignGenAgent 每次生成落库，记录 prompt + 图片 URL。
 */
@Data
@TableName("deepay_design_generated")
public class DeepayDesignGeneratedDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码 */
    private String chainCode;

    /** 生成图片 URL */
    private String imageUrl;

    /** 生成时使用的完整 Prompt */
    private String prompt;

    /** 版本号（同 chainCode 下第几次生成，从 1 开始） */
    private Integer version;

    private LocalDateTime createdAt;
}
