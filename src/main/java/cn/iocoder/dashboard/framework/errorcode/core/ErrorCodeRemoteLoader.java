package cn.iocoder.dashboard.framework.errorcode.core;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.modules.system.controller.errorcode.vo.ErrorCodeVO;
import cn.iocoder.dashboard.modules.system.service.errorcode.ErrorCodeService;
import cn.iocoder.dashboard.modules.system.service.errorcode.bo.ErrorCodeBO;
import cn.iocoder.dashboard.util.date.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

public class ErrorCodeRemoteLoader {

    private static final int REFRESH_ERROR_CODE_PERIOD = 60 * 1000;

    private Logger logger = LoggerFactory.getLogger(ErrorCodeRemoteLoader.class);

    /**
     * 应用分组
     */
    private final String group;

    @Resource
    private ErrorCodeService errorCodeService;

    private Date maxUpdateTime;

    public ErrorCodeRemoteLoader(String group) {
        this.group = group;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadErrorCodes() {
        // 从 ErrorCodeRpc 全量加载 ErrorCode 错误码
        CommonResult<List<ErrorCodeVO>> listErrorCodesResult = errorCodeService.listErrorCodes1(group, null);
        listErrorCodesResult.checkError();
        logger.info("[loadErrorCodes][从 group({}) 全量加载到 {} 个 ErrorCode 错误码]", group, listErrorCodesResult.getData().size());
        // 写入到 ServiceExceptionUtil 到
        listErrorCodesResult.getData().forEach(errorCodeVO -> {
            ServiceExceptionUtil.put(errorCodeVO.getCode(), errorCodeVO.getMessage());
            // 记录下更新时间，方便增量更新
            maxUpdateTime = DateUtils.max(maxUpdateTime, errorCodeVO.getUpdateTime());
        });
    }

    @Scheduled(fixedDelay = REFRESH_ERROR_CODE_PERIOD, initialDelay = REFRESH_ERROR_CODE_PERIOD)
    public void refreshErrorCodes() {
        // 从 ErrorCodeRpc 增量加载 ErrorCode 错误码
        // TODO 优化点：假设删除错误码的配置，会存在问题；
        CommonResult<List<ErrorCodeVO>> listErrorCodesResult = errorCodeService.listErrorCodes1(group, maxUpdateTime);
        listErrorCodesResult.checkError();
        if (CollUtil.isEmpty(listErrorCodesResult.getData())) {
            return;
        }
        logger.info("[refreshErrorCodes][从 group({}) 增量加载到 {} 个 ErrorCode 错误码]", group, listErrorCodesResult.getData().size());
        // 写入到 ServiceExceptionUtil 到
        listErrorCodesResult.getData().forEach(errorCodeVO -> {
            ServiceExceptionUtil.put(errorCodeVO.getCode(), errorCodeVO.getMessage());
            // 记录下更新时间，方便增量更新
            maxUpdateTime = DateUtils.max(maxUpdateTime, errorCodeVO.getUpdateTime());
        });
    }

}
