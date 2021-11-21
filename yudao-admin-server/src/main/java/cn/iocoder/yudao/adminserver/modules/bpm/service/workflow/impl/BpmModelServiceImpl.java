package cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.FileResp;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelPageReqVo;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelVO;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants;
import cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.BpmModelService;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * 工作流模型实现
 *
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
    public CommonResult<String> newModel(ModelVO modelVO) {
        try {
            //初始化一个空模型
            Model model = repositoryService.newModel();
            // TODO @Li：name 可以直接赋值过去哈，不用声明一个变量
            String name = Optional.ofNullable(modelVO.getName()).orElse("new-process");
            //设置一些默认信息
            model.setName(name);
            model.setKey(Optional.ofNullable(modelVO.getKey()).orElse("processKey"));
            model.setMetaInfo(JsonUtils.toJsonString(modelVO));
            repositoryService.saveModel(model);
            if (!ObjectUtils.isEmpty(modelVO.getBpmnXml())) {
                repositoryService.addModelEditorSource(model.getId(), modelVO.getBpmnXml().getBytes(StandardCharsets.UTF_8));
            }
            return CommonResult.success(model.getId());
        }catch (Exception e){
            // TODO @Li：这里可以捕获，交给全局么？
            // TODO @Li：异常，是不是 error 比较合适，然后堆栈使用 e 直接打印即可
            log.info("模型创建失败！modelVO = {} e = {} ", modelVO, ExceptionUtils.getStackTrace(e));
            throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_ERROR);
        }
    }

    @Override
    public CommonResult<String> updateModel(ModelVO modelVO) {
        try {
            Model model = repositoryService.getModel(modelVO.getId());
            if (ObjectUtils.isEmpty(model)) {
                throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_EDITOR_SOURCE_NOT_EXISTS);
            }
            // 只能修改名字跟描述
            ModelVO modelCreateVO = JsonUtils.parseObject(model.getMetaInfo(), ModelVO.class);
            if (ObjectUtils.isEmpty(modelCreateVO)) {
                modelCreateVO = new ModelVO();
            }
            modelCreateVO.setName(modelVO.getName());
            modelCreateVO.setDescription(modelVO.getDescription());
            model.setMetaInfo(JsonUtils.toJsonString(modelCreateVO));
            model.setName(modelVO.getName());
            model.setKey(modelVO.getKey());
            // 更新模型
            repositoryService.saveModel(model);

            repositoryService.addModelEditorSource(model.getId(), modelVO.getBpmnXml().getBytes(StandardCharsets.UTF_8));

            return CommonResult.success("保存成功");
        }catch (Exception e){
            log.info("模型更新失败！modelVO = {}", modelVO, e);
            throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_ERROR);
        }
    }

    @Override
    public CommonResult<String> deploy(String modelId) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            if (ObjectUtils.isEmpty(modelData)) {
                throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_EDITOR_SOURCE_NOT_EXISTS);
            }
            byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
            if (bytes == null) {
                throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_EDITOR_SOURCE_NOT_EXISTS);
            }
            // 将xml转换为流
            // TODO @Li：这里是标准逻辑，看看 hutool 有没工具类提供。如果没有，咱自己封装一个
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
    public FileResp exportBpmnXml(String modelId) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            if (ObjectUtils.isEmpty(modelData)) {
                throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_EDITOR_SOURCE_NOT_EXISTS);
            }
            byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());
            FileResp fileResp = new FileResp();
            fileResp.setFileName(String.format("%s.bpmn", Optional.ofNullable(modelData.getName()).orElse("流程图")));
            fileResp.setFileByte(bytes);
            return fileResp;
        } catch (Exception e) {
            log.info("模型部署失败！modelId = {} e = {} ", modelId, ExceptionUtils.getStackTrace(e));
            throw ServiceExceptionUtil.exception(BpmErrorCodeConstants.BPMN_MODEL_ERROR);
        }
    }

    @Override
    public CommonResult<String> deleteModel(String modelId) {
        // TODO @Li：activitie 是逻辑删除么？
        repositoryService.deleteModel(modelId);
        return CommonResult.success("删除成功");
    }

}
