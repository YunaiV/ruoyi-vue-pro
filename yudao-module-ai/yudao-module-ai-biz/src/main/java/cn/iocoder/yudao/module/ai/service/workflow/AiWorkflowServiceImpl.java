package cn.iocoder.yudao.module.ai.service.workflow;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.workflow.vo.AiWorkflowPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.workflow.vo.AiWorkflowSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.workflow.vo.AiWorkflowTestReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.workflow.AiWorkflowDO;
import cn.iocoder.yudao.module.ai.dal.mysql.workflow.AiWorkflowMapper;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import dev.tinyflow.core.Tinyflow;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.WORKFLOW_CODE_EXISTS;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.WORKFLOW_NOT_EXISTS;

/**
 * AI 工作流 Service 实现类
 *
 * @author lesan
 */
@Service
@Slf4j
public class AiWorkflowServiceImpl implements AiWorkflowService {

    @Resource
    private AiWorkflowMapper workflowMapper;

    @Resource
    private AiApiKeyService apiKeyService;

    @Override
    public Long createWorkflow(AiWorkflowSaveReqVO createReqVO) {
        validateWorkflowForCreateOrUpdate(null, createReqVO.getCode());
        AiWorkflowDO workflow = BeanUtils.toBean(createReqVO, AiWorkflowDO.class);
        workflowMapper.insert(workflow);
        return workflow.getId();
    }

    @Override
    public void updateWorkflow(AiWorkflowSaveReqVO updateReqVO) {
        validateWorkflowForCreateOrUpdate(updateReqVO.getId(), updateReqVO.getCode());
        AiWorkflowDO workflow = BeanUtils.toBean(updateReqVO, AiWorkflowDO.class);
        workflowMapper.updateById(workflow);
    }

    @Override
    public void deleteWorkflow(Long id) {
        validateWorkflowExists(id);
        workflowMapper.deleteById(id);
    }

    @Override
    public AiWorkflowDO getWorkflow(Long id) {
        return workflowMapper.selectById(id);
    }

    @Override
    public PageResult<AiWorkflowDO> getWorkflowPage(AiWorkflowPageReqVO pageReqVO) {
        return workflowMapper.selectPage(pageReqVO);
    }

    @Override
    public Object testWorkflow(AiWorkflowTestReqVO testReqVO) {
        Map<String, Object> variables = testReqVO.getParams();
        Tinyflow tinyflow = parseFlowParam(testReqVO.getGraph());
        return tinyflow.toChain().executeForResult(variables);
    }

    private void validateWorkflowForCreateOrUpdate(Long id, String code) {
        validateWorkflowExists(id);
        validateCodeUnique(id, code);
    }

    private void validateWorkflowExists(Long id) {
        if (ObjUtil.isNull(id)) {
            return;
        }
        AiWorkflowDO workflow = workflowMapper.selectById(id);
        if (ObjUtil.isNull(workflow)) {
            throw exception(WORKFLOW_NOT_EXISTS);
        }
    }

    private void validateCodeUnique(Long id, String code) {
        if (StrUtil.isBlank(code)) {
            return;
        }
        AiWorkflowDO workflow = workflowMapper.selectByCode(code);
        if (ObjUtil.isNull(workflow)) {
            return;
        }
        if (ObjUtil.isNull(id)) {
            throw exception(WORKFLOW_CODE_EXISTS);
        }
        if (ObjUtil.notEqual(workflow.getId(), id)) {
            throw exception(WORKFLOW_CODE_EXISTS);
        }
    }

    private Tinyflow parseFlowParam(String graph) {
        // TODO @lesan：可以使用 jackson 哇？
        JSONObject json = JSONObject.parseObject(graph);
        JSONArray nodeArr = json.getJSONArray("nodes");
        Tinyflow tinyflow = new Tinyflow(json.toJSONString());
        for (int i = 0; i < nodeArr.size(); i++) {
            JSONObject node = nodeArr.getJSONObject(i);
            switch (node.getString("type")) {
                case "llmNode":
                    JSONObject data = node.getJSONObject("data");
                    AiApiKeyDO apiKey = apiKeyService.getApiKey(data.getLong("llmId"));
                    switch (apiKey.getPlatform()) {
                        // TODO @lesan 需要讨论一下这里怎么弄
                        // TODO @lesan llmId 对应 model 的编号如何？这样的话，就是 apiModelService 提供一个获取 LLM 的方法。然后，创建的方法，也在 AiModelFactory 提供。可以先接个 deepseek 先。deepseek yyds！
                        case "OpenAI":
                            break;
                        case "Ollama":
                            break;
                        case "YiYan":
                            break;
                        case "XingHuo":
                            break;
                        case "TongYi":
                            break;
                        case "DeepSeek":
                            break;
                        case "ZhiPu":
                            break;
                    }
                    break;
                case "internalNode":
                    break;
                default:
                    break;
            }
        }
        return tinyflow;
    }

}
