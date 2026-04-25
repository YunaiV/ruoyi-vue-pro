package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * deepay_style_chain — 设计链
 */
@TableName("deepay_style_chain")
@Data
public class DeepayStyleChainDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 6 位唯一链码 */
    private String chainCode;

    /** 原始图片 URL（向后兼容） */
    private String imageUrl;

    /** AI 决策选中的图片（selected_image） */
    private String selectedImage;

    /** 用户输入关键词，REDESIGN 时重新触发流程用 */
    private String keyword;

    /** 打版文件路径（pattern_file） */
    private String patternFile;

    /** AI 决策原因（decision_reason） */
    private String decisionReason;

    /** 完整 Context 决策快照（JSON），用于 AI 决策回溯审计 */
    private String contextSnapshot;

    /** 商品标题 */
    private String title;

    /** 商品描述 */
    private String description;

    /** 商品价格（单位：欧元） */
    private BigDecimal price;

    /** 状态：CREATED / PUBLISHED / STOPPED */
    private String status;

    private LocalDateTime createdAt;

    /** ima 知识库 ID（可选） */
    private String imaKbId;

}
