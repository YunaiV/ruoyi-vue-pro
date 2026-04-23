package cn.iocoder.yudao.module.im.service.sensitiveword;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.module.im.dal.dataobject.sensitiveword.ImSensitiveWordDO;
import cn.iocoder.yudao.module.im.dal.mysql.sensitiveword.ImSensitiveWordMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.MESSAGE_SENSITIVE_WORD_BLOCKED;

// DONE @AI：已实现内存化，启动时加载 + 定时刷新
/**
 * IM 敏感词 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ImSensitiveWordServiceImpl implements ImSensitiveWordService {

    @Resource
    private ImSensitiveWordMapper imSensitiveWordMapper;

    /**
     * 内存缓存的敏感词列表
     */
    private volatile List<String> sensitiveWords = Collections.emptyList();

    /**
     * 初始化：启动时加载敏感词到内存
     */
    @PostConstruct
    public void init() {
        loadSensitiveWords();
    }

    /**
     * 定时刷新：每 5 分钟重新加载一次
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 5 * 60 * 1000)
    public void refresh() {
        loadSensitiveWords();
    }

    private void loadSensitiveWords() {
        List<ImSensitiveWordDO> enabledWords = imSensitiveWordMapper.selectListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        sensitiveWords = enabledWords.stream()
                .map(ImSensitiveWordDO::getWord)
                .collect(Collectors.toList());
        log.info("[loadSensitiveWords][加载 IM 敏感词 {} 个]", sensitiveWords.size());
    }

    @Override
    public void validateText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        // 内存匹配
        for (String word : sensitiveWords) {
            if (text.contains(word)) {
                throw exception(MESSAGE_SENSITIVE_WORD_BLOCKED);
            }
        }
    }

}
