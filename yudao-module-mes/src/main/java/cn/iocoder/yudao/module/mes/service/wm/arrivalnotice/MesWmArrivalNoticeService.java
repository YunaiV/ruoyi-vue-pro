package cn.iocoder.yudao.module.mes.service.wm.arrivalnotice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.MesWmArrivalNoticePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.MesWmArrivalNoticeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 到货通知单 Service 接口
 */
public interface MesWmArrivalNoticeService {

    /**
     * 创建到货通知单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createArrivalNotice(@Valid MesWmArrivalNoticeSaveReqVO createReqVO);

    /**
     * 修改到货通知单
     *
     * @param updateReqVO 修改信息
     */
    void updateArrivalNotice(@Valid MesWmArrivalNoticeSaveReqVO updateReqVO);

    /**
     * 删除到货通知单（级联删除行）
     *
     * @param id 编号
     */
    void deleteArrivalNotice(Long id);

    /**
     * 获得到货通知单
     *
     * @param id 编号
     * @return 到货通知单
     */
    MesWmArrivalNoticeDO getArrivalNotice(Long id);

    /**
     * 获得到货通知单分页
     *
     * @param pageReqVO 分页参数
     * @return 到货通知单分页
     */
    PageResult<MesWmArrivalNoticeDO> getArrivalNoticePage(MesWmArrivalNoticePageReqVO pageReqVO);

    /**
     * 提交到货通知单（草稿 → 待质检/待入库）
     *
     * @param id 编号
     */
    void submitArrivalNotice(Long id);

    /**
     * IQC 检验完成后回写到货通知单（更新行 + 尝试推进主表状态）
     *
     * @param id 到货通知单编号
     * @param lineId 到货通知单行编号
     * @param iqcId IQC 检验单编号
     * @param qualifiedQuantity 合格数量
     */
    void updateArrivalNoticeWhenIqcFinish(Long id, Long lineId, Long iqcId, BigDecimal qualifiedQuantity);

    /**
     * 完成到货通知单（待入库 → 已完成），内部调用
     *
     * @param id 编号
     */
    void finishArrivalNotice(Long id);

    /**
     * 按编号集合获得到货通知单列表
     *
     * @param ids 编号集合
     * @return 到货通知单列表
     */
    List<MesWmArrivalNoticeDO> getArrivalNoticeList(Collection<Long> ids);

    default Map<Long, MesWmArrivalNoticeDO> getArrivalNoticeMap(Collection<Long> ids) {
        return convertMap(getArrivalNoticeList(ids), MesWmArrivalNoticeDO::getId);
    }

    /**
     * 校验到货通知单存在
     *
     * @param id 编号
     * @return 到货通知单
     */
    MesWmArrivalNoticeDO validateArrivalNoticeExists(Long id);

    /**
     * 校验到货通知单存在且为草稿状态
     *
     * @param id 编号
     * @return 到货通知单
     */
    MesWmArrivalNoticeDO validateArrivalNoticeExistsAndDraft(Long id);

    /**
     * 校验到货通知单和行存在，且行属于该通知单
     *
     * @param noticeId 到货通知单 ID
     * @param lineId 到货通知单行 ID
     */
    void validateArrivalNoticeAndLineExists(Long noticeId, Long lineId);

    /**
     * 按状态获得到货通知单列表
     *
     * @param status 状态
     * @return 到货通知单列表
     */
    List<MesWmArrivalNoticeDO> getArrivalNoticeListByStatus(Integer status);

    /**
     * 查询指定供应商的到货通知单数量
     *
     * @param vendorId 供应商编号
     * @return 数量
     */
    Long getArrivalNoticeCountByVendorId(Long vendorId);

}
