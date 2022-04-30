package cn.iocoder.yudao.module.infra.dal.mysql.codegen;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table.CodegenTablePageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CodegenTableMapper extends BaseMapperX<CodegenTableDO> {

    default CodegenTableDO selectByTableNameAndDataSourceConfigId(String tableName, Long dataSourceConfigId) {
        return selectOne(CodegenTableDO::getTableName, tableName,
                CodegenTableDO::getDataSourceConfigId, dataSourceConfigId);
    }

    default PageResult<CodegenTableDO> selectPage(CodegenTablePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new QueryWrapperX<CodegenTableDO>()
                .likeIfPresent("table_name", pageReqVO.getTableName())
                .likeIfPresent("table_comment", pageReqVO.getTableComment())
                .betweenIfPresent("create_time", pageReqVO.getBeginCreateTime(), pageReqVO.getEndCreateTime()));
    }

    default List<CodegenTableDO> selectListByDataSourceConfigId(Long dataSourceConfigId) {
        return selectList(CodegenTableDO::getDataSourceConfigId, dataSourceConfigId);
    }

}
