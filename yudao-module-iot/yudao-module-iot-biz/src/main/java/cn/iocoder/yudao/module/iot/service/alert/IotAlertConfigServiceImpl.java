package cn.iocoder.yudao.module.iot.service.alert;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.IotAlertConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.IotAlertConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import cn.iocoder.yudao.module.iot.dal.mysql.alert.IotAlertConfigMapper;
import cn.iocoder.yudao.module.iot.service.rule.scene.IotRuleSceneService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.ALERT_CONFIG_NOT_EXISTS;

/**
 * IoT 告警配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class IotAlertConfigServiceImpl implements IotAlertConfigService {

    @Resource
    private IotAlertConfigMapper alertConfigMapper;

    @Resource
    private IotRuleSceneService ruleSceneService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public Long createAlertConfig(IotAlertConfigSaveReqVO createReqVO) {
        // 校验关联数据是否存在
        ruleSceneService.validateRuleSceneList(createReqVO.getSceneRuleIds());
        adminUserApi.validateUserList(createReqVO.getReceiveUserIds());

        IotAlertConfigDO alertConfig = BeanUtils.toBean(createReqVO, IotAlertConfigDO.class);
        alertConfigMapper.insert(alertConfig);
        return alertConfig.getId();
    }

    @Override
    public void updateAlertConfig(IotAlertConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateAlertConfigExists(updateReqVO.getId());
        // 校验关联数据是否存在
        ruleSceneService.validateRuleSceneList(updateReqVO.getSceneRuleIds());
        adminUserApi.validateUserList(updateReqVO.getReceiveUserIds());

        // 更新
        IotAlertConfigDO updateObj = BeanUtils.toBean(updateReqVO, IotAlertConfigDO.class);
        alertConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteAlertConfig(Long id) {
        // 校验存在
        validateAlertConfigExists(id);
        // 删除
        alertConfigMapper.deleteById(id);
    }

    private void validateAlertConfigExists(Long id) {
        if (alertConfigMapper.selectById(id) == null) {
            throw exception(ALERT_CONFIG_NOT_EXISTS);
        }
    }

    @Override
    public IotAlertConfigDO getAlertConfig(Long id) {
        return alertConfigMapper.selectById(id);
    }

    @Override
    public PageResult<IotAlertConfigDO> getAlertConfigPage(IotAlertConfigPageReqVO pageReqVO) {
        return alertConfigMapper.selectPage(pageReqVO);
    }

}