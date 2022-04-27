package cn.iocoder.yudao.module.infra.dal.mysql.codegen;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CodegenColumnMapper extends BaseMapperX<CodegenColumnDO> {

    default List<CodegenColumnDO> selectListByTableId(Long tableId) {
        return selectList(new QueryWrapper<CodegenColumnDO>().eq("table_id", tableId)
                .orderByAsc("ordinal_position"));
    }

    default void deleteListByTableId(Long tableId) {
        delete(new QueryWrapper<CodegenColumnDO>().eq("table_id", tableId));
    }

}
