package cn.iocoder.yudao.module.mes.service.md.item;

import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.batchconfig.MesMdItemBatchConfigSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemBatchConfigDO;
import jakarta.validation.Valid;

/**
 * MES 物料批次属性配置 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdItemBatchConfigService {

    /**
     * 根据物料编号获取批次属性配置
     *
     * @param itemId 物料编号
     * @return 批次属性配置，不存在则返回 null
     */
    MesMdItemBatchConfigDO getItemBatchConfigByItemId(Long itemId);

    /**
     * 校验物料批次配置存在
     *
     * @param itemId 物料 ID
     * @return 物料批次配置
     */
    MesMdItemBatchConfigDO validateItemBatchConfigExists(Long itemId);

    /**
     * 保存批次属性配置（新增或更新）
     *
     * @param saveReqVO 保存信息
     * @return 配置编号
     */
    Long saveItemBatchConfig(@Valid MesMdItemBatchConfigSaveReqVO saveReqVO);

    /**
     * 根据物料编号删除批次属性配置
     *
     * @param itemId 物料编号
     */
    void deleteItemBatchConfigByItemId(Long itemId);

}
