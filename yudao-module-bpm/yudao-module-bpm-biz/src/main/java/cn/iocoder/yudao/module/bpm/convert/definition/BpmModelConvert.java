package cn.iocoder.yudao.module.bpm.convert.definition;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModeImportReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelUpdateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.service.definition.dto.BpmModelMetaInfoRespDTO;
import cn.iocoder.yudao.module.bpm.service.definition.dto.BpmProcessDefinitionCreateReqDTO;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
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
        BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
        BpmModelRespVO modelVO = buildModel0(model, metaInfo, null, null, null, null);
        if (ArrayUtil.isNotEmpty(bpmnBytes)) {
            modelVO.setBpmnXml(new String(bpmnBytes));
        }
        return modelVO;
    }

    default BpmModelRespVO buildModel0(Model model,
                                      BpmModelMetaInfoRespDTO metaInfo, BpmFormDO form, BpmCategoryDO category,
                                      Deployment deployment, ProcessDefinition processDefinition) {
        BpmModelRespVO modelRespVO = new BpmModelRespVO().setId(model.getId()).setName(model.getName())
                .setKey(model.getKey()).setCategory(model.getCategory())
                .setCreateTime(DateUtils.of(model.getCreateTime()));
        // Form
        if (metaInfo != null) {
            modelRespVO.setFormType(metaInfo.getFormType()).setFormId(metaInfo.getFormId())
                    .setFormCustomCreatePath(metaInfo.getFormCustomCreatePath())
                    .setFormCustomViewPath(metaInfo.getFormCustomViewPath());
            modelRespVO.setDescription(metaInfo.getDescription());
        }
        if (form != null) {
            modelRespVO.setFormId(form.getId()).setFormName(form.getName());
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

    BpmModelCreateReqVO convert(BpmModeImportReqVO bean);

    default BpmProcessDefinitionCreateReqDTO convert2(Model model, BpmFormDO form) {
        BpmProcessDefinitionCreateReqDTO createReqDTO = new BpmProcessDefinitionCreateReqDTO();
        createReqDTO.setModelId(model.getId());
        createReqDTO.setName(model.getName());
        createReqDTO.setKey(model.getKey());
        createReqDTO.setCategory(model.getCategory());
        BpmModelMetaInfoRespDTO metaInfo = JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class);
        // metaInfo
        copyTo(metaInfo, createReqDTO);
        // form
        if (form != null) {
            createReqDTO.setFormConf(form.getConf());
            createReqDTO.setFormFields(form.getFields());
        }
        return createReqDTO;
    }

    void copyTo(BpmModelMetaInfoRespDTO from, @MappingTarget BpmProcessDefinitionCreateReqDTO to);

    default void copyToCreateModel(Model model, BpmModelCreateReqVO bean) {
        model.setName(bean.getName());
        model.setKey(bean.getKey());
        model.setMetaInfo(buildMetaInfoStr(null, bean.getDescription(), null, null,
                null, null));
    }

    default void copyToUpdateModel(Model model, BpmModelUpdateReqVO bean) {
        model.setName(bean.getName());
        model.setCategory(bean.getCategory());
        model.setMetaInfo(buildMetaInfoStr(JsonUtils.parseObject(model.getMetaInfo(), BpmModelMetaInfoRespDTO.class),
                bean.getDescription(), bean.getFormType(), bean.getFormId(),
                bean.getFormCustomCreatePath(), bean.getFormCustomViewPath()));
    }

    default String buildMetaInfoStr(BpmModelMetaInfoRespDTO metaInfo, String description, Integer formType,
                                    Long formId, String formCustomCreatePath, String formCustomViewPath) {
        if (metaInfo == null) {
            metaInfo = new BpmModelMetaInfoRespDTO();
        }
        // 只有非空，才进行设置，避免更新时的覆盖
        if (StrUtil.isNotEmpty(description)) {
            metaInfo.setDescription(description);
        }
        if (Objects.nonNull(formType)) {
            metaInfo.setFormType(formType);
            metaInfo.setFormId(formId);
            metaInfo.setFormCustomCreatePath(formCustomCreatePath);
            metaInfo.setFormCustomViewPath(formCustomViewPath);
        }
        return JsonUtils.toJsonString(metaInfo);
    }

}
