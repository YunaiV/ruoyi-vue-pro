package cn.iocoder.yudao.module.study.dal.dataobject.question;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

// TODO
@TableName("study_question")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyQuestionDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 名称
     */
    private String name;

    /**
     * 支付类型
     */
    private Integer payType;

    /**
     * 考试时长，单位秒
     */
    private Integer testDuration;

    /**
     * 分数线
     */
    private Integer passMarker;

    /**
     * 考试规则 TODO
     */
    private String extractingRule;

    /**
     * 排序
     */
    private Integer sort;
}
