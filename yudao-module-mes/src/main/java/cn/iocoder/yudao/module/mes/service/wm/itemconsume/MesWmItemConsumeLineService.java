package cn.iocoder.yudao.module.mes.service.wm.itemconsume;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemconsume.vo.MesWmItemConsumeLinePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeLineDO;

import java.util.List;

/**
 * MES 物料消耗记录行 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmItemConsumeLineService {

    /**
     * 批量创建消耗行
     *
     * @param lines 消耗行列表
     */
    void createItemConsumeLineBatch(List<MesWmItemConsumeLineDO> lines);

    /**
     * 根据报工记录编号分页查询消耗行
     *
     * @param pageReqVO 分页查询参数（含 feedbackId）
     * @return 消耗行分页
     */
    PageResult<MesWmItemConsumeLineDO> getItemConsumeLinePage(MesWmItemConsumeLinePageReqVO pageReqVO);

    /**
     * 查询指定消耗记录的所有行
     *
     * @param consumeId 消耗记录编号
     * @return 消耗行列表
     */
    List<MesWmItemConsumeLineDO> getItemConsumeLineListByConsumeId(Long consumeId);

}
