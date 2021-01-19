package cn.iocoder.dashboard.modules.system.service.config.impl;

import cn.iocoder.dashboard.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigUpdateReqVO;
import cn.iocoder.dashboard.modules.system.convert.config.SysConfigConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dao.config.SysConfigMapper;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.config.SysConfigDO;
import cn.iocoder.dashboard.modules.system.enums.config.SysConfigTypeEnum;
import cn.iocoder.dashboard.modules.system.service.config.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;

import static cn.iocoder.dashboard.modules.system.enums.SysErrorCodeConstants.*;

/**
 * 参数配置 Service 实现类
 */
@Service
@Slf4j
public class SysConfigServiceImpl implements SysConfigService {

    @Resource
    private SysConfigMapper configMapper;

    @Override
    public PageResult<SysConfigDO> getConfigPage(SysConfigPageReqVO reqVO) {
        return configMapper.selectPage(reqVO);
    }

    @Override
    public List<SysConfigDO> getConfigList(SysConfigExportReqVO reqVO) {
        return configMapper.selectList(reqVO);
    }

    @Override
    public SysConfigDO getConfig(Long id) {
        return configMapper.selectById(id);
    }

    @Override
    public SysConfigDO getConfigByKey(String key) {
        return configMapper.selectByKey(key);
    }

    @Override
    public Long createConfig(SysConfigCreateReqVO reqVO) {
        // 校验正确性
        checkCreateOrUpdate(null, reqVO.getKey());
        // 插入参数配置
        SysConfigDO config = SysConfigConvert.INSTANCE.convert(reqVO);
        config.setType(SysConfigTypeEnum.CUSTOM.getType());
        configMapper.insert(config);
        return config.getId();
    }

    @Override
    public void updateConfig(SysConfigUpdateReqVO reqVO) {
        // 校验正确性
        checkCreateOrUpdate(reqVO.getId(), null); // 不允许更新 key
        // 更新参数配置
        SysConfigDO updateObj = SysConfigConvert.INSTANCE.convert(reqVO);
        configMapper.updateById(updateObj);
    }

    @Override
    public void deleteConfig(Long id) {
        // 校验配置存在
        SysConfigDO config = checkConfigExists(id);
        // 内置配置，不允许删除
        if (SysConfigTypeEnum.SYSTEM.getType().equals(config.getType())) {
            throw ServiceExceptionUtil.exception(CONFIG_CAN_NOT_DELETE_SYSTEM_TYPE);
        }
        // 删除
        configMapper.deleteById(id);
    }

    private void checkCreateOrUpdate(Long id, String key) {
        // 校验自己存在
        checkConfigExists(id);
        // 校验参数配置 key 的唯一性
        checkConfigKeyUnique(id, key);
    }

    private SysConfigDO checkConfigExists(Long id) {
        if (id == null) {
            return null;
        }
        SysConfigDO config = configMapper.selectById(id);
        if (config == null) {
            throw ServiceExceptionUtil.exception(CONFIG_NOT_FOUND);
        }
        return config;
    }

    private void checkConfigKeyUnique(Long id, String key) {
        SysConfigDO config = configMapper.selectByKey(key);
        if (config == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的参数配置
        if (id == null) {
            throw ServiceExceptionUtil.exception(CONFIG_NAME_DUPLICATE);
        }
        if (!config.getId().equals(id)) {
            throw ServiceExceptionUtil.exception(CONFIG_NAME_DUPLICATE);
        }
    }
}
