package cn.iocoder.yudao.module.crm.service.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.framework.core.service.CrmPermissionValidateService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 商机 Service 接口
 *
 * @author ljlleo
 */
public interface CrmBusinessService extends CrmPermissionValidateService {

    /**
     * 创建商机
     *
     * @param createReqVO 创建信息
     * @param userId      用户编号
     * @return 编号
     */
    Long createBusiness(@Valid CrmBusinessCreateReqVO createReqVO, Long userId);

    /**
     * 更新商机
     *
     * @param updateReqVO 更新信息
     */
    void updateBusiness(@Valid CrmBusinessUpdateReqVO updateReqVO);

    /**
     * 删除商机
     *
     * @param id 编号
     */
    void deleteBusiness(Long id);

    /**
     * 获得商机
     *
     * @param id 编号
     * @return 商机
     */
    CrmBusinessDO getBusiness(Long id);

    /**
     * 获得商机列表
     *
     * @param ids 编号
     * @return 商机列表
     */
    List<CrmBusinessDO> getBusinessList(Collection<Long> ids);

    /**
     * 获得商机分页
     *
     * @param pageReqVO 分页查询
     * @param userId    用户编号
     * @return 商机分页
     */
    PageResult<CrmBusinessDO> getBusinessPage(CrmBusinessPageReqVO pageReqVO, Long userId);

    /**
     * 获得商机列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 商机列表
     */
    List<CrmBusinessDO> getBusinessList(CrmBusinessExportReqVO exportReqVO);

    /**
     * 商机转移
     *
     * @param reqVO  请求
     * @param userId 用户编号
     */
    void transferBusiness(CrmBusinessTransferReqVO reqVO, Long userId);

}
