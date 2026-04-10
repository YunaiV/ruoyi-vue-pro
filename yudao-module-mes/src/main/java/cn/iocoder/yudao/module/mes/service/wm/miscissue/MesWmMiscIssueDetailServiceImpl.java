package cn.iocoder.yudao.module.mes.service.wm.miscissue;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.line.MesWmMiscIssueLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscissue.MesWmMiscIssueDetailDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.miscissue.MesWmMiscIssueDetailMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_MISC_ISSUE_DETAIL_NOT_EXISTS;

/**
 * MES 杂项出库明细 Service 实现类
 */
@Service
@Validated
public class MesWmMiscIssueDetailServiceImpl implements MesWmMiscIssueDetailService {

    @Resource
    private MesWmMiscIssueDetailMapper issueDetailMapper;

    @Override
    public Long createMiscIssueDetail(MesWmMiscIssueLineSaveReqVO createReqVO) {
        MesWmMiscIssueDetailDO detail = BeanUtils.toBean(createReqVO, MesWmMiscIssueDetailDO.class);
        detail.setId(null); // 清空 id，让数据库自动生成
        detail.setLineId(createReqVO.getId()); // VO 的 id 就是 lineId
        issueDetailMapper.insert(detail);
        return detail.getId();
    }

    @Override
    public void updateMiscIssueDetail(MesWmMiscIssueLineSaveReqVO updateReqVO) {
        // 基于 lineId 查询唯一的 detail 记录（VO 的 id 字段是 lineId）
        List<MesWmMiscIssueDetailDO> details = issueDetailMapper.selectListByLineId(updateReqVO.getId());
        if (CollUtil.isEmpty(details)) {
            return;
        }

        // 更新 detail（应该只有一条记录）
        MesWmMiscIssueDetailDO updateObj = BeanUtils.toBean(updateReqVO, MesWmMiscIssueDetailDO.class);
        updateObj.setId(details.get(0).getId()); // 使用查询到的 detail id
        updateObj.setLineId(updateReqVO.getId()); // VO 的 id 就是 lineId
        issueDetailMapper.updateById(updateObj);
    }

    @Override
    public void deleteMiscIssueDetailByIssueId(Long issueId) {
        issueDetailMapper.deleteByIssueId(issueId);
    }

    @Override
    public void deleteMiscIssueDetailByLineId(Long lineId) {
        issueDetailMapper.deleteByLineId(lineId);
    }

    @Override
    public void validateMiscIssueDetailExists(Long id) {
        if (issueDetailMapper.selectById(id) == null) {
            throw exception(WM_MISC_ISSUE_DETAIL_NOT_EXISTS);
        }
    }

}
