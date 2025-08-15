package cn.iocoder.yudao.module.system.dal.mysql.student;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.student.StudentCourseDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生课程 Mapper
 *
 * @author 京京
 */
@Mapper
public interface StudentCourseMapper extends BaseMapperX<StudentCourseDO> {

    default PageResult<StudentCourseDO> selectPage(PageParam reqVO, Long studentId) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StudentCourseDO>()
            .eq(StudentCourseDO::getStudentId, studentId)
            .orderByDesc(StudentCourseDO::getId));
    }

    default int deleteByStudentId(Long studentId) {
        return delete(StudentCourseDO::getStudentId, studentId);
    }

	default int deleteByStudentIds(List<Long> studentIds) {
	    return deleteBatch(StudentCourseDO::getStudentId, studentIds);
	}

}