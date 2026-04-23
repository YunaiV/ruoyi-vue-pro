package cn.iocoder.yudao.module.deepay.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生产记录表 deepay_production
 * PatternAgent 打版结果持久化。
 */
@TableName("deepay_production")
@Data
public class DeepayProductionDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联链码 */
    private String chainCode;

    /** 打版文件路径（.dxf） */
    private String patternFile;

    /** 技术包 URL（tech pack PDF） */
    private String techPack;

    private LocalDateTime createdAt;

}
