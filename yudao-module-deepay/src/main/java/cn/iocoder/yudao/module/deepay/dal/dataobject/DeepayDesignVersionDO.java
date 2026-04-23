package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设计版本表 deepay_design_version
 * 记录每次 DesignAgent 输出的图片，支持版本追溯。
 */
@TableName("deepay_design_version")
@Data
public class DeepayDesignVersionDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码 */
    private String chainCode;

    /** 图片 URL */
    private String imageUrl;

    /** 版本号（从 1 开始，每次 REDESIGN +1） */
    private Integer version;

    /** 是否被 AIDecisionAgent 选中 */
    private Boolean selected;

    private LocalDateTime createdAt;

}
