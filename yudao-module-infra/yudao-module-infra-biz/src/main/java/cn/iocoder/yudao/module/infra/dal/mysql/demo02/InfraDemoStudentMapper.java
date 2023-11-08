package cn.iocoder.yudao.module.infra.dal.mysql.demo02;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.InfraDemoStudentExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.InfraDemoStudentPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 学生 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemoStudentMapper extends BaseMapperX<InfraDemoStudentDO> {

    default PageResult<InfraDemoStudentDO> selectPage(InfraDemoStudentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InfraDemoStudentDO>()
                .orderByDesc(InfraDemoStudentDO::getId));
    }

    default List<InfraDemoStudentDO> selectList(InfraDemoStudentExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<InfraDemoStudentDO>()
                .orderByDesc(InfraDemoStudentDO::getId));
    }

}