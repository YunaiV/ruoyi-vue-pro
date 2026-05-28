package cn.iocoder.yudao.module.yaya.dal.dataobject.evaluation;

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

@TableName(value = "yaya_evaluation", autoResultMap = true)
@KeySequence("yaya_evaluation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class YayaEvaluationDO extends BaseDO {

    @TableId
    private Long id;
    private Long memberUserId;
    private Long recordingId;
    private Long topicId;
    private Long aiTaskId;
    private String status;
    private String provider;
    private String model;
    private Double scoreOverall;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> scores;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> report;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> textRouteScores;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> audioRouteScores;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> pronRouteScores;
    @TableField(typeHandler = YayaJsonbTypeHandler.class)
    private Map<String, Object> fluencyMetrics;
    private Double bandLower;
    private Double bandUpper;

}
