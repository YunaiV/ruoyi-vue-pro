package cn.iocoder.yudao.module.infra.dal.mysql.demo02;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentContactDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 学生联系人 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemoStudentContactMapper extends BaseMapperX<InfraDemoStudentContactDO> {

    default List<InfraDemoStudentContactDO> selectListByStudentId(Long studentId) {
        return selectList(InfraDemoStudentContactDO::getStudentId, studentId);
    }

    default List<InfraDemoStudentContactDO> selectListByStudentId(List<Long> studentIds) {
        return selectList(InfraDemoStudentContactDO::getStudentId, studentIds);
    }

    default int deleteByStudentId(Long studentId) {
        return delete(InfraDemoStudentContactDO::getStudentId, studentId);
    }

}