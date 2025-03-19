package cn.iocoder.yudao.module.wms.service.approval.history;

import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.approval.history.WmsApprovalHistoryDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.approval.history.WmsApprovalHistoryMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 审批历史 Service 实现类
 *
 * @author 李方捷
 */
@Service
public class WmsApprovalHistoryServiceImpl implements WmsApprovalHistoryService {

    @Resource
    private WmsApprovalHistoryMapper approvalHistoryMapper;

    /**
     * @sign : F7AFA51ADC8F335C
     */
    @Override
    public WmsApprovalHistoryDO createApprovalHistory(WmsApprovalHistorySaveReqVO createReqVO) {
        // 插入
        WmsApprovalHistoryDO approvalHistory = BeanUtils.toBean(createReqVO, WmsApprovalHistoryDO.class);
        approvalHistoryMapper.insert(approvalHistory);
        // 返回
        return approvalHistory;
    }

    /**
     * @sign : 4EB4EC505C82C40A
     */
    @Override
    public WmsApprovalHistoryDO updateApprovalHistory(WmsApprovalHistorySaveReqVO updateReqVO) {
        // 校验存在
        WmsApprovalHistoryDO exists = validateApprovalHistoryExists(updateReqVO.getId());
        // 更新
        WmsApprovalHistoryDO approvalHistory = BeanUtils.toBean(updateReqVO, WmsApprovalHistoryDO.class);
        approvalHistoryMapper.updateById(approvalHistory);
        // 返回
        return approvalHistory;
    }

    /**
     * @sign : ABC6DE5623435EC2
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApprovalHistory(Long id) {
        // 校验存在
        WmsApprovalHistoryDO approvalHistory = validateApprovalHistoryExists(id);
        // 删除
        approvalHistoryMapper.deleteById(id);
    }

    /**
     * @sign : 0EA5B905A9B287F0
     */
    private WmsApprovalHistoryDO validateApprovalHistoryExists(Long id) {
        WmsApprovalHistoryDO approvalHistory = approvalHistoryMapper.selectById(id);
        if (approvalHistory == null) {
            throw exception(APPROVAL_HISTORY_NOT_EXISTS);
        }
        return approvalHistory;
    }

    @Override
    public WmsApprovalHistoryDO getApprovalHistory(Long id) {
        return approvalHistoryMapper.selectById(id);
    }

    @Override
    public PageResult<WmsApprovalHistoryDO> getApprovalHistoryPage(WmsApprovalHistoryPageReqVO pageReqVO) {
        return approvalHistoryMapper.selectPage(pageReqVO);
    }
}
