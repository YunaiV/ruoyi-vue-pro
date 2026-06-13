package cn.iocoder.yudao.module.mes.service.wm.salesnotice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.MesWmSalesNoticePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.salesnotice.vo.MesWmSalesNoticeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.salesnotice.MesWmSalesNoticeDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 发货通知单 Service 接口
 */
public interface MesWmSalesNoticeService {

    /**
     * 创建发货通知单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSalesNotice(@Valid MesWmSalesNoticeSaveReqVO createReqVO);

    /**
     * 修改发货通知单
     *
     * @param updateReqVO 修改信息
     */
    void updateSalesNotice(@Valid MesWmSalesNoticeSaveReqVO updateReqVO);

    /**
     * 删除发货通知单
     *
     * @param id 编号
     */
    void deleteSalesNotice(Long id);

    /**
     * 校验发货通知单存在
     *
     * @param id 编号
     * @return 发货通知单
     */
    MesWmSalesNoticeDO validateSalesNoticeExists(Long id);

    /**
     * 获得发货通知单
     *
     * @param id 编号
     * @return 发货通知单
     */
    MesWmSalesNoticeDO getSalesNotice(Long id);

    /**
     * 获得发货通知单分页
     *
     * @param pageReqVO 分页参数
     * @return 发货通知单分页
     */
    PageResult<MesWmSalesNoticeDO> getSalesNoticePage(MesWmSalesNoticePageReqVO pageReqVO);

    /**
     * 提交发货通知单
     *
     * @param id 编号
     */
    void submitSalesNotice(Long id);

    /**
     * 获得发货通知单列表
     *
     * @param ids 编号列表
     * @return 发货通知单列表
     */
    List<MesWmSalesNoticeDO> getSalesNoticeList(Collection<Long> ids);

    /**
     * 获得发货通知单 Map
     *
     * @param ids 编号列表
     * @return 发货通知单 Map
     */
    default Map<Long, MesWmSalesNoticeDO> getSalesNoticeMap(Collection<Long> ids) {
        return convertMap(getSalesNoticeList(ids), MesWmSalesNoticeDO::getId);
    }

}
