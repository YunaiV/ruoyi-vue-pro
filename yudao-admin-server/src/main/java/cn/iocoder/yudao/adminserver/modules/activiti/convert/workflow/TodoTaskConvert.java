package cn.iocoder.yudao.adminserver.modules.activiti.convert.workflow;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TodoTaskConvert {
    TodoTaskConvert INSTANCE = Mappers.getMapper(TodoTaskConvert.class);
}
