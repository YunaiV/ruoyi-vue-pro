package cn.iocoder.dashboard.modules.tool.dal.mysql.codegen;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolSchemaTableDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ToolSchemaTableMapper extends BaseMapperX<ToolSchemaTableDO> {

    default List<ToolSchemaTableDO> selectList(Collection<String> tableSchemas, String tableName, String tableComment) {
        return selectList(new QueryWrapperX<ToolSchemaTableDO>().in("table_schema", tableSchemas)
                .likeIfPresent("table_name", tableName)
                .likeIfPresent("table_comment", tableComment));
    }

    default ToolSchemaTableDO selectByTableSchemaAndTableName(String tableSchema, String tableName) {
        return selectOne(new QueryWrapper<ToolSchemaTableDO>().eq("table_schema",tableSchema)
                        .eq("table_name", tableName));
    }

}
