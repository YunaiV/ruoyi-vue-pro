package cn.iocoder.yudao.module.iot.service.ota;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwareCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwarePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwareUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ota.IotOtaFirmwareMapper;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.OTA_FIRMWARE_NOT_EXISTS;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.OTA_FIRMWARE_PRODUCT_VERSION_DUPLICATE;

@Slf4j
@Service
@Validated
public class IotOtaFirmwareServiceImpl implements IotOtaFirmwareService {

    @Resource
    private IotOtaFirmwareMapper otaFirmwareMapper;
    @Lazy
    @Resource
    private IotProductService productService;

    @Override
    public Long createOtaFirmware(IotOtaFirmwareCreateReqVO saveReqVO) {
        // 1. 校验固件产品 + 版本号不能重复
        validateProductAndVersionDuplicate(saveReqVO.getProductId(), saveReqVO.getVersion());

        // 2.1.转化数据格式，准备存储到数据库中
        IotOtaFirmwareDO firmware = BeanUtils.toBean(saveReqVO, IotOtaFirmwareDO.class);
        // 2.2.查询ProductKey
        // TODO @li：productService.getProduct(Convert.toLong(firmware.getProductId())) 放到 1. 后面，先做参考校验。逻辑两段：1）先参数校验；2）构建对象 + 存储
        IotProductDO product = productService.getProduct(Convert.toLong(firmware.getProductId()));
        firmware.setProductKey(Objects.requireNonNull(product).getProductKey());
        // TODO @芋艿: 附件、附件签名等属性的计算
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

    // TODO @li：注释有点冗余
    /**
     * 验证产品和版本号是否重复
     * <p>
     * 该方法用于确保在系统中不存在具有相同产品ID和版本号的固件条目
     * 它通过调用otaFirmwareMapper的selectByProductIdAndVersion方法来查询数据库中是否存在匹配的产品ID和版本号的固件信息
     * 如果查询结果非空且不为null，则抛出异常，提示固件信息已存在，从而避免数据重复
     *
     * @param productId 产品ID，用于数据库查询
     * @param version   版本号，用于数据库查询
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException，则抛出异常，提示固件信息已存在
     */
    private void validateProductAndVersionDuplicate(String productId, String version) {
        // 查询数据库中是否存在具有相同产品ID和版本号的固件信息
        List<IotOtaFirmwareDO> list = otaFirmwareMapper.selectByProductIdAndVersion(productId, version);
        // 如果查询结果非空且不为null，则抛出异常，提示固件信息已存在
        if (CollUtil.isNotEmpty(list)) {
            throw exception(OTA_FIRMWARE_PRODUCT_VERSION_DUPLICATE);
        }
    }

}
