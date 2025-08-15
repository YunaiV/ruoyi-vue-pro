package cn.iocoder.yudao.module.system.dal.mysql.student;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.student.StudentDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.system.controller.admin.student.vo.*;

/**
 * 学生主子表测试 Mapper
 *
 * @author 京京
 */
@Mapper
public interface StudentMapper extends BaseMapperX<StudentDO> {

    default PageResult<StudentDO> selectPage(StudentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StudentDO>()
                .likeIfPresent(StudentDO::getName, reqVO.getName())
                .eqIfPresent(StudentDO::getBirthday, reqVO.getBirthday())
                .eqIfPresent(StudentDO::getDescription, reqVO.getDescription())
                .betweenIfPresent(StudentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(StudentDO::getId));
    }

}