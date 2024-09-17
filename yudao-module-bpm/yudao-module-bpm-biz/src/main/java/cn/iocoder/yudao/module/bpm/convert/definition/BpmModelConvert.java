package cn.iocoder.yudao.module.bpm.convert.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.util.BpmnModelUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelMetaInfoVO;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 流程模型 Convert
 *
 * @author yunlongn
 */
@Mapper
public interface BpmModelConvert {

    BpmModelConvert INSTANCE = Mappers.getMapper(BpmModelConvert.class);

    default PageResult<BpmModelRespVO> buildModelPage(PageResult<Model> pageResult,
                                                      Map<Long, BpmFormDO> formMap,
                                                      Map<String, BpmCategoryDO> categoryMap, Map<String, Deployment> deploymentMap,
                                                      Map<String, ProcessDefinition> processDefinitionMap) {
        List<BpmModelRespVO> list = CollectionUtils.convertList(pageResult.getList(), model -> {
            BpmModelMetaInfoVO metaInfo = buildMetaInfo(model);
            BpmFormDO form = metaInfo != null ? formMap.get(metaInfo.getFormId()) : null;
            BpmCategoryDO category = categoryMap.get(model.getCategory());
            Deployment deployment = model.getDeploymentId() != null ? deploymentMap.get(model.getDeploymentId()) : null;
            ProcessDefinition processDefinition = model.getDeploymentId() != null ? processDefinitionMap.get(model.getDeploymentId()) : null;
            return buildModel0(model, metaInfo, form, category, deployment, processDefinition);
        });
        return new PageResult<>(list, pageResult.getTotal());
    }

    default BpmModelRespVO buildModel(Model model,
                                     byte[] bpmnBytes) {
        BpmModelMetaInfoVO metaInfo = buildMetaInfo(model);
        BpmModelRespVO modelVO = buildModel0(model, metaInfo, null, null, null, null);
        if (ArrayUtil.isNotEmpty(bpmnBytes)) {
            modelVO.setBpmnXml(BpmnModelUtils.getBpmnXml(bpmnBytes));
        }
        return modelVO;
    }

    default BpmModelRespVO buildModel0(Model model,
                                       BpmModelMetaInfoVO metaInfo, BpmFormDO form, BpmCategoryDO category,
                                       Deployment deployment, ProcessDefinition processDefinition) {
        BpmModelRespVO modelRespVO = new BpmModelRespVO().setId(model.getId()).setName(model.getName())
                .setKey(model.getKey()).setCategory(model.getCategory())
                .setCreateTime(DateUtils.of(model.getCreateTime()));
        // Form
        BeanUtils.copyProperties(metaInfo, modelRespVO);
        if (form != null) {
            modelRespVO.setFormName(form.getName());
        }
        // Category
        if (category != null) {
            modelRespVO.setCategoryName(category.getName());
        }
        // ProcessDefinition
        if (processDefinition != null) {
            modelRespVO.setProcessDefinition(BeanUtils.toBean(processDefinition, BpmProcessDefinitionRespVO.class));
            modelRespVO.getProcessDefinition().setSuspensionState(processDefinition.isSuspended() ?
                    SuspensionState.SUSPENDED.getStateCode() : SuspensionState.ACTIVE.getStateCode());
            if (deployment != null) {
                modelRespVO.getProcessDefinition().setDeploymentTime(DateUtils.of(deployment.getDeploymentTime()));
            }
        }
        return modelRespVO;
    }

    default void copyToModel(Model model, BpmModelSaveReqVO reqVO) {
        model.setName(reqVO.getName());
        model.setKey(reqVO.getKey());
        model.setCategory(reqVO.getCategory());
        model.setMetaInfo(JsonUtils.toJsonString(BeanUtils.toBean(reqVO, BpmModelMetaInfoVO.class)));
    }

    default BpmModelMetaInfoVO buildMetaInfo(Model model) {
        return JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoVO.class);
    }

}
