package cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen;

import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolInformationSchemaTableDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ToolInformationSchemaTableMapper extends BaseMapperX<ToolInformationSchemaTableDO> {

    default List<ToolInformationSchemaTableDO> selectListByTableSchema(String tableSchema) {
        return selectList(new QueryWrapper<ToolInformationSchemaTableDO>().eq("table_schema", tableSchema));
    }

}
