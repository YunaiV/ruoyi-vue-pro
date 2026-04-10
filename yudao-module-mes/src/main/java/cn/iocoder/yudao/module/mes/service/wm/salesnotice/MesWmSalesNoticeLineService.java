package cn.iocoder.yudao.module.mes.service.wm.salesnotice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.line.MesWmSalesNoticeLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.line.MesWmSalesNoticeLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.salesnotice.MesWmSalesNoticeLineDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 发货通知单行 Service 接口
 */
public interface MesWmSalesNoticeLineService {

    /**
     * 创建发货通知单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSalesNoticeLine(@Valid MesWmSalesNoticeLineSaveReqVO createReqVO);

    /**
     * 修改发货通知单行
     *
     * @param updateReqVO 修改信息
     */
    void updateSalesNoticeLine(@Valid MesWmSalesNoticeLineSaveReqVO updateReqVO);

    /**
     * 删除发货通知单行
     *
     * @param id 编号
     */
    void deleteSalesNoticeLine(Long id);

    /**
     * 获得发货通知单行
     *
     * @param id 编号
     * @return 发货通知单行
     */
    MesWmSalesNoticeLineDO getSalesNoticeLine(Long id);

    /**
     * 获得发货通知单行分页
     *
     * @param pageReqVO 分页参数
     * @return 发货通知单行分页
     */
    PageResult<MesWmSalesNoticeLineDO> getSalesNoticeLinePage(MesWmSalesNoticeLinePageReqVO pageReqVO);

    /**
     * 按通知单编号获得行列表
     *
     * @param noticeId 通知单编号
     * @return 行列表
     */
    List<MesWmSalesNoticeLineDO> getSalesNoticeLineListByNoticeId(Long noticeId);

    /**
     * 按通知单编号批量删除行
     *
     * @param noticeId 通知单编号
     */
    void deleteSalesNoticeLineByNoticeId(Long noticeId);

}
