package cn.iocoder.yudao.module.bpm.convert.definition;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionRespVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.service.definition.dto.BpmProcessDefinitionCreateReqDTO;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * Bpm 流程定义的 Convert
 *
 * @author yunlong.li
 */
@Mapper
public interface BpmProcessDefinitionConvert {

    BpmProcessDefinitionConvert INSTANCE = Mappers.getMapper(BpmProcessDefinitionConvert.class);


    default PageResult<BpmProcessDefinitionRespVO> buildProcessDefinitionPage(PageResult<ProcessDefinition> page,
                                                                              Map<String, Deployment> deploymentMap,
                                                                              Map<String, BpmProcessDefinitionInfoDO> processDefinitionInfoMap,
                                                                              Map<Long, BpmFormDO> formMap,
                                                                              Map<String, BpmCategoryDO> categoryMap) {
        List<BpmProcessDefinitionRespVO> list = buildProcessDefinitionList(page.getList(), deploymentMap, processDefinitionInfoMap, formMap, categoryMap);
        return new PageResult<>(list, page.getTotal());
    }

    default List<BpmProcessDefinitionRespVO> buildProcessDefinitionList(List<ProcessDefinition> list,
                                                                        Map<String, Deployment> deploymentMap,
                                                                        Map<String, BpmProcessDefinitionInfoDO> processDefinitionInfoMap,
                                                                        Map<Long, BpmFormDO> formMap,
                                                                        Map<String, BpmCategoryDO> categoryMap) {
        return CollectionUtils.convertList(list, definition -> {
            BpmProcessDefinitionRespVO respVO = BeanUtils.toBean(definition, BpmProcessDefinitionRespVO.class);
            respVO.setSuspensionState(definition.isSuspended() ? SuspensionState.SUSPENDED.getStateCode() : SuspensionState.ACTIVE.getStateCode());
            // Deployment
            MapUtils.findAndThen(deploymentMap, definition.getDeploymentId(),
                    deployment -> respVO.setDeploymentTime(LocalDateTimeUtil.of(deployment.getDeploymentTime())));
            // BpmProcessDefinitionInfoDO
            BpmProcessDefinitionInfoDO processDefinitionInfo = MapUtil.get(processDefinitionInfoMap, definition.getId(), BpmProcessDefinitionInfoDO.class);
            if (processDefinitionInfo != null) {
                copyTo(processDefinitionInfo, respVO);
                // Form
                BpmFormDO form = MapUtil.get(formMap, processDefinitionInfo.getFormId(), BpmFormDO.class);
                if (form != null) {
                    respVO.setFormName(form.getName());
                }
            }
            // Category
            MapUtils.findAndThen(categoryMap, definition.getCategory(), category -> respVO.setCategoryName(category.getName()));
            return respVO;
        });
    }

    BpmProcessDefinitionInfoDO convert2(BpmProcessDefinitionCreateReqDTO bean);

    @Mapping(source = "from.id", target = "to.id", ignore = true)
    void copyTo(BpmProcessDefinitionInfoDO from, @MappingTarget BpmProcessDefinitionRespVO to);

}
