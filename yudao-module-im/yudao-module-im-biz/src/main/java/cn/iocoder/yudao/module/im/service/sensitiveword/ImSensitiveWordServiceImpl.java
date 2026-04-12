package cn.iocoder.yudao.module.im.service.sensitiveword;

import cn.iocoder.yudao.module.im.dal.dataobject.sensitiveword.ImSensitiveWordDO;
import cn.iocoder.yudao.module.im.dal.mysql.sensitiveword.ImSensitiveWordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.MESSAGE_SENSITIVE_WORD_BLOCKED;

// TODO @AI：需要内存化（加载到内存缓存，避免每次查库）；优先级低，后续优化
/**
 * IM 敏感词 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ImSensitiveWordServiceImpl implements ImSensitiveWordService {

    @Resource
    private ImSensitiveWordMapper imSensitiveWordMapper;

    @Override
    public void validateText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        // 查询所有启用状态的敏感词
        List<ImSensitiveWordDO> enabledWords = imSensitiveWordMapper.selectListByStatus();
        // 逐词匹配
        for (ImSensitiveWordDO wordDO : enabledWords) {
            if (text.contains(wordDO.getWord())) {
                throw exception(MESSAGE_SENSITIVE_WORD_BLOCKED);
            }
        }
    }

}
