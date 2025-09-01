package cn.iocoder.yudao.module.iot.service.ota;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwareCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwarePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwareUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ota.IotOtaFirmwareMapper;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.OTA_FIRMWARE_NOT_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.OTA_FIRMWARE_PRODUCT_VERSION_DUPLICATE;

/**
 * OTA 固件管理 Service 实现类
 *
 * @author Shelly Chan
 */
@Service
@Validated
@Slf4j
public class IotOtaFirmwareServiceImpl implements IotOtaFirmwareService {

    @Resource
    private IotOtaFirmwareMapper otaFirmwareMapper;
    @Lazy
    @Resource
    private IotProductService productService;

    @Override
    public Long createOtaFirmware(IotOtaFirmwareCreateReqVO saveReqVO) {
        // 1.1 校验固件产品 + 版本号不能重复
        if (otaFirmwareMapper.selectByProductIdAndVersion(saveReqVO.getProductId(), saveReqVO.getVersion()) != null) {
            throw exception(OTA_FIRMWARE_PRODUCT_VERSION_DUPLICATE);
        }
        // 1.2 校验产品存在
        productService.validateProductExists(saveReqVO.getProductId());

        // 2. 构建对象 + 存储
        IotOtaFirmwareDO firmware = BeanUtils.toBean(saveReqVO, IotOtaFirmwareDO.class);
        // 2.1 计算文件签名等属性
        try {
            calculateFileDigest(firmware);
        } catch (Exception e) {
            log.error("[createOtaFirmware][url({}) 计算文件签名失败]", firmware.getFileUrl(), e);
            throw new RuntimeException("计算文件签名失败: " + e.getMessage());
        }
        otaFirmwareMapper.insert(firmware);
        return firmware.getId();
    }

    @Override
    public void updateOtaFirmware(IotOtaFirmwareUpdateReqVO updateReqVO) {
        // 1. 校验存在
        validateFirmwareExists(updateReqVO.getId());

        // 2. 更新数据
        IotOtaFirmwareDO updateObj = BeanUtils.toBean(updateReqVO, IotOtaFirmwareDO.class);
        otaFirmwareMapper.updateById(updateObj);
    }

    @Override
    public IotOtaFirmwareDO getOtaFirmware(Long id) {
        return otaFirmwareMapper.selectById(id);
    }

    @Override
    public IotOtaFirmwareDO getOtaFirmwareByProductIdAndVersion(Long productId, String version) {
        return otaFirmwareMapper.selectByProductIdAndVersion(productId, version);
    }

    @Override
    public List<IotOtaFirmwareDO> getOtaFirmwareList(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return otaFirmwareMapper.selectByIds(ids);
    }

    @Override
    public PageResult<IotOtaFirmwareDO> getOtaFirmwarePage(IotOtaFirmwarePageReqVO pageReqVO) {
        return otaFirmwareMapper.selectPage(pageReqVO);
    }

    @Override
    public IotOtaFirmwareDO validateFirmwareExists(Long id) {
        IotOtaFirmwareDO firmware = otaFirmwareMapper.selectById(id);
        if (firmware == null) {
            throw exception(OTA_FIRMWARE_NOT_EXISTS);
        }
        return firmware;
    }

    /**
     * 计算文件签名
     *
     * @param firmware 固件对象
     */
    private void calculateFileDigest(IotOtaFirmwareDO firmware) {
        String fileUrl = firmware.getFileUrl();
        // 下载文件并计算签名
        byte[] fileBytes = HttpUtil.downloadBytes(fileUrl);
        // 设置文件大小
        firmware.setFileSize((long) fileBytes.length);
        // 计算 MD5 签名
        firmware.setFileDigestAlgorithm(DigestAlgorithm.MD5.getValue());
        String md5Hex = DigestUtil.digester(firmware.getFileDigestAlgorithm()).digestHex(new ByteArrayInputStream(fileBytes));
        firmware.setFileDigestValue(md5Hex);
    }

}
