package cn.iocoder.yudao.module.huawei.smarthome.service.scenario;

import cn.iocoder.yudao.module.huawei.smarthome.dto.scenario.*;

import javax.validation.Valid;
import java.io.IOException;

/**
 * 华为智能家居 - 场景管理和执行 Service 接口
 *
 * @author Jules
 */
public interface HuaweiScenarioService {

    /**
     * 查询指定空间下所有场景信息摘要
     *
     * @param listReqDTO 包含 spaceId 的请求 DTO
     * @return 场景信息列表
     * @throws IOException 当 API 调用失败时抛出
     */
    ScenarioListRespDTO listScenariosBySpace(@Valid ScenarioListBySpaceReqDTO listReqDTO) throws IOException;

    /**
     * 修改场景 (启用/禁用/修改参数)
     *
     * @param updateReqDTO 修改场景请求 DTO
     * @throws IOException 当 API 调用失败时抛出
     */
    void updateScenario(@Valid ScenarioUpdateReqDTO updateReqDTO) throws IOException;

    /**
     * 执行场景
     *
     * @param triggerReqDTO 执行场景请求 DTO
     * @throws IOException 当 API 调用失败时抛出
     */
    void triggerScenario(@Valid ScenarioTriggerReqDTO triggerReqDTO) throws IOException;

    /**
     * 删除场景
     *
     * @param deleteReqDTO 删除场景请求 DTO
     * @throws IOException 当 API 调用失败时抛出
     */
    void deleteScenario(@Valid ScenarioDeleteReqDTO deleteReqDTO) throws IOException;

    /**
     * 导入单个场景 (异步接口)
     *
     * @param importReqDTO 导入场景请求 DTO
     * @throws IOException 当 API 调用失败时抛出
     */
    void importScenario(@Valid ScenarioImportReqDTO importReqDTO) throws IOException;

    /**
     * 场景个数与 ECA 单元统计
     *
     * @param statisticsReqDTO 统计请求 DTO
     * @return 统计结果
     * @throws IOException 当 API 调用失败时抛出
     */
    ScenarioStatisticsRespDTO getScenarioStatistics(@Valid ScenarioStatisticsReqDTO statisticsReqDTO) throws IOException;

    /**
     * 查询场景实例详情
     *
     * @param detailReqDTO 场景详情请求 DTO
     * @return 场景详情 (ECA 数据)
     * @throws IOException 当 API 调用失败时抛出
     */
    ScenarioDetailRespDTO getScenarioDetail(@Valid ScenarioDetailReqDTO detailReqDTO) throws IOException;

}
