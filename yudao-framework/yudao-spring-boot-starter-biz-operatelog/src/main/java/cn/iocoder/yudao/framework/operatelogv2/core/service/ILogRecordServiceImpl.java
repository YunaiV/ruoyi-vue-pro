package cn.iocoder.yudao.framework.operatelogv2.core.service;

import cn.iocoder.yudao.framework.operatelogv2.core.aop.OperateLogV2Aspect;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.ILogRecordService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 操作日志 ILogRecordService 实现类
 *
 * 基于 {@link OperateLogV2Aspect} 实现，记录操作日志
 *
 * @author HUIHUI
 */
@Slf4j
public class ILogRecordServiceImpl implements ILogRecordService {

    @Override
    public void record(LogRecord logRecord) {
        OperateLogV2Aspect.setContent(logRecord); // 操作日志
        OperateLogV2Aspect.addExtra(LogRecordContext.getVariables()); // 扩展信息
        // TODO @puhui999：这里是不是调用 operateLogApi 进行记录哈
    }

    @Override
    public List<LogRecord> queryLog(String bizNo, String type) {
        throw new UnsupportedOperationException("不支持该操作，请使用 OperateLogApi 查询！");
    }

    @Override
    public List<LogRecord> queryLogByBizNo(String bizNo, String type, String subType) {
        throw new UnsupportedOperationException("不支持该操作，请使用 OperateLogApi 查询！");
    }

}
