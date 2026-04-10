package cn.iocoder.yudao.module.mes.service.wm.barcode;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.config.MesWmBarcodeConfigPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.config.MesWmBarcodeConfigSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.barcode.MesWmBarcodeConfigDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.barcode.MesWmBarcodeConfigMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 条码配置 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmBarcodeConfigServiceImpl implements MesWmBarcodeConfigService {

    @Resource
    private MesWmBarcodeConfigMapper barcodeConfigMapper;

    @Resource
    @Lazy
    private MesWmBarcodeService barcodeService;

    @Override
    public Long createBarcodeConfig(MesWmBarcodeConfigSaveReqVO createReqVO) {
        // 校验业务类型唯一
        validateBarcodeConfigBizTypeUnique(null, createReqVO.getBizType());

        // 插入
        MesWmBarcodeConfigDO config = BeanUtils.toBean(createReqVO, MesWmBarcodeConfigDO.class);
        barcodeConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    public void updateBarcodeConfig(MesWmBarcodeConfigSaveReqVO updateReqVO) {
        // 校验存在
        validateBarcodeConfigExists(updateReqVO.getId());
        // 校验业务类型唯一
        validateBarcodeConfigBizTypeUnique(updateReqVO.getId(), updateReqVO.getBizType());

        // 更新
        MesWmBarcodeConfigDO updateObj = BeanUtils.toBean(updateReqVO, MesWmBarcodeConfigDO.class);
        barcodeConfigMapper.updateById(updateObj);
    }

    @Override
    public void deleteBarcodeConfig(Long id) {
        // 校验存在
        validateBarcodeConfigExists(id);
        // 校验是否有关联的条码记录
        if (barcodeService.getBarcodeCountByConfigId(id) > 0) {
            throw exception(WM_BARCODE_CONFIG_HAS_BARCODE);
        }

        // 删除
        barcodeConfigMapper.deleteById(id);
    }

    private MesWmBarcodeConfigDO validateBarcodeConfigExists(Long id) {
        MesWmBarcodeConfigDO config = barcodeConfigMapper.selectById(id);
        if (config == null) {
            throw exception(WM_BARCODE_CONFIG_NOT_EXISTS);
        }
        return config;
    }

    private void validateBarcodeConfigBizTypeUnique(Long id, Integer bizType) {
        MesWmBarcodeConfigDO config = barcodeConfigMapper.selectByBizType(bizType);
        if (config == null) {
            return;
        }
        if (ObjUtil.notEqual(config.getId(), id)) {
            throw exception(WM_BARCODE_CONFIG_BIZ_TYPE_DUPLICATE);
        }
    }

    @Override
    public MesWmBarcodeConfigDO getBarcodeConfig(Long id) {
        return barcodeConfigMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmBarcodeConfigDO> getBarcodeConfigPage(MesWmBarcodeConfigPageReqVO pageReqVO) {
        return barcodeConfigMapper.selectPage(pageReqVO);
    }

    @Override
    public MesWmBarcodeConfigDO getBarcodeConfigByBizType(Integer bizType) {
        return barcodeConfigMapper.selectByBizType(bizType);
    }

    @Override
    public MesWmBarcodeConfigDO validateBarcodeConfigByBizType(Integer bizType) {
        MesWmBarcodeConfigDO config = barcodeConfigMapper.selectByBizType(bizType);
        if (config == null) {
            throw exception(WM_BARCODE_CONFIG_NOT_EXISTS);
        }
        return config;
    }

}
