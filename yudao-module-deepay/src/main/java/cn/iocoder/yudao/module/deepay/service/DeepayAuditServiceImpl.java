package cn.iocoder.yudao.module.deepay.service;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayLogDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 审计日志服务实现。
 *
 * <p>每条记录异步写入 deepay_log，主流程中调用不会因日志失败而回滚。</p>
 */
@Slf4j
@Service
public class DeepayAuditServiceImpl implements DeepayAuditService {

    @Resource
    private DeepayLogMapper deepayLogMapper;

    @Override
    public void log(String chainCode, String action, String before, String after) {
        try {
            DeepayLogDO entry = new DeepayLogDO();
            entry.setChainCode(chainCode);
            entry.setAction(action);
            entry.setBefore(before);
            entry.setAfter(after);
            entry.setTime(LocalDateTime.now());
            deepayLogMapper.insert(entry);
        } catch (Exception e) {
            // 审计日志写入失败不影响主流程，仅打印警告
            log.warn("[Audit] 写入审计日志失败 chainCode={} action={}", chainCode, action, e);
        }
    }

}
