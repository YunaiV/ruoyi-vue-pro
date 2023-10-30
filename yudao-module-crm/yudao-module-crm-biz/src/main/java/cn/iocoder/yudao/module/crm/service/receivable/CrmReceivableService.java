package cn.iocoder.yudao.module.crm.service.receivable;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivableDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 回款管理 Service 接口
 *
 * @author 赤焰
 */
public interface CrmReceivableService {

    /**
     * 创建回款管理
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReceivable(@Valid CrmReceivableCreateReqVO createReqVO);

    /**
     * 更新回款管理
     *
     * @param updateReqVO 更新信息
     */
    void updateReceivable(@Valid CrmReceivableUpdateReqVO updateReqVO);

    /**
     * 删除回款管理
     *
     * @param id 编号
     */
    void deleteReceivable(Long id);

    /**
     * 获得回款管理
     *
     * @param id 编号
     * @return 回款管理
     */
    CrmReceivableDO getReceivable(Long id);

    /**
     * 获得回款管理列表
     *
     * @param ids 编号
     * @return 回款管理列表
     */
    List<CrmReceivableDO> getReceivableList(Collection<Long> ids);

    /**
     * 获得回款管理分页
     *
     * @param pageReqVO 分页查询
     * @return 回款管理分页
     */
    PageResult<CrmReceivableDO> getReceivablePage(CrmReceivablePageReqVO pageReqVO);

    /**
     * 获得回款管理列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 回款管理列表
     */
    List<CrmReceivableDO> getReceivableList(CrmReceivableExportReqVO exportReqVO);

}
