package cn.iocoder.yudao.module.mes.service.wm.barcode;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.MesWmBarcodePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.MesWmBarcodeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.barcode.MesWmBarcodeConfigDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.barcode.MesWmBarcodeDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.barcode.MesWmBarcodeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 条码清单 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmBarcodeServiceImpl implements MesWmBarcodeService {

    @Resource
    private MesWmBarcodeMapper barcodeMapper;

    @Resource
    private MesWmBarcodeConfigService barcodeConfigService;

    @Override
    public Long createBarcode(MesWmBarcodeSaveReqVO createReqVO) {
        // 1. 校验数据
        MesWmBarcodeConfigDO config = validateBarcodeSaveData(createReqVO.getId(),
                createReqVO.getBizType(), createReqVO.getBizId());

        // 2. 生成条码内容，并校验唯一性
        String content = generateAndValidateContent(createReqVO.getId(),
                createReqVO.getContent(), config.getContentFormat(), createReqVO.getBizCode());

        // 3. 保存条码记录
        MesWmBarcodeDO barcode = BeanUtils.toBean(createReqVO, MesWmBarcodeDO.class).setContent(content)
                .setConfigId(config.getId()).setFormat(config.getFormat());
        barcodeMapper.insert(barcode);
        return barcode.getId();
    }

    @Override
    public String generateBarcodeContent(Integer bizType, String bizCode) {
        // 1.1 校验参数
        if (bizType == null) {
            throw exception(BARCODE_BIZ_TYPE_NOT_EXISTS);
        }
        if (StrUtil.isBlank(bizCode)) {
            throw exception(BARCODE_BIZ_CODE_NOT_EXISTS);
        }
        // 1.2 查询条码配置
        MesWmBarcodeConfigDO config = barcodeConfigService.validateBarcodeConfigByBizType(bizType);

        // 2. 生成条码内容
        return generateBarcodeContent(config.getContentFormat(), bizCode);
    }

    @Override
    public void updateBarcode(MesWmBarcodeSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateBarcodeExists(updateReqVO.getId());
        // 1.2 校验数据
        MesWmBarcodeConfigDO config = validateBarcodeSaveData(updateReqVO.getId(),
                updateReqVO.getBizType(), updateReqVO.getBizId());

        // 2. 生成条码内容，并校验唯一性
        String content = generateAndValidateContent(updateReqVO.getId(),
                updateReqVO.getContent(), config.getContentFormat(), updateReqVO.getBizCode());

        // 3. 更新（刷新 configId 和 format，确保与最新配置一致）
        MesWmBarcodeDO updateObj = BeanUtils.toBean(updateReqVO, MesWmBarcodeDO.class)
                .setConfigId(config.getId()).setFormat(config.getFormat());
        if (StrUtil.isNotBlank(content)) {
            updateObj.setContent(content);
        }
        barcodeMapper.updateById(updateObj);
    }

    /**
     * 校验条码保存数据：条码配置 + 业务对象唯一性
     *
     * @param id 条码编号（新增时为 null）
     * @param bizType 业务类型
     * @param bizId 业务编号
     * @return 条码配置
     */
    private MesWmBarcodeConfigDO validateBarcodeSaveData(Long id, Integer bizType, Long bizId) {
        // 1. 校验条码配置
        MesWmBarcodeConfigDO config = barcodeConfigService.validateBarcodeConfigByBizType(bizType);
        // 2. 校验业务对象唯一性
        validateBarcodeUnique(id, bizType, bizId);
        return config;
    }

    /**
     * 校验业务对象（bizType + bizId）唯一性
     *
     * @param id 条码编号（新增时为 null，更新时排除自身）
     * @param bizType 业务类型
     * @param bizId 业务编号
     */
    private void validateBarcodeUnique(Long id, Integer bizType, Long bizId) {
        MesWmBarcodeDO barcode = barcodeMapper.selectByBizTypeAndBizId(bizType, bizId);
        if (barcode == null) {
            return;
        }
        if (ObjUtil.notEqual(barcode, id)) {
            throw exception(WM_BARCODE_ALREADY_EXISTS);
        }
    }

    /**
     * 生成条码内容（如果前端未传递则自动生成），并校验内容唯一性
     *
     * @param id 条码编号（新增时为 null，更新时排除自身）
     * @param content 前端传递的条码内容（可为空）
     * @param contentFormat 内容格式模板
     * @param bizCode 业务编码
     * @return 最终条码内容
     */
    private String generateAndValidateContent(Long id, String content, String contentFormat, String bizCode) {
        // 1. 如果前端未传递内容，则自动生成
        if (StrUtil.isBlank(content)) {
            content = generateBarcodeContent(contentFormat, bizCode);
        }
        // 2. 校验条码内容唯一性（排除自身）
        if (StrUtil.isNotBlank(content)) {
            MesWmBarcodeDO contentBarcode = barcodeMapper.selectByContent(content);
            if (contentBarcode != null && ObjUtil.notEqual(contentBarcode.getId(), id)) {
                throw exception(WM_BARCODE_CONTENT_DUPLICATE);
            }
        }
        return content;
    }

    /**
     * 生成条码内容
     *
     * @param contentFormat 内容格式模板
     * @param bizCode 业务编码
     * @return 条码内容
     */
    private String generateBarcodeContent(String contentFormat, String bizCode) {
        if (StrUtil.isBlank(contentFormat)) {
            return bizCode;
        }
        return contentFormat.replace(MesWmBarcodeConfigDO.PLACEHOLDER_BUSINESS_CODE, bizCode);
    }

    @Override
    public void deleteBarcode(Long id) {
        // 校验存在
        validateBarcodeExists(id);
        // 删除
        barcodeMapper.deleteById(id);
    }

    private MesWmBarcodeDO validateBarcodeExists(Long id) {
        MesWmBarcodeDO barcode = barcodeMapper.selectById(id);
        if (barcode == null) {
            throw exception(WM_BARCODE_NOT_EXISTS);
        }
        return barcode;
    }

    @Override
    public MesWmBarcodeDO getBarcode(Long id) {
        return barcodeMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmBarcodeDO> getBarcodePage(MesWmBarcodePageReqVO pageReqVO) {
        return barcodeMapper.selectPage(pageReqVO);
    }

    @Override
    public MesWmBarcodeDO getBarcodeByBizTypeAndBizId(Integer bizType, Long bizId) {
        return barcodeMapper.selectByBizTypeAndBizId(bizType, bizId);
    }

    @Override
    public void autoGenerateBarcode(Integer bizType, Long bizId, String bizCode, String bizName) {
        // 1.1 检查是否配置自动生成
        MesWmBarcodeConfigDO config = barcodeConfigService.getBarcodeConfigByBizType(bizType);
        if (config == null || Boolean.FALSE.equals(config.getAutoGenerateFlag())
                || CommonStatusEnum.isDisable(config.getStatus())) {
            return;
        }
        // 1.2 检查是否已存在条码
        MesWmBarcodeDO existBarcode = barcodeMapper.selectByBizTypeAndBizId(bizType, bizId);
        if (existBarcode != null) {
            return;
        }

        // 2. 创建条码记录
        MesWmBarcodeSaveReqVO createReqVO = new MesWmBarcodeSaveReqVO()
                .setBizType(bizType).setBizId(bizId).setBizCode(bizCode).setBizName(bizName)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        createBarcode(createReqVO);
    }

    @Override
    public long getBarcodeCountByConfigId(Long configId) {
        return barcodeMapper.selectCountByConfigId(configId);
    }

}
