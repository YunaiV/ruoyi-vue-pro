package cn.iocoder.yudao.module.infra.dal.mysql.demo11;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo11.InfraDemo11StudentContactDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生联系人 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemo11StudentContactMapper extends BaseMapperX<InfraDemo11StudentContactDO> {

    default List<InfraDemo11StudentContactDO> selectListByStudentId(Long studentId) {
        return selectList(InfraDemo11StudentContactDO::getStudentId, studentId);
    }

    default int deleteByStudentId(Long studentId) {
        return delete(InfraDemo11StudentContactDO::getStudentId, studentId);
    }

}