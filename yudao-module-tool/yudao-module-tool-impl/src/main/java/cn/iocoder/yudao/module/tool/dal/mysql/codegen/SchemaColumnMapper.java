package cn.iocoder.yudao.module.tool.dal.mysql.codegen;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.SchemaColumnDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SchemaColumnMapper extends BaseMapperX<SchemaColumnDO> {

    default List<SchemaColumnDO> selectListByTableName(String tableSchema, String tableName) {
        return selectList(new QueryWrapper<SchemaColumnDO>().eq("table_name", tableName)
                .eq("table_schema", tableSchema)
                .orderByAsc("ordinal_position"));
    }

}
