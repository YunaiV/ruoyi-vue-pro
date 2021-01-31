package cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen;

import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenTableDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ToolCodegenTableMapper extends BaseMapperX<ToolCodegenTableDO> {

    default ToolCodegenTableDO selectByTableName(String tableName) {
        return selectOne(new QueryWrapper<ToolCodegenTableDO>().eq("table_name", tableName));
    }

}
