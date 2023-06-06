package cn.iocoder.yudao.module.jl.service.laboratory;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.laboratory.vo.*;
import cn.iocoder.yudao.module.jl.entity.laboratory.ChargeItem;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 实验收费项 Service 接口
 *
 */
public interface ChargeItemService {

    /**
     * 创建实验收费项
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createChargeItem(@Valid ChargeItemCreateReqVO createReqVO);

    /**
     * 更新实验收费项
     *
     * @param updateReqVO 更新信息
     */
    void updateChargeItem(@Valid ChargeItemUpdateReqVO updateReqVO);

    /**
     * 删除实验收费项
     *
     * @param id 编号
     */
    void deleteChargeItem(Long id);

    /**
     * 获得实验收费项
     *
     * @param id 编号
     * @return 实验收费项
     */
    Optional<ChargeItem> getChargeItem(Long id);

    /**
     * 获得实验收费项列表
     *
     * @param ids 编号
     * @return 实验收费项列表
     */
    List<ChargeItem> getChargeItemList(Collection<Long> ids);

    /**
     * 获得实验收费项分页
     *
     * @param pageReqVO 分页查询
     * @return 实验收费项分页
     */
    PageResult<ChargeItem> getChargeItemPage(ChargeItemPageReqVO pageReqVO, ChargeItemPageOrder orderV0);

    /**
     * 获得实验收费项列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 实验收费项列表
     */
    List<ChargeItem> getChargeItemList(ChargeItemExportReqVO exportReqVO);

}
