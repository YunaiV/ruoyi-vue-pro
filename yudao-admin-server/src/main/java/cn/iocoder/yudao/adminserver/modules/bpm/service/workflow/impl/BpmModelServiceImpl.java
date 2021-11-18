package cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelCreateVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelPageReqVo;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelRespVo;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelUpdateVO;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants;
import cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.BpmModelService;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.runtime.shared.query.Pageable;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * 工作流模型实现
 * @author yunlongn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BpmModelServiceImpl implements BpmModelService {

    private final RepositoryService repositoryService;


    @Override
    public PageResult<Model> pageList(ModelPageReqVo modelPageReqVo) {
        ModelQuery modelQuery = repositoryService.createModelQuery();
        String likeName = modelPageReqVo.getName();
        if (StrUtil.isNotBlank(likeName)){
            modelQuery.modelNameLike("%"+likeName+"%");
        }
        List<Model> models = modelQuery.orderByCreateTime().desc()
                .listPage((modelPageReqVo.getPageNo() - 1) * modelPageReqVo.getPageSize(), modelPageReqVo.getPageSize());
        return new PageResult<>(models, modelQuery.count());
    }

    @Override
    public CommonResult<String> newModel(ModelCreateVO modelCreateVO) {
        try {
            //初始化一个空模型
            Model model = repositoryService.newModel();
            String name = Optional.ofNullable(modelCreateVO.getName()).orElse("new-process");
            //设置一些默认信息
            model.setName(name);
            model.setKey(Optional.ofNullable(modelCreateVO.getKey()).orElse("processKey"));
            model.setMetaInfo(JsonUtils.toJsonString(modelCreateVO));
            repositoryService.saveModel(model);
            return CommonResult.success("保存成功");
        }catch (Exception e){
            log.info("模型创建失败！modelCreateVO = {} e = {} ", modelCreateVO, ExceptionUtils.getStackTrace(e));
            throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_ERROR);
        }

    }

    @Override
    public CommonResult<String> updateModel(ModelUpdateVO modelUpdateVO) {
        try {
            Model model = repositoryService.getModel(modelUpdateVO.getId());
            if (ObjectUtils.isEmpty(model)) {
                throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_EDITOR_SOURCE_NOT_EXISTS);
            }
            // 只能修改名字跟描述
            ModelCreateVO modelCreateVO = JsonUtils.parseObject(model.getMetaInfo(), ModelCreateVO.class);
            if (ObjectUtils.isEmpty(modelCreateVO)) {
                modelCreateVO = new ModelCreateVO();
            }
            modelCreateVO.setName(modelUpdateVO.getName());
            modelCreateVO.setDescription(modelUpdateVO.getDescription());
            model.setMetaInfo(JsonUtils.toJsonString(modelCreateVO));
            model.setName(modelUpdateVO.getName());
            // 更新模型
            repositoryService.saveModel(model);

            repositoryService.addModelEditorSource(model.getId(), modelUpdateVO.getBpmnXml().getBytes(StandardCharsets.UTF_8));

            return CommonResult.success("保存成功");
        }catch (Exception e){
            log.info("模型更新失败！modelUpdateVO = {} e = {} ", modelUpdateVO, ExceptionUtils.getStackTrace(e));
            throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_ERROR);
        }
    }

    @Override
    public CommonResult<String> deploy(String modelId) {
        // 获取模型
        Model modelData = repositoryService.getModel(modelId);
        if (ObjectUtils.isEmpty(modelData)) {
            throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_EDITOR_SOURCE_NOT_EXISTS);
        }
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
        if (bytes == null) {
            throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_EDITOR_SOURCE_NOT_EXISTS);
        }
        try {
            // 将xml转换为流
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            XMLInputFactory xif = XMLInputFactory.newInstance();
            InputStreamReader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            XMLStreamReader xtr = xif.createXMLStreamReader(in);
            // 流数据转化为 model
            BpmnModel model = new BpmnXMLConverter().convertToBpmnModel(xtr);
            if(ObjectUtils.isEmpty(model.getProcesses())){
                throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_PROCESS_NOT_EXISTS);
            }
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
            // 部署发布模型流程
            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .addString(processName, new String(bpmnBytes, StandardCharsets.UTF_8))
                    .deploy();
            // 部署成功
            return CommonResult.success(deployment.getId());
        } catch (Exception e) {
            log.info("模型部署失败！modelId = {} e = {} ", modelId, ExceptionUtils.getStackTrace(e));
            throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_ERROR);
        }
    }

    @Override
    public FileResp exportBpmnXml(String deploymentId) {

        // 查询流程定义
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
        if (ObjectUtils.isEmpty(pd)) {
            throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_PROCESS_DEFINITION_NOT_EXISTS);
        }
        String resourceName = Optional.ofNullable(pd.getDiagramResourceName()).orElse(pd.getResourceName());
        InputStream inputStream = repositoryService.getResourceAsStream(deploymentId,
                resourceName);
        FileResp fileResp = new FileResp();

        fileResp.setFileName(String.format("%s.bpmn", Optional.ofNullable(pd.getName()).orElse("流程图")));
        fileResp.setFileByte(IoUtil.readBytes(inputStream));
        return fileResp;
    }

    @Override
    public CommonResult<String> deleteModel(String modelId) {
        repositoryService.deleteModel(modelId);
        return CommonResult.success("删除成功");
    }
}
