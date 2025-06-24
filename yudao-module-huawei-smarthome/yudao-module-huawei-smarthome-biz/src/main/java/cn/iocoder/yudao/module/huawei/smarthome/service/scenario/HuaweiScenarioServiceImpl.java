package cn.iocoder.yudao.module.huawei.smarthome.service.scenario;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.huawei.smarthome.dto.scenario.*;
import cn.iocoder.yudao.module.huawei.smarthome.framework.core.HuaweiSmartHomeAuthClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class HuaweiScenarioServiceImpl implements HuaweiScenarioService {

    @Resource
    private HuaweiSmartHomeAuthClient huaweiSmartHomeAuthClient;

    private static final String SCENARIO_BASE_PATH_FORMAT = "/openapi/asset/v1/space/%s/scenarios"; // 查询列表
    private static final String SCENARIO_UPDATE_PATH_FORMAT = "/openapi/asset/v1/space/%s/scenario"; // 修改
    private static final String SCENARIO_TRIGGER_PATH_FORMAT = "/openapi/asset/v1/space/%s/scenario/%s/trigger"; // 执行
    private static final String SCENARIO_DELETE_PATH_FORMAT = "/openapi/asset/v1/space/%s/scenario/%s"; // 删除
    private static final String SCENARIO_IMPORT_PATH_FORMAT = "/openapi/asset/v1/space/%s/import/scenario"; // 导入
    private static final String SCENARIO_STATISTICS_PATH_FORMAT = "/openapi/asset/v1/space/%s/statistics/scenario"; // 统计
    private static final String SCENARIO_DETAIL_PATH_FORMAT = "/openapi/asset/v1/space/%s/scenario/%s/detail"; // 详情

    @Override
    public ScenarioListRespDTO listScenariosBySpace(ScenarioListBySpaceReqDTO listReqDTO) throws IOException {
        String path = String.format(SCENARIO_BASE_PATH_FORMAT, listReqDTO.getSpaceId());
        String responseBody = huaweiSmartHomeAuthClient.get(path);
        return JsonUtils.parseObject(responseBody, ScenarioListRespDTO.class);
    }

    @Override
    public void updateScenario(ScenarioUpdateReqDTO updateReqDTO) throws IOException {
        String path = String.format(SCENARIO_UPDATE_PATH_FORMAT, updateReqDTO.getSpaceId());
        // 请求体包含 scenarioId, status, datas
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("scenarioId", updateReqDTO.getScenarioId());
        if (updateReqDTO.getStatus() != null) {
            requestBodyMap.put("status", updateReqDTO.getStatus());
        }
        if (updateReqDTO.getDatas() != null && !updateReqDTO.getDatas().isEmpty()) {
            requestBodyMap.put("datas", updateReqDTO.getDatas());
        }
        String requestBody = JsonUtils.toJsonString(requestBodyMap);
        huaweiSmartHomeAuthClient.put(path, requestBody);
    }

    @Override
    public void triggerScenario(ScenarioTriggerReqDTO triggerReqDTO) throws IOException {
        String path = String.format(SCENARIO_TRIGGER_PATH_FORMAT, triggerReqDTO.getSpaceId(), triggerReqDTO.getScenarioId());
        // POST请求，通常没有请求体或请求体为空
        huaweiSmartHomeAuthClient.post(path, "{}"); // 华为文档中未明确说明此接口是否有body，通常触发类POST可以为空body
    }

    @Override
    public void deleteScenario(ScenarioDeleteReqDTO deleteReqDTO) throws IOException {
        String path = String.format(SCENARIO_DELETE_PATH_FORMAT, deleteReqDTO.getSpaceId(), deleteReqDTO.getScenarioId());
        huaweiSmartHomeAuthClient.delete(path);
    }

    @Override
    public void importScenario(ScenarioImportReqDTO importReqDTO) throws IOException {
        String path = String.format(SCENARIO_IMPORT_PATH_FORMAT, importReqDTO.getSpaceId());
        // 请求体是 datas
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("datas", importReqDTO.getDatas());
        String requestBody = JsonUtils.toJsonString(requestBodyMap);
        huaweiSmartHomeAuthClient.post(path, requestBody);
        // 异步接口，结果通过通知回调
    }

    @Override
    public ScenarioStatisticsRespDTO getScenarioStatistics(ScenarioStatisticsReqDTO statisticsReqDTO) throws IOException {
        String path = String.format(SCENARIO_STATISTICS_PATH_FORMAT, statisticsReqDTO.getSpaceId());
        // 文档中请求方法是GET，但示例是POST，这里按文档的GET处理
        String responseBody = huaweiSmartHomeAuthClient.get(path);
        return JsonUtils.parseObject(responseBody, ScenarioStatisticsRespDTO.class);
    }

    @Override
    public ScenarioDetailRespDTO getScenarioDetail(ScenarioDetailReqDTO detailReqDTO) throws IOException {
        String path = String.format(SCENARIO_DETAIL_PATH_FORMAT, detailReqDTO.getSpaceId(), detailReqDTO.getScenarioId());
        // 文档中请求方法是GET，但示例是POST，这里按文档的GET处理
        // 响应体是ECA数据，直接用Map接收
        String responseBody = huaweiSmartHomeAuthClient.get(path);
        Map<String, Object> detailMap = JsonUtils.parseObject(responseBody, Map.class);
        ScenarioDetailRespDTO respDTO = new ScenarioDetailRespDTO();
        respDTO.setDetail(detailMap);
        return respDTO;
    }
}
