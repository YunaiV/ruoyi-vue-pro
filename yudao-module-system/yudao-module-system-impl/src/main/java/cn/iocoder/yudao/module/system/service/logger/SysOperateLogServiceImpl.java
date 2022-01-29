package cn.iocoder.yudao.module.system.service.logger;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.system.controller.logger.vo.operatelog.SysOperateLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.logger.vo.operatelog.SysOperateLogPageReqVO;
import cn.iocoder.yudao.module.system.convert.logger.SysOperateLogConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.SysOperateLogDO;
import cn.iocoder.yudao.module.system.dal.mysql.logger.SysOperateLogMapper;
import cn.iocoder.yudao.module.system.service.logger.SysOperateLogService;
import cn.iocoder.yudao.module.system.service.user.SysUserService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.framework.operatelog.core.dto.OperateLogCreateReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import static cn.iocoder.yudao.module.system.dal.dataobject.logger.SysOperateLogDO.JAVA_METHOD_ARGS_MAX_LENGTH;
import static cn.iocoder.yudao.module.system.dal.dataobject.logger.SysOperateLogDO.RESULT_MAX_LENGTH;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Service
@Slf4j
public class SysOperateLogServiceImpl implements SysOperateLogService {

    @Resource
    private SysOperateLogMapper operateLogMapper;

    @Resource
    private SysUserService userService;

    @Override
    @Async
    public Future<Boolean> createOperateLogAsync(OperateLogCreateReqDTO reqVO) {
        boolean success = false;
        try {
            SysOperateLogDO logDO = SysOperateLogConvert.INSTANCE.convert(reqVO);
            logDO.setJavaMethodArgs(StrUtils.maxLength(logDO.getJavaMethodArgs(), JAVA_METHOD_ARGS_MAX_LENGTH));
            logDO.setResultData(StrUtils.maxLength(logDO.getResultData(), RESULT_MAX_LENGTH));
            success = operateLogMapper.insert(logDO) == 1;
        } catch (Throwable throwable) {
            // 仅仅打印日志，不对外抛出。原因是，还是要保留现场数据。
            log.error("[createOperateLogAsync][记录操作日志异常，日志为 ({})]", reqVO, throwable);
        }
        return new AsyncResult<>(success);
    }

    @Override
    public PageResult<SysOperateLogDO> getOperateLogPage(SysOperateLogPageReqVO reqVO) {
        // 处理基于用户昵称的查询
        Collection<Long> userIds = null;
        if (StrUtil.isNotEmpty(reqVO.getUserNickname())) {
            userIds = convertSet(userService.getUsersByNickname(reqVO.getUserNickname()), SysUserDO::getId);
            if (CollUtil.isEmpty(userIds)) {
                return PageResult.empty();
            }
        }
        // 查询分页
        return operateLogMapper.selectPage(reqVO, userIds);
    }

    @Override
    public List<SysOperateLogDO> getOperateLogs(SysOperateLogExportReqVO reqVO) {
        // 处理基于用户昵称的查询
        Collection<Long> userIds = null;
        if (StrUtil.isNotEmpty(reqVO.getUserNickname())) {
            userIds = convertSet(userService.getUsersByNickname(reqVO.getUserNickname()), SysUserDO::getId);
            if (CollUtil.isEmpty(userIds)) {
                return Collections.emptyList();
            }
        }
        // 查询列表
        return operateLogMapper.selectList(reqVO, userIds);
    }

}
