package cn.iocoder.yudao.adminserver.modules.bpm.convert.workflow;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelRespVO;
import org.activiti.engine.repository.Model;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


/**
 * @author yunlongn
 */
@Mapper
public interface ModelConvert {
    ModelConvert INSTANCE = Mappers.getMapper(ModelConvert.class);

    BpmModelRespVO convert(Model model);
}
