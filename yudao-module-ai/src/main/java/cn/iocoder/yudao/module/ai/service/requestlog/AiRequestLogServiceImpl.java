package cn.iocoder.yudao.module.ai.service.requestlog;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.requestlog.vo.AiRequestLogPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.requestlog.vo.AiRequestLogStatisticsRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.requestlog.AiRequestLogDO;
import cn.iocoder.yudao.module.ai.dal.mysql.requestlog.AiRequestLogMapper;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogBizTypeEnum;
import cn.iocoder.yudao.module.ai.service.requestlog.dto.AiRequestLogCreateReqDTO;
import cn.iocoder.yudao.module.ai.util.AiUsageUtils;
import cn.iocoder.yudao.module.ai.util.AiUsageUtils.UsageSnapshot;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI 请求日志 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AiRequestLogServiceImpl implements AiRequestLogService {

    @Resource
    private AiRequestLogMapper requestLogMapper;

    @Override
    public AiRequestLogCreateReqDTO buildCreateReqDTO(String requestId, Long userId, AiRequestLogBizTypeEnum bizType, Long bizId, AiModelDO model) {
        return new AiRequestLogCreateReqDTO().setRequestId(requestId).setUserId(userId)
                .setTenantId(TenantContextHolder.getTenantId())
                .setBizType(bizType.getType()).setBizId(bizId)
                .setPlatform(model.getPlatform()).setModelId(model.getId()).setModel(model.getModel())
                .setBeginTime(LocalDateTime.now()).setResponseMetadata(new LinkedHashMap<>());
    }

    @Override
    public void setCreateReqDTO(AiRequestLogCreateReqDTO createReqDTO, ChatResponse response) {
        if (response == null) {
            return;
        }
        // 流式 chunk 与取消可能并发，按单个请求加锁
        synchronized (createReqDTO) {
            // 已创建请求日志（endTime 已设置），不再设置
            if (createReqDTO.getEndTime() != null) {
                return;
            }
            try {
                // 1. 用量：有效 usage 作为当前整体快照；合计 Token 由 createReqDTO 按输入、输出计算
                // DONE @AI：totalTokens 是不是自己加好了。已由 AiRequestLogCreateReqDTO#getTotalTokens 自己计算，这里只设置输入、输出
                Usage usage = response.getMetadata() != null ? response.getMetadata().getUsage() : null;
                boolean usagePresent = usage != null && !(usage instanceof EmptyUsage);
                if (usagePresent) {
                    UsageSnapshot snapshot = AiUsageUtils.extractUsage(response, createReqDTO.getPlatform());
                    createReqDTO.setInputTokens(snapshot.getInputTokens()).setOutputTokens(snapshot.getOutputTokens());
                }

                // 2. 元数据
                // 2.1 转换本次响应的元数据快照
                Map<String, Object> metadata = AiUsageUtils.extractResponseMetadata(response);
                if (metadata == null) {
                    return;
                }
                Map<String, Object> target = createReqDTO.getResponseMetadata();
                if (target == null) {
                    target = new LinkedHashMap<>();
                    createReqDTO.setResponseMetadata(target);
                }
                // 2.2 usage：有效 usage 整体替换；null、EmptyUsage 不覆盖已有快照
                Object usageNode = metadata.remove(AiUsageUtils.USAGE_KEY);
                if (usagePresent) {
                    target.put(AiUsageUtils.USAGE_KEY, usageNode);
                }
                // 2.3 generations：按 index 合并到同一项
                mergeGenerations(target, metadata.remove(AiUsageUtils.GENERATIONS_KEY));
                // 2.4 快照失败记录：去重追加
                mergeSnapshotErrors(target, metadata.remove(AiUsageUtils.SNAPSHOT_ERRORS_KEY));
                // 2.5 其余字段：按非空值合并
                mergeMap(target, metadata);
            } catch (Exception ex) {
                log.warn("[setCreateReqDTO][requestId({}) 记录响应元数据失败]", createReqDTO.getRequestId(), ex);
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void createRequestLog(AiRequestLogCreateReqDTO createReqDTO) {
        // 1. 设置结束时间，并生成日志：耗时、合计 Token 在保存时计算
        AiRequestLogDO requestLog;
        synchronized (createReqDTO) {
            if (createReqDTO.getEndTime() != null) {
                return;
            }
            LocalDateTime endTime = LocalDateTime.now();
            createReqDTO.setEndTime(endTime);
            requestLog = BeanUtils.toBean(createReqDTO, AiRequestLogDO.class)
                    .setDuration(Duration.between(createReqDTO.getBeginTime(), endTime).toMillis())
                    .setTotalTokens(createReqDTO.getTotalTokens()) // 只有 getter，BeanUtils 不会复制，需要手动设置
                    .setResponseMetadata(MapUtil.isEmpty(createReqDTO.getResponseMetadata()) ? null
                            : createReqDTO.getResponseMetadata());
        }
        // 2.1 异步结束时没有登录上下文，使用请求开始时捕获的用户作为创建者、更新者
        String operator = StrUtil.toStringOrNull(createReqDTO.getUserId());
        requestLog.setCreator(operator).setUpdater(operator);
        // 2.2 按请求开始时捕获的租户插入
        TenantUtils.execute(createReqDTO.getTenantId(), () -> requestLogMapper.insert(requestLog));
    }

    @Override
    public AiRequestLogDO getRequestLog(Long id) {
        return requestLogMapper.selectById(id);
    }

    @Override
    public PageResult<AiRequestLogDO> getRequestLogPage(AiRequestLogPageReqVO pageReqVO) {
        return requestLogMapper.selectPage(pageReqVO);
    }

    @Override
    public AiRequestLogStatisticsRespVO getRequestLogStatistics(AiRequestLogPageReqVO pageReqVO) {
        return requestLogMapper.selectStatistics(pageReqVO);
    }

    /**
     * 合并 generations 元数据
     *
     * 流式每个 chunk 都会带上 generation，按 index 合并到同一项，避免重复追加
     *
     * @param target 合并目标
     * @param value  本次响应的 generations 元数据
     */
    @SuppressWarnings("unchecked")
    private static void mergeGenerations(Map<String, Object> target, Object value) {
        if (!(value instanceof List)) {
            return;
        }
        List<Map<String, Object>> targetGenerations = (List<Map<String, Object>>) target
                .computeIfAbsent(AiUsageUtils.GENERATIONS_KEY, key -> new ArrayList<>());
        for (Object item : (List<Object>) value) {
            if (!(item instanceof Map)) {
                continue;
            }
            // 1. 按 index 查找已有项，不存在则新增
            Map<String, Object> generation = (Map<String, Object>) item;
            Object index = generation.get(AiUsageUtils.GENERATION_INDEX_KEY);
            // DONE @AI：g 是不是不缩写噢？（已改为 targetGeneration）
            Map<String, Object> existing = CollUtil.findOne(targetGenerations,
                    targetGeneration -> Objects.equals(targetGeneration.get(AiUsageUtils.GENERATION_INDEX_KEY), index));
            if (existing == null) {
                existing = new LinkedHashMap<>();
                targetGenerations.add(existing);
            }
            // 2. 合并非空值
            mergeMap(existing, generation);
        }
    }

    /**
     * 合并元数据快照的错误信息
     *
     * @param target 合并目标
     * @param value  本次响应的错误信息列表
     */
    @SuppressWarnings("unchecked")
    private static void mergeSnapshotErrors(Map<String, Object> target, Object value) {
        if (!(value instanceof List) || ((List<Object>) value).isEmpty()) {
            return;
        }
        List<Object> targetErrors = (List<Object>) target.computeIfAbsent(AiUsageUtils.SNAPSHOT_ERRORS_KEY, key -> new ArrayList<>());
        // 流式每个 chunk 可能重复同一错误，去重保存
        for (Object error : (List<Object>) value) {
            if (!targetErrors.contains(error)) {
                targetErrors.add(error);
            }
        }
    }

    /**
     * 合并非空值，避免末尾 chunk 的空值覆盖已有字段
     *
     * @param target 合并目标
     * @param source 合并来源
     */
    @SuppressWarnings("unchecked")
    private static void mergeMap(Map<String, Object> target, Map<String, Object> source) {
        source.forEach((key, value) -> {
            // 1. 空值（null、空字符串、空集合、空 Map）跳过；数字 0、false 仍视为非空
            if (ObjUtil.isEmpty(value)) {
                return;
            }
            // 2. 嵌套 Map 递归合并，其余直接覆盖
            Object existing = target.get(key);
            if (existing instanceof Map && value instanceof Map) {
                mergeMap((Map<String, Object>) existing, (Map<String, Object>) value);
            } else {
                target.put(key, value instanceof Map ? new LinkedHashMap<>((Map<String, Object>) value) : value);
            }
        });
    }

}
