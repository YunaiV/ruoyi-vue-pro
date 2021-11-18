package cn.iocoder.yudao.adminserver.modules.bpm.convert.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.TodoTaskRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelRespVo;
import org.activiti.api.task.model.Task;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


/**
 * @author yunlongn
 */
@Mapper
public interface ModelConvert {
    ModelConvert INSTANCE = Mappers.getMapper(ModelConvert.class);

    ModelRespVo convert(Model model);
}
