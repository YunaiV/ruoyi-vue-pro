package cn.iocoder.yudao.module.crm.service.followup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.followup.vo.CrmFollowUpRecordSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmFollowUpCreateReqBO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

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
     * 创建更进
     *
     * @param list 请求
     */
    void createFollowUpRecordBatch(List<CrmFollowUpCreateReqBO> list);

    /**
     * 删除跟进记录 (数据权限基于 bizType、 bizId)
     *
     * @param id     编号
     * @param userId 用户编号
     */
    void deleteFollowUpRecord(Long id, Long userId);

    /**
     * 删除跟进
     *
     * @param bizType 模块类型
     * @param bizId   模块数据编号
     */
    void deleteFollowUpRecordByBiz(Integer bizType, Long bizId);

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

    /**
     * 获取跟进记录
     *
     * @param bizType 模块类型
     * @param bizIds  模块数据编号
     * @return 跟进列表
     */
    List<CrmFollowUpRecordDO> getFollowUpRecordByBiz(Integer bizType, Collection<Long> bizIds);

}