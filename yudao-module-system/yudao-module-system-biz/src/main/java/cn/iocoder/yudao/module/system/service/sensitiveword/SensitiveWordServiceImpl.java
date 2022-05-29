package cn.iocoder.yudao.module.system.service.sensitiveword;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo.SensitiveWordCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo.SensitiveWordExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo.SensitiveWordPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo.SensitiveWordUpdateReqVO;
import cn.iocoder.yudao.module.system.convert.sensitiveword.SensitiveWordConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.sensitiveword.SensitiveWordDO;
import cn.iocoder.yudao.module.system.dal.mysql.sensitiveword.SensitiveWordMapper;
import cn.iocoder.yudao.module.system.mq.producer.sensitiveword.SensitiveWordProducer;
import cn.iocoder.yudao.module.system.util.collection.SimpleTrie;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.SENSITIVE_WORD_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.SENSITIVE_WORD_NOT_EXISTS;

/**
 * 敏感词 Service 实现类
 *
 * @author 永不言败
 */
@Service
@Slf4j
@Validated
public class SensitiveWordServiceImpl implements SensitiveWordService {

    /**
     * 定时执行 {@link #schedulePeriodicRefresh()} 的周期
     * 因为已经通过 Redis Pub/Sub 机制，所以频率不需要高
     */
    private static final long SCHEDULER_PERIOD = 5 * 60 * 1000L;

    /**
     * 敏感词标签缓存
     * key：敏感词编号 {@link SensitiveWordDO#getId()}
     * <p>
     * 这里声明 volatile 修饰的原因是，每次刷新时，直接修改指向
     */
    @Getter
    private volatile Set<String> sensitiveWordTagsCache = Collections.emptySet();

    /**
     * 缓存敏感词的最大更新时间，用于后续的增量轮询，判断是否有更新
     */
    @Getter
    private volatile Date maxUpdateTime;

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    @Resource
    private SensitiveWordProducer sensitiveWordProducer;

    /**
     * 默认的敏感词的字典树，包含所有敏感词
     */
    @Getter
    private volatile SimpleTrie defaultSensitiveWordTrie = new SimpleTrie(Collections.emptySet());
    /**
     * 标签与敏感词的字段数的映射
     */
    @Getter
    private volatile Map<String, SimpleTrie> tagSensitiveWordTries = Collections.emptyMap();

    /**
     * 初始化缓存
     */
    @Override
    @PostConstruct
    public void initLocalCache() {
        // 获取敏感词列表，如果有更新
        List<SensitiveWordDO> sensitiveWordList = loadSensitiveWordIfUpdate(maxUpdateTime);
        if (CollUtil.isEmpty(sensitiveWordList)) {
            return;
        }

        // 写入 sensitiveWordTagsCache 缓存
        Set<String> tags = new HashSet<>();
        sensitiveWordList.forEach(word -> tags.addAll(word.getTags()));
        sensitiveWordTagsCache = tags;
        // 写入 defaultSensitiveWordTrie、tagSensitiveWordTries 缓存
        initSensitiveWordTrie(sensitiveWordList);
        // 写入 maxUpdateTime 最大更新时间
        maxUpdateTime = CollectionUtils.getMaxValue(sensitiveWordList, SensitiveWordDO::getUpdateTime);
        log.info("[initLocalCache][初始化 敏感词 数量为 {}]", sensitiveWordList.size());
    }

    private void initSensitiveWordTrie(List<SensitiveWordDO> wordDOs) {
        // 过滤禁用的敏感词
        wordDOs = CollectionUtils.filterList(wordDOs, word -> word.getStatus().equals(CommonStatusEnum.ENABLE.getStatus()));

        // 初始化默认的 defaultSensitiveWordTrie
        this.defaultSensitiveWordTrie = new SimpleTrie(CollectionUtils.convertList(wordDOs, SensitiveWordDO::getName));

        // 初始化 tagSensitiveWordTries
        Multimap<String, String> tagWords = HashMultimap.create();
        for (SensitiveWordDO word : wordDOs) {
            if (CollUtil.isEmpty(word.getTags())) {
                continue;
            }
            word.getTags().forEach(tag -> tagWords.put(tag, word.getName()));
        }
        // 添加到 tagSensitiveWordTries 中
        Map<String, SimpleTrie> tagSensitiveWordTries = new HashMap<>();
        tagWords.asMap().forEach((tag, words) -> tagSensitiveWordTries.put(tag, new SimpleTrie(words)));
        this.tagSensitiveWordTries = tagSensitiveWordTries;
    }

    @Scheduled(fixedDelay = SCHEDULER_PERIOD, initialDelay = SCHEDULER_PERIOD)
    public void schedulePeriodicRefresh() {
        initLocalCache();
    }

