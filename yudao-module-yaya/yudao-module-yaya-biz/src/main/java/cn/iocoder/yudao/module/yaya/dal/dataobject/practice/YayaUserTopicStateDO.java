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

import java.time.LocalDateTime;
import java.util.Map;

@TableName(value = "yaya_user_topic_state", autoResultMap = true)
@KeySequence("yaya_user_topic_state_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaUserTopicStateDO extends BaseDO {

    @TableId
    private Long id;
    private Long memberUserId;
    private Long topicId;
    private Boolean practiced;
    private Integer attemptCount;
    private Long lastAttemptId;
    private LocalDateTime lastPracticedAt;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> metadata;

}
