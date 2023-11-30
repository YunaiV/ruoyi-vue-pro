package cn.iocoder.yudao.module.crm.service.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusQueryVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 商机状态 Service 接口
 *
 * @author ljlleo
 */
public interface CrmBusinessStatusService {

    /**
     * 创建商机状态
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createBusinessStatus(@Valid CrmBusinessStatusSaveReqVO createReqVO);

    /**
     * 更新商机状态
     *
     * @param updateReqVO 更新信息
     */
    void updateBusinessStatus(@Valid CrmBusinessStatusSaveReqVO updateReqVO);

    /**
     * 删除商机状态
     *
     * @param id 编号
     */
    void deleteBusinessStatus(Long id);

    /**
     * 获得商机状态
     *
     * @param id 编号
     * @return 商机状态
     */
    CrmBusinessStatusDO getBusinessStatus(Long id);

    /**
     * 获得商机状态分页
     *
     * @param pageReqVO 分页查询
     * @return 商机状态分页
     */
    PageResult<CrmBusinessStatusDO> getBusinessStatusPage(CrmBusinessStatusPageReqVO pageReqVO);

    // TODO @ljlleo 常用的 ids 之类的查询，可以封装单独的方法，不用走类似 QueryVO，用起来更方便。
    // TODO @ljlleo 方法名用 getBusinessStatusList
    /**
     * 获得商机状态分页
     *
     * @param queryVO 查询参数
     * @return 商机状态分页
     */
    List<CrmBusinessStatusDO> selectList(CrmBusinessStatusQueryVO queryVO);

}
