package cn.iocoder.yudao.module.infra.dal.mysql.demo.demo03;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo03.Demo03GradeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生班级 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Demo03GradeMapper extends BaseMapperX<Demo03GradeDO> {

    default Demo03GradeDO selectByStudentId(Long studentId) {
        return selectOne(Demo03GradeDO::getStudentId, studentId);
    }

    default int deleteByStudentId(Long studentId) {
        return delete(Demo03GradeDO::getStudentId, studentId);
    }

}