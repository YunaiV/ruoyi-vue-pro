package cn.iocoder.yudao.module.system.api.logger;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.logger.dto.OperateLogCreateReqDTO;
import cn.iocoder.yudao.module.system.api.logger.dto.OperateLogV2RespDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.OperateLogV2DO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.logger.OperateLogService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

/**
 * 操作日志 API 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OperateLogApiImpl implements OperateLogApi {

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private AdminUserService adminUserService;

    @Override
    public void createOperateLog(OperateLogCreateReqDTO createReqDTO) {
        operateLogService.createOperateLog(createReqDTO);
    }

    @Override
    public List<OperateLogV2RespDTO> getOperateLogByModuleAndBizId(String module, Long bizId) {
        List<OperateLogV2DO> logList = operateLogService.getOperateLogByModuleAndBizId(module, bizId);
        if (CollUtil.isEmpty(logList)) {
            return Collections.emptyList();
        }

        // 获取用户
        List<AdminUserDO> userList = adminUserService.getUserList(convertSet(logList, item -> Long.parseLong(item.getCreator())));
        Map<Long, AdminUserDO> userMap = convertMap(userList, AdminUserDO::getId);
        return convertList(logList, item -> {
            OperateLogV2RespDTO bean = BeanUtils.toBean(item, OperateLogV2RespDTO.class);
            findAndThen(userMap, Long.parseLong(item.getCreator()), user -> {
                bean.setCreatorName(user.getNickname());
            });
            return bean;
        });
    }

}
