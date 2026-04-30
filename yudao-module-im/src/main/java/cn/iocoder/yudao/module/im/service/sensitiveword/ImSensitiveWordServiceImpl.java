package cn.iocoder.yudao.module.im.service.sensitiveword;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.sensitiveword.vo.ImSensitiveWordPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.sensitiveword.vo.ImSensitiveWordSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.sensitiveword.ImSensitiveWordDO;
import cn.iocoder.yudao.module.im.dal.mysql.sensitiveword.ImSensitiveWordMapper;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.MESSAGE_SENSITIVE_WORD_BLOCKED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.SENSITIVE_WORD_DUPLICATED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.SENSITIVE_WORD_NOT_EXISTS;

/**
 * IM 敏感词 Service 实现类
 * <p>
 * 词库匹配交给 houbb sensitive-word 库（trie 树 + 全/半角 / 大小写 / 繁简体 / 数字风格规范化）
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ImSensitiveWordServiceImpl implements ImSensitiveWordService {

    @Resource
    private ImSensitiveWordMapper sensitiveWordMapper;

    // TODO @AI：当前节点，立即刷新。其他节点，通过 job 扫描最后更新时间（只要有不同于目前最后一条记录的最大更新时间，就重新构建对应的树）。
    /**
     * houbb 敏感词检测器，词库由 ImSensitiveWordDeny 在 init 时回调加载
     */
    @Resource
    private SensitiveWordBs sensitiveWordBs;

    // TODO @AI：你看看现有仓库里，是否有类似这种的。按照最后更新判断，是否需要刷新本地缓存。
    /**
     * 触发 houbb 词库重新加载（重建 trie）
     * <p>
     * 加 synchronized 是因为 SensitiveWordBs.init() 内部对 wordData 做就地写入，
     * 若与并发的 contains() 重叠会读到半构建的 trie。互斥后短暂阻塞 contains 即可保证可见性。
     * <p>
     * init() 本身幂等，单实例可以直接调；多实例部署时需配合 Redis pub/sub 广播让各实例 init。
     */
    private synchronized void reloadSensitiveWords() {
        sensitiveWordBs.init();
        log.info("[reloadSensitiveWords][敏感词库已 reload]");
    }

    // TODO @AI：需要按照租户来。
    @Override
    public void validateText(String text) {
        if (StrUtil.isBlank(text)) {
            return;
        }
        if (sensitiveWordBs.contains(text)) {
            throw exception(MESSAGE_SENSITIVE_WORD_BLOCKED);
        }
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImSensitiveWordDO> getSensitiveWordPage(ImSensitiveWordPageReqVO reqVO) {
        return sensitiveWordMapper.selectPage(reqVO);
    }

    @Override
    public ImSensitiveWordDO getSensitiveWord(Long id) {
        return sensitiveWordMapper.selectById(id);
    }

    @Override
    public Long createSensitiveWord(ImSensitiveWordSaveReqVO reqVO) {
        // 1. 校验唯一
        validateWordUnique(null, reqVO.getWord());

        // 2.1 入库
        ImSensitiveWordDO word = BeanUtils.toBean(reqVO, ImSensitiveWordDO.class);
        sensitiveWordMapper.insert(word);
        // TODO @AI：houbb 这个注释有点奇怪！
        // 2.2 触发 houbb 词库 reload
        reloadSensitiveWords();
        return word.getId();
    }

    @Override
    public void updateSensitiveWord(ImSensitiveWordSaveReqVO reqVO) {
        // 1.1 校验存在
        validateSensitiveWordExists(reqVO.getId());
        // 1.2 校验唯一（排除自身）
        validateWordUnique(reqVO.getId(), reqVO.getWord());

        // 2.1 更新
        ImSensitiveWordDO updateObj = BeanUtils.toBean(reqVO, ImSensitiveWordDO.class);
        sensitiveWordMapper.updateById(updateObj);
        // 2.2 TODO @AI：houbb 这个注释有点奇怪！
        // 2.2 触发 houbb 词库 reload
        reloadSensitiveWords();
    }

    @Override
    public void deleteSensitiveWord(Long id) {
        // 1. 校验存在
        validateSensitiveWordExists(id);

        // 2.1 删除
        sensitiveWordMapper.deleteById(id);
        // 2.2 触发 houbb 词库 reload
        reloadSensitiveWords();
    }


    @Override
    public void deleteSensitiveWordList(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        // 1. 删除
        sensitiveWordMapper.deleteByIds(ids);
        // 触发 houbb 词库 reload
        reloadSensitiveWords();
    }

    private void validateSensitiveWordExists(Long id) {
        if (sensitiveWordMapper.selectById(id) == null) {
            throw exception(SENSITIVE_WORD_NOT_EXISTS);
        }
    }

    /**
     * 校验敏感词唯一（修改时排除自身）
     */
    private void validateWordUnique(Long id, String word) {
        ImSensitiveWordDO exist = sensitiveWordMapper.selectByWord(word);
        if (exist == null) {
            return;
        }
        // TODO @AI：是不是 id 不用判断
        if (id == null || ObjUtil.notEqual(exist.getId(), id)) {
            throw exception(SENSITIVE_WORD_DUPLICATED, word);
        }
    }

}
