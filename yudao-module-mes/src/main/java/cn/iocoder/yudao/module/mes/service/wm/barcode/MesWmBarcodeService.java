package cn.iocoder.yudao.module.mes.service.wm.barcode;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.MesWmBarcodePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.barcode.vo.MesWmBarcodeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.barcode.MesWmBarcodeDO;
import jakarta.validation.Valid;

/**
 * MES 条码清单 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmBarcodeService {

    /**
     * 创建条码
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBarcode(@Valid MesWmBarcodeSaveReqVO createReqVO);

    /**
     * 更新条码
     *
     * @param updateReqVO 更新信息
     */
    void updateBarcode(@Valid MesWmBarcodeSaveReqVO updateReqVO);

    /**
     * 删除条码
     *
     * @param id 编号
     */
    void deleteBarcode(Long id);

    /**
     * 获得条码
     *
     * @param id 编号
     * @return 条码
     */
    MesWmBarcodeDO getBarcode(Long id);

    /**
     * 获得条码分页
     *
     * @param pageReqVO 分页参数
     * @return 条码分页
     */
    PageResult<MesWmBarcodeDO> getBarcodePage(MesWmBarcodePageReqVO pageReqVO);

    /**
     * 根据业务类型和业务编号获取条码
     *
     * @param bizType 业务类型
     * @param bizId 业务编号
     * @return 条码
     */
    MesWmBarcodeDO getBarcodeByBizTypeAndBizId(Integer bizType, Long bizId);

    /**
     * 自动生成条码
     *
     * @param bizType 业务类型
     * @param bizId 业务编号
     * @param bizCode 业务编码
     * @param bizName 业务名称
     */
    void autoGenerateBarcode(Integer bizType, Long bizId, String bizCode, String bizName);

    /**
     * 生成条码内容（供前端预览使用）
     *
     * @param bizType 业务类型
     * @param bizCode 业务编码
     * @return 生成的条码内容
     */
    String generateBarcodeContent(Integer bizType, String bizCode);

    /**
     * 根据条码配置编号获取相关联的条码数量
     *
     * @param configId 条码配置编号
     * @return 条码数量
     */
    long getBarcodeCountByConfigId(Long configId);

}
