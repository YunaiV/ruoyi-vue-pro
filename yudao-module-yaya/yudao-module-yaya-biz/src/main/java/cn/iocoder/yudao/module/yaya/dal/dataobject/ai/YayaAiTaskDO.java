package cn.iocoder.yudao.module.yaya.dal.dataobject.ai;

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

@TableName(value = "yaya_ai_task", autoResultMap = true)
@KeySequence("yaya_ai_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaAiTaskDO extends BaseDO {

    @TableId
    private Long id;
    private Long memberUserId;
    private Long recordingId;
    private Long topicId;
    private String taskKey;
    private String taskType;
    private String status;
    @TableField(value = "request_payload", typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> request;
    @TableField(value = "response_payload", typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> response;
    @TableField(value = "result_payload", typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> result;
    @TableField(value = "error_payload", typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> error;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;

}
