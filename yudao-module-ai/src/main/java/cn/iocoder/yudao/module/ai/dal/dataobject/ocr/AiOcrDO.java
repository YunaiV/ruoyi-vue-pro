package cn.iocoder.yudao.module.ai.dal.dataobject.ocr;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.ocr.AiOcrStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 文字识别 DO
 *
 * 一次识别一行：成功、失败都记录；重新识别是新的一行，不覆盖已有的修订
 *
 * @author 芋道源码
 */
@TableName("ai_ocr")
@KeySequence("ai_ocr_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiOcrDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     * <p>
     * 关联 AdminUserDO 的 userId 字段
     */
    private Long userId;

    /**
     * 模型编号
     * <p>
     * 关联 {@link AiModelDO#getId()} 字段
     */
    private Long modelId;
    /**
     * 模型名字，冗余 {@link AiModelDO#getName()}
     */
    private String modelName;
    /**
     * 平台，冗余 {@link AiModelDO#getPlatform()}
     */
    private String platform;
    /**
     * 模型标识，冗余 {@link AiModelDO#getModel()}
     */
    private String model;

    /**
     * 图片文件名
     */
    private String fileName;
    /**
     * 图片文件地址
     */
    private String imageUrl;

    /**
     * 识别状态
     * <p>
     * 枚举 {@link AiOcrStatusEnum}
     */
    private Integer status;
    /**
     * 识别出的原文；图片中没有文字时为空字符串
     */
    private String originalText;
    /**
     * 用户修订后的文本
     */
    private String editedText;
    /**
     * 失败原因
     */
    private String errorMessage;
    /**
     * 识别耗时，单位：毫秒
     */
    private Integer duration;

}
