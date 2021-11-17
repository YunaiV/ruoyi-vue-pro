package cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelCreateVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelUpdateVO;
import cn.iocoder.yudao.adminserver.modules.bpm.enums.BpmErrorCodeConstants;
import cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.BpmModelService;
import cn.iocoder.yudao.adminserver.modules.tool.framework.errorcode.config.ErrorCodeProperties;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static cn.iocoder.yudao.adminserver.modules.system.enums.SysErrorCodeConstants.NOTICE_NOT_FOUND;

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
            return CommonResult.success("success");
        }catch (Exception e){
            log.info("模型创建失败！modelCreateVO = {} e = {} ", modelCreateVO, ExceptionUtils.getStackTrace(e));
        }

        return CommonResult.success("success");
    }

    @Override
    public CommonResult<String> updateModel(ModelUpdateVO modelUpdateVO) {
        try {
            Model model = repositoryService.getModel(modelUpdateVO.getId());

            // 只能修改名字跟描述
            ModelCreateVO modelCreateVO = JsonUtils.parseObject(model.getMetaInfo(), ModelCreateVO.class);
            modelCreateVO.setName(modelUpdateVO.getName());
            modelCreateVO.setDescription(modelUpdateVO.getDescription());
            model.setMetaInfo(JsonUtils.toJsonString(modelCreateVO));
            model.setName(modelUpdateVO.getName());

            repositoryService.saveModel(model);

            repositoryService.addModelEditorSource(model.getId(), modelUpdateVO.getBpmnXml().getBytes(StandardCharsets.UTF_8));

            return CommonResult.success("success");
        }catch (Exception e){
            log.info("模型修改失败！modelCreateVO = {} e = {} ", modelUpdateVO, ExceptionUtils.getStackTrace(e));
        }

        return CommonResult.success("success");
    }

    @Override
    public CommonResult<String> deploy(String modelId) {
        // 获取模型
        Model modelData = repositoryService.getModel(modelId);
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
            BpmnModel model = new BpmnXMLConverter().convertToBpmnModel(xtr);
            log.info("model ={} ", model);
        } catch (Exception e) {
            log.error("e {}", e);
        }

        return CommonResult.success("success");
    }
}
