package cn.iocoder.yudao.module.tool.dal.mysql.codegen;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.SchemaTableDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SchemaTableMapper extends BaseMapperX<SchemaTableDO> {

    default List<SchemaTableDO> selectList(Collection<String> tableSchemas, String tableName, String tableComment) {
        return selectList(new QueryWrapperX<SchemaTableDO>().in("table_schema", tableSchemas)
                .likeIfPresent("table_name", tableName)
                .likeIfPresent("table_comment", tableComment));
    }

    default SchemaTableDO selectByTableSchemaAndTableName(String tableSchema, String tableName) {
        return selectOne(new QueryWrapper<SchemaTableDO>().eq("table_schema",tableSchema)
                        .eq("table_name", tableName));
    }

}
