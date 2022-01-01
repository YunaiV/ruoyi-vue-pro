package cn.iocoder.yudao.adminserver.modules.bpm.convert.model;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelPageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.model.vo.BpmModelUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.form.BpmFormDO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.model.dto.BpmModelMetaInfoRespDTO;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;


/**
 * 流程定义 Convert
 *
 * @author yunlongn
 */
@Mapper
public interface ModelConvert {

    ModelConvert INSTANCE = Mappers.getMapper(ModelConvert.class);

    default List<BpmModelPageItemRespVO> convertList(List<Model> list, Map<Long, BpmFormDO> formMap,
                                                     Map<String, ProcessDefinition> processDefinitionMap) {
        return CollectionUtils.convertList(list, model -> {
            BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
            BpmFormDO form = metaInfo != null ? formMap.get(metaInfo.getFormId()) : null;
            ProcessDefinition processDefinition = model.getDeploymentId() != null ? processDefinitionMap.get(model.getDeploymentId()) : null;
            return convert(model, form, processDefinition);
        });
    }

    default BpmModelPageItemRespVO convert(Model model, BpmFormDO form, ProcessDefinition processDefinition) {
        BpmModelPageItemRespVO modelRespVO = new BpmModelPageItemRespVO();
        modelRespVO.setId(model.getId());
        modelRespVO.setName(model.getName());
        modelRespVO.setKey(model.getKey());
        modelRespVO.setCategory(model.getCategory());
        modelRespVO.setCreateTime(model.getCreateTime());
        BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
        if (metaInfo != null) {
            modelRespVO.setDescription(metaInfo.getDescription());
        }
        if (form != null) {
            modelRespVO.setFormId(form.getId());
            modelRespVO.setFormName(form.getName());
        }
        modelRespVO.setProcessDefinition(this.convert(processDefinition));
        return modelRespVO;
    }

    BpmModelRespVO convert(Model model);

    default void copy(Model model, BpmModelCreateReqVO bean) {
        model.setName(bean.getName());
        model.setKey(bean.getKey());
        model.setCategory(bean.getCategory());
        model.setMetaInfo(JsonUtils.toJsonString(this.buildMetaInfo(bean.getDescription(), bean.getFormId())));
    }

    default void copy(Model model, BpmModelUpdateReqVO bean) {
        model.setName(bean.getName());
        model.setCategory(bean.getCategory());
        model.setMetaInfo(JsonUtils.toJsonString(this.buildMetaInfo(bean.getDescription(), bean.getFormId())));
    }

    default BpmModelMetaInfoRespDTO buildMetaInfo(String description, Long formId) {
        BpmModelMetaInfoRespDTO metaInfo = new BpmModelMetaInfoRespDTO();
        metaInfo.setDescription(description);
        metaInfo.setFormId(formId);
        return metaInfo;
    }

    BpmModelPageItemRespVO.ProcessDefinition convert(ProcessDefinition bean);

}