    /**
     * 如果敏感词发生变化，从数据库中获取最新的全量敏感词。
     * 如果未发生变化，则返回空
     *
     * @param maxUpdateTime 当前敏感词的最大更新时间
     * @return 敏感词列表
     */
    private List<SensitiveWordDO> loadSensitiveWordIfUpdate(Date maxUpdateTime) {
        // 第一步，判断是否要更新。
        // 如果更新时间为空，说明 DB 一定有新数据
        if (maxUpdateTime == null) {
            log.info("[loadSensitiveWordIfUpdate][首次加载全量敏感词]");
        } else { // 判断数据库中是否有更新的敏感词
            if (sensitiveWordMapper.selectCountByUpdateTimeGt(maxUpdateTime) == 0) {
                return null;
            }
            log.info("[loadSensitiveWordIfUpdate][增量加载全量敏感词]");
        }
        // 第二步，如果有更新，则从数据库加载所有敏感词
        return sensitiveWordMapper.selectList();
    }

    @Override
    public Long createSensitiveWord(SensitiveWordCreateReqVO createReqVO) {
        // 校验唯一性
        checkSensitiveWordNameUnique(null, createReqVO.getName());
        // 插入
        SensitiveWordDO sensitiveWord = SensitiveWordConvert.INSTANCE.convert(createReqVO);
        sensitiveWordMapper.insert(sensitiveWord);
        // 发送消息，刷新缓存
        sensitiveWordProducer.sendSensitiveWordRefreshMessage();
        return sensitiveWord.getId();
    }

    @Override
    public void updateSensitiveWord(SensitiveWordUpdateReqVO updateReqVO) {
        // 校验唯一性
        checkSensitiveWordExists(updateReqVO.getId());
        checkSensitiveWordNameUnique(updateReqVO.getId(), updateReqVO.getName());
        // 更新
        SensitiveWordDO updateObj = SensitiveWordConvert.INSTANCE.convert(updateReqVO);
        sensitiveWordMapper.updateById(updateObj);
        // 发送消息，刷新缓存
        sensitiveWordProducer.sendSensitiveWordRefreshMessage();
    }

    @Override
    public void deleteSensitiveWord(Long id) {
        // 校验存在
        checkSensitiveWordExists(id);
        // 删除
        sensitiveWordMapper.deleteById(id);
        // 发送消息，刷新缓存
        sensitiveWordProducer.sendSensitiveWordRefreshMessage();
    }

    private void checkSensitiveWordNameUnique(Long id, String name) {
        SensitiveWordDO word = sensitiveWordMapper.selectByName(name);
        if (word == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的敏感词
        if (id == null) {
            throw exception(SENSITIVE_WORD_EXISTS);
        }
        if (!word.getId().equals(id)) {
            throw exception(SENSITIVE_WORD_EXISTS);
        }
    }

    private void checkSensitiveWordExists(Long id) {
        if (sensitiveWordMapper.selectById(id) == null) {
            throw exception(SENSITIVE_WORD_NOT_EXISTS);
        }
    }

    @Override
    public SensitiveWordDO getSensitiveWord(Long id) {
        return sensitiveWordMapper.selectById(id);
    }

    @Override
    public List<SensitiveWordDO> getSensitiveWordList() {
        return sensitiveWordMapper.selectList();
    }

    @Override
    public PageResult<SensitiveWordDO> getSensitiveWordPage(SensitiveWordPageReqVO pageReqVO) {
        return sensitiveWordMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SensitiveWordDO> getSensitiveWordList(SensitiveWordExportReqVO exportReqVO) {
        return sensitiveWordMapper.selectList(exportReqVO);
    }

    @Override
    public Set<String> getSensitiveWordTags() {
        return sensitiveWordTagsCache;
    }

    @Override
    public List<String> validateText(String text, List<String> tags) {
        if (CollUtil.isEmpty(tags)) {
            return defaultSensitiveWordTrie.validate(text);
        }
        // 有标签的情况
        Set<String> result = new HashSet<>();
        tags.forEach(tag -> {
            SimpleTrie trie = tagSensitiveWordTries.get(tag);
            if (trie == null) {
                return;
            }
            result.addAll(trie.validate(text));
        });
        return new ArrayList<>(result);
    }

    @Override
    public boolean isTextValid(String text, List<String> tags) {
        if (CollUtil.isEmpty(tags)) {
            return defaultSensitiveWordTrie.isValid(text);
        }
        // 有标签的情况
        for (String tag : tags) {
            SimpleTrie trie = tagSensitiveWordTries.get(tag);
            if (trie == null) {
                continue;
            }
            if (!trie.isValid(text)) {
                return false;
            }
        }
        return true;
    }

}
