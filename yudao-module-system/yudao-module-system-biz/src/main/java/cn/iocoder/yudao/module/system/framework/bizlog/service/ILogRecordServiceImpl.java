package cn.iocoder.yudao.module.system.framework.bizlog.service;

import cn.iocoder.yudao.module.system.api.logger.OperateLogApi;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.service.ILogRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 操作日志 ILogRecordService 实现类
 *
 * 基于 {@link OperateLogApi} 实现，记录操作日志
 *
 * @author HUIHUI
 */
@Slf4j
@RequiredArgsConstructor
public class ILogRecordServiceImpl implements ILogRecordService {

    private final OperateLogApi operateLogApi;

    @Override
    public void record(LogRecord logRecord) {
        log.info("【logRecord】log={}", logRecord);
    }

    @Override
    public List<LogRecord> queryLog(String bizNo, String type) {
        return Collections.emptyList();
    }

    @Override
    public List<LogRecord> queryLogByBizNo(String bizNo, String type, String subType) {
        return Collections.emptyList();
    }
}
