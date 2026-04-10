package cn.iocoder.yudao.module.mes.service.wm.barcode;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.config.MesWmBarcodeConfigPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.config.MesWmBarcodeConfigSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.barcode.MesWmBarcodeConfigDO;
import jakarta.validation.Valid;

/**
 * MES 条码配置 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmBarcodeConfigService {

    /**
     * 创建条码配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBarcodeConfig(@Valid MesWmBarcodeConfigSaveReqVO createReqVO);

    /**
     * 更新条码配置
     *
     * @param updateReqVO 更新信息
     */
    void updateBarcodeConfig(@Valid MesWmBarcodeConfigSaveReqVO updateReqVO);

    /**
     * 删除条码配置
     *
     * @param id 编号
     */
    void deleteBarcodeConfig(Long id);

    /**
     * 获得条码配置
     *
     * @param id 编号
     * @return 条码配置
     */
    MesWmBarcodeConfigDO getBarcodeConfig(Long id);

    /**
     * 获得条码配置分页
     *
     * @param pageReqVO 分页参数
     * @return 条码配置分页
     */
    PageResult<MesWmBarcodeConfigDO> getBarcodeConfigPage(MesWmBarcodeConfigPageReqVO pageReqVO);

    /**
     * 根据业务类型获取条码配置
     *
     * @param bizType 业务类型
     * @return 条码配置
     */
    MesWmBarcodeConfigDO getBarcodeConfigByBizType(Integer bizType);

    /**
     * 校验条码配置存在（根据业务类型）
     *
     * @param bizType 业务类型
     * @return 条码配置
     */
    MesWmBarcodeConfigDO validateBarcodeConfigByBizType(Integer bizType);

}
