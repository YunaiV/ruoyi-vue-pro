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

@TableName(value = "yaya_topic_relation", autoResultMap = true)
@KeySequence("yaya_topic_relation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaTopicRelationDO extends BaseDO {

    @TableId
    private Long id;
    private Long sourceTopicId;
    private Long targetTopicId;
    private String relationType;
    private String confidence;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> metadata;

}
