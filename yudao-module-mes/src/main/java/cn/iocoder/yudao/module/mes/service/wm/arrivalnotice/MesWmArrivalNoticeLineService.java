package cn.iocoder.yudao.module.mes.service.wm.arrivalnotice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.line.MesWmArrivalNoticeLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.arrivalnotice.vo.line.MesWmArrivalNoticeLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.arrivalnotice.MesWmArrivalNoticeLineDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

/**
 * MES 到货通知单行 Service 接口
 */
public interface MesWmArrivalNoticeLineService {

    /**
     * 创建到货通知单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createArrivalNoticeLine(@Valid MesWmArrivalNoticeLineSaveReqVO createReqVO);

    /**
     * 修改到货通知单行
     *
     * @param updateReqVO 修改信息
     */
    void updateArrivalNoticeLine(@Valid MesWmArrivalNoticeLineSaveReqVO updateReqVO);

    /**
     * 删除到货通知单行
     *
     * @param id 编号
     */
    void deleteArrivalNoticeLine(Long id);

    /**
     * 获得到货通知单行
     *
     * @param id 编号
     * @return 到货通知单行
     */
    MesWmArrivalNoticeLineDO getArrivalNoticeLine(Long id);

    /**
     * 获得到货通知单行分页
     *
     * @param pageReqVO 分页参数
     * @return 到货通知单行分页
     */
    PageResult<MesWmArrivalNoticeLineDO> getArrivalNoticeLinePage(MesWmArrivalNoticeLinePageReqVO pageReqVO);

    /**
     * 按通知单编号获得行列表
     *
     * @param noticeId 通知单编号
     * @return 行列表
     */
    List<MesWmArrivalNoticeLineDO> getArrivalNoticeLineListByNoticeId(Long noticeId);

    /**
     * 按通知单编号批量删除行
     *
     * @param noticeId 通知单编号
     */
    void deleteArrivalNoticeLineByNoticeId(Long noticeId);

    /**
     * IQC 检验完成后回写行数据
     *
     * @param lineId 行编号
     * @param iqcId IQC 检验单编号
     * @param qualifiedQuantity 合格数量
     */
    void updateArrivalNoticeLineWhenIqcFinish(Long lineId, Long iqcId, BigDecimal qualifiedQuantity);

    /**
     * 校验到货通知单行存在
     *
     * @param id 编号
     * @return 到货通知单行
     */
    MesWmArrivalNoticeLineDO validateArrivalNoticeLineExists(Long id);

    /**
     * 校验到货通知单行存在且属于指定的到货通知单
     *
     * @param lineId 行编号
     * @param noticeId 到货通知单编号
     * @return 到货通知单行
     */
    MesWmArrivalNoticeLineDO validateArrivalNoticeLineExists(Long lineId, Long noticeId);

}
