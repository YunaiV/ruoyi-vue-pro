package cn.iocoder.yudao.module.point.service.pointconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.point.controller.admin.pointconfig.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.pointconfig.PointConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.point.convert.pointconfig.PointConfigConvert;
import cn.iocoder.yudao.module.point.dal.mysql.pointconfig.PointConfigMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.point.enums.ErrorCodeConstants.*;

/**
 * 积分设置 Service 实现类
 *
 * @author QingX
 */
@Service
@Validated
public class PointConfigServiceImpl implements PointConfigService {

    @Autowired
    private PointConfigMapper configMapper;

    @Override
    public Integer createConfig(PointConfigCreateReqVO createReqVO) {
        // 插入
        PointConfigDO config = PointConfigConvert.INSTANCE.convert(createReqVO);
        //每个租户只允许存在一条记录
        validateConfigExistsOne();

        configMapper.insert(config);
        // 返回
        return config.getId();
    }

    @Override
    public void updateConfig(PointConfigUpdateReqVO updateReqVO) {
        // 校验存在
        validateConfigExists(updateReqVO.getId());
        // 更新
        PointConfigDO updateObj = PointConfigConvert.INSTANCE.convert(updateReqVO);
        configMapper.updateById(updateObj);
    }

    @Override
    public void deleteConfig(Integer id) {
        // 校验存在
        validateConfigExists(id);
        // 删除
        configMapper.deleteById(id);
    }

    private void validateConfigExists(Integer id) {
        if (configMapper.selectById(id) == null) {
            throw exception(CONFIG_NOT_EXISTS);
        }
    }

    private void validateConfigExistsOne() {
        if (configMapper.selectCount() > 0) {
            throw exception(CONFIG_EXISTS);
        }
    }

    @Override
    public PointConfigDO getConfig(Integer id) {
        return configMapper.selectById(id);
    }

    @Override
    public List<PointConfigDO> getConfigList(Collection<Integer> ids) {
        return configMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PointConfigDO> getConfigPage(PointConfigPageReqVO pageReqVO) {
        return configMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PointConfigDO> getConfigList(PointConfigExportReqVO exportReqVO) {
        return configMapper.selectList(exportReqVO);
    }

}
