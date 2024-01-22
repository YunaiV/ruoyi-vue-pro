package cn.iocoder.yudao.module.crm.service.followup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import jakarta.validation.Valid;

/**
 * 跟进记录 Service 接口
 *
 * @author 芋道源码
 */
public interface CrmFollowUpRecordService {

    /**
     * 创建跟进记录 (数据权限基于 bizType、 bizId)
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFollowUpRecord(@Valid CrmFollowUpRecordSaveReqVO createReqVO);

    /**
     * 删除跟进记录 (数据权限基于 bizType、 bizId)
     *
     * @param id     编号
     * @param userId 用户编号
     */
    void deleteFollowUpRecord(Long id, Long userId);

    /**
     * 获得跟进记录
     *
     * @param id 编号
     * @return 跟进记录
     */
    CrmFollowUpRecordDO getFollowUpRecord(Long id);

    /**
     * 获得跟进记录分页 (数据权限基于 bizType、 bizId)
     *
     * @param pageReqVO 分页查询
     * @return 跟进记录分页
     */
    PageResult<CrmFollowUpRecordDO> getFollowUpRecordPage(CrmFollowUpRecordPageReqVO pageReqVO);

}