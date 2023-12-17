package cn.iocoder.yudao.module.system.framework.bizlog.service;

import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.system.service.logger.OperateLogService;
import cn.iocoder.yudao.module.system.service.logger.bo.OperateLogV2CreateReqBO;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.service.ILogRecordService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 操作日志 ILogRecordService 实现类
 *
 * 基于 {@link OperateLogService} 实现，记录操作日志
 *
 * @author HUIHUI
 */
@Slf4j
@Service
public class ILogRecordServiceImpl implements ILogRecordService {

    @Resource
    private OperateLogService operateLogService;

    @Override
    public void record(LogRecord logRecord) {
        OperateLogV2CreateReqBO reqBO = new OperateLogV2CreateReqBO();
        // 补全通用字段
        reqBO.setTraceId(TracerUtils.getTraceId());
        // 补充用户信息
        fillUserFields(reqBO);
        // 补全模块信息
        fillModuleFields(reqBO, logRecord);
        // 补全请求信息
        fillRequestFields(reqBO);
        // 异步记录日志
        operateLogService.createOperateLogV2(reqBO);
        log.info("操作日志 ===> {}", reqBO);
    }

    private static void fillUserFields(OperateLogV2CreateReqBO reqBO) {
        reqBO.setUserId(WebFrameworkUtils.getLoginUserId());
        reqBO.setUserType(WebFrameworkUtils.getLoginUserType());
    }

    public static void fillModuleFields(OperateLogV2CreateReqBO reqBO, LogRecord logRecord) {
        reqBO.setModule(logRecord.getType()); // 大模块类型如 crm-客户
        reqBO.setName(logRecord.getSubType());// 操作名称如 转移客户
        reqBO.setBizId(Long.parseLong(logRecord.getBizNo())); // 操作模块业务编号
        reqBO.setContent(logRecord.getAction());// 例如说，修改编号为 1 的用户信息，将性别从男改成女，将姓名从芋道改成源码。
        reqBO.setExtra(logRecord.getExtra()); // 拓展字段，有些复杂的业务，需要记录一些字段 ( JSON 格式 )，例如说，记录订单编号，{ orderId: "1"}
    }

    private static void fillRequestFields(OperateLogV2CreateReqBO reqBO) {
        // 获得 Request 对象
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            return;
        }
        // 补全请求信息
        reqBO.setRequestMethod(request.getMethod());
        reqBO.setRequestUrl(request.getRequestURI());
        reqBO.setUserIp(ServletUtils.getClientIP(request));
        reqBO.setUserAgent(ServletUtils.getUserAgent(request));
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
