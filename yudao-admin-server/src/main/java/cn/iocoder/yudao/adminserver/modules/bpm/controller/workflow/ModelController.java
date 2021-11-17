package cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.TodoTaskRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelCreateVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.model.ModelUpdateVO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.workflow.BpmModelService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 工作流模型
 * @author yunlongn
 */
@Slf4j
@RestController
@RequestMapping("/workflow/models")
@Api(tags = "工作流模型")
@RequiredArgsConstructor
public class ModelController {

    private final BpmModelService bpmModelService;

    @PostMapping("/create")
    public CommonResult<String> newModel(@RequestBody ModelCreateVO modelCreateVO) {
       return bpmModelService.newModel(modelCreateVO);
    }

    @PostMapping("/update")
    public CommonResult<String> updateModel(@RequestBody ModelUpdateVO modelUpdateVO) {
       return bpmModelService.updateModel(modelUpdateVO);
    }

    @PostMapping("/deploy/{modelId}")
    public CommonResult<String> updateModel(@PathVariable String modelId) {
       return bpmModelService.deploy(modelId);
    }
}
