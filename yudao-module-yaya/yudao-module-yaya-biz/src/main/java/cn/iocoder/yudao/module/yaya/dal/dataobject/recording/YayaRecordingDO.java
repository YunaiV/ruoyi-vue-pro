package cn.iocoder.yudao.module.yaya.dal.dataobject.recording;

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

@TableName(value = "yaya_recording", autoResultMap = true)
@KeySequence("yaya_recording_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaRecordingDO extends BaseDO {

    @TableId
    private Long id;
    private Long memberUserId;
    private Long topicId;
    private Long questionId;
    private Long attemptId;
    private String storagePath;
    private String mimeType;
    private Long sizeBytes;
    private Double durationSeconds;
    private String checksum;
    private String status;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> metadata;

}
