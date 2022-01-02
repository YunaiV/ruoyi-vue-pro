package cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition;

import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmProcessDefinitionDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface BpmProcessDefinitionMapper extends BaseMapper<BpmProcessDefinitionDO> {

    default List<BpmProcessDefinitionDO> selectListByProcessDefinitionIds(Collection<String> processDefinitionIds) {
        return selectList(new QueryWrapper<BpmProcessDefinitionDO>().in("process_definition_id", processDefinitionIds));
    }

}
