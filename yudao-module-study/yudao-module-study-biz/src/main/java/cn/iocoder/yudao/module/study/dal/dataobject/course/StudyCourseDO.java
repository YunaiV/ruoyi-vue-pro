package cn.iocoder.yudao.module.study.dal.dataobject.course;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("study_course")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyCourseDO extends BaseDO {

    /**
     * 课程ID
     */
    @TableId
    private Long id;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 课程全程
     */
    private String fullName;

    /**
     * 课程封面图标
     */
    private String icon;

    /**
     * 课程排序
     */
    private Integer sort;
}
