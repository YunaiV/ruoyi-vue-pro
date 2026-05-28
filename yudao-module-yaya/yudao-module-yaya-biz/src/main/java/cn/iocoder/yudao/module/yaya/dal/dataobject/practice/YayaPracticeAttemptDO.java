package cn.iocoder.yudao.module.yaya.dal.dataobject.practice;

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

@TableName(value = "yaya_practice_attempt", autoResultMap = true)
@KeySequence("yaya_practice_attempt_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaPracticeAttemptDO extends BaseDO {

    @TableId
    private Long id;
    private Long memberUserId;
    private Long topicId;
    private String status;
    private String answerText;
    private Integer durationSeconds;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> metadata;

}
