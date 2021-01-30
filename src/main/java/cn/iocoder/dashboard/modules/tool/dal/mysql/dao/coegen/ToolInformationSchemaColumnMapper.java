package cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen;

import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolInformationSchemaColumnDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ToolInformationSchemaColumnMapper extends BaseMapperX<ToolInformationSchemaColumnDO> {

    default List<ToolInformationSchemaColumnDO> selectListByTableName(String tableName) {
        return selectList(new QueryWrapper<ToolInformationSchemaColumnDO>().eq("table_name", tableName)
            .orderByAsc("ordinal_position"));
    }

}
