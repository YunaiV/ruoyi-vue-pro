package cn.iocoder.yudao.module.mes.service.pro.andon;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.config.MesProAndonConfigPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.config.MesProAndonConfigSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.andon.MesProAndonConfigDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.andon.MesProAndonConfigMapper;
import cn.iocoder.yudao.module.system.api.permission.RoleApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.PRO_ANDON_CONFIG_NOT_EXISTS;

/**
 * MES 安灯呼叫配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProAndonConfigServiceImpl implements MesProAndonConfigService {

    @Resource
    private MesProAndonConfigMapper andonConfigMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private RoleApi roleApi;

    @Override
    public Long createAndonConfig(MesProAndonConfigSaveReqVO createReqVO) {
        // 1. 校验处置人/角色存在
        validateHandlerUserAndRole(createReqVO);

        // 2. 插入
        MesProAndonConfigDO config = BeanUtils.toBean(createReqVO, MesProAndonConfigDO.class);
        andonConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    public void updateAndonConfig(MesProAndonConfigSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateAndonConfigExists(updateReqVO.getId());
        // 1.2 校验处置人/角色存在
        validateHandlerUserAndRole(updateReqVO);

        // 2. 更新
        MesProAndonConfigDO updateObj = BeanUtils.toBean(updateReqVO, MesProAndonConfigDO.class);
        andonConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteAndonConfig(Long id) {
        // 校验存在
        validateAndonConfigExists(id);

        // 删除
        andonConfigMapper.deleteById(id);
    }

    @Override
    public MesProAndonConfigDO validateAndonConfigExists(Long id) {
        MesProAndonConfigDO config = andonConfigMapper.selectById(id);
        if (config == null) {
            throw exception(PRO_ANDON_CONFIG_NOT_EXISTS);
        }
        return config;
    }

    @Override
    public MesProAndonConfigDO getAndonConfig(Long id) {
        return andonConfigMapper.selectById(id);
    }

    @Override
    public PageResult<MesProAndonConfigDO> getAndonConfigPage(MesProAndonConfigPageReqVO pageReqVO) {
        return andonConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesProAndonConfigDO> getAndonConfigList() {
        return andonConfigMapper.selectList();
    }

    // ==================== 校验方法 ====================

    private void validateHandlerUserAndRole(MesProAndonConfigSaveReqVO reqVO) {
        if (reqVO.getHandlerUserId() != null) {
            adminUserApi.validateUser(reqVO.getHandlerUserId());
        }
        if (reqVO.getHandlerRoleId() != null) {
            roleApi.validRoleList(Collections.singleton(reqVO.getHandlerRoleId()));
        }
    }

}
