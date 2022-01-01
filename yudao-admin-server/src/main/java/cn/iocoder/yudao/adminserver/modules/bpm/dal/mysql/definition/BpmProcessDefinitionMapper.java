package cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition;

import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmProcessDefinitionDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BpmProcessDefinitionMapper extends BaseMapper<BpmProcessDefinitionDO> {
}
