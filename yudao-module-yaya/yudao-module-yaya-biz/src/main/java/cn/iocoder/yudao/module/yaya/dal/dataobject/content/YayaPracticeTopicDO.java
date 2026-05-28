package cn.iocoder.yudao.module.yaya.dal.dataobject.content;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.yaya.dal.typehandler.YayaJsonbTypeHandler;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@TableName(value = "yaya_practice_topic", autoResultMap = true)
@KeySequence("yaya_practice_topic_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaPracticeTopicDO extends BaseDO {

    @TableId
    private Long id;
    private String legacyUuid;
    private Long seasonId;
    private Long sourceSnapshotId;
    private Integer part;
    private String stableKey;
    private Integer topicNo;
    private String titleEn;
    private String titleZh;
    private String topicType;
    private String category;
    private String promptEn;
    private String promptZh;
    private Integer displayOrder;
    private String reviewStatus;
    private String publishStatus;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> metadata;

}
