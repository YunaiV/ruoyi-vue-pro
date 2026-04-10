package cn.iocoder.yudao.module.mes.service.wm.productissue;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productissue.vo.detail.MesWmProductIssueDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productissue.MesWmProductIssueDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productissue.MesWmProductIssueDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productissue.MesWmProductIssueLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productissue.MesWmProductIssueDetailMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmProductIssueStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 领料出库明细 Service 实现类
 */
@Service
@Validated
public class MesWmProductIssueDetailServiceImpl implements MesWmProductIssueDetailService {

    @Resource
    private MesWmProductIssueDetailMapper issueDetailMapper;

    @Resource
    @Lazy
    private MesWmProductIssueService issueService;
    @Resource
    @Lazy
    private MesWmProductIssueLineService issueLineService;

    @Override
    public Long createProductIssueDetail(MesWmProductIssueDetailSaveReqVO createReqVO) {
        // 校验数据
        validateProductIssueDetailSaveData(createReqVO);

        // 插入
        MesWmProductIssueDetailDO detail = BeanUtils.toBean(createReqVO, MesWmProductIssueDetailDO.class);
        issueDetailMapper.insert(detail);
        return detail.getId();
    }

    @Override
    public void updateProductIssueDetail(MesWmProductIssueDetailSaveReqVO updateReqVO) {
        // 校验存在
        validateProductIssueDetailExists(updateReqVO.getId());
        // 校验数据
        validateProductIssueDetailSaveData(updateReqVO);

        // 更新
        MesWmProductIssueDetailDO updateObj = BeanUtils.toBean(updateReqVO, MesWmProductIssueDetailDO.class);
        issueDetailMapper.updateById(updateObj);
    }

    @Override
    public void deleteProductIssueDetail(Long id) {
        // 校验存在
        MesWmProductIssueDetailDO detail = issueDetailMapper.selectById(id);
        if (detail == null) {
            throw exception(WM_PRODUCT_ISSUE_DETAIL_NOT_EXISTS);
        }
        // 校验主单状态为草稿或待拣货（允许在拣货模式删除明细）
        MesWmProductIssueLineDO line = issueLineService.validateProductIssueLineExists(detail.getLineId());
        MesWmProductIssueDO issue = issueService.validateProductIssueExists(line.getIssueId());
        if (!ObjectUtils.equalsAny(issue.getStatus(),
                MesWmProductIssueStatusEnum.PREPARE.getStatus(),
                MesWmProductIssueStatusEnum.APPROVING.getStatus())) {
            throw exception(WM_PRODUCT_ISSUE_STATUS_INVALID);
        }

        // 删除
        issueDetailMapper.deleteById(id);
    }

    @Override
    public MesWmProductIssueDetailDO getProductIssueDetail(Long id) {
        return issueDetailMapper.selectById(id);
    }

    @Override
    public List<MesWmProductIssueDetailDO> getProductIssueDetailListByLineId(Long lineId) {
        return issueDetailMapper.selectListByLineId(lineId);
    }

    @Override
    public List<MesWmProductIssueDetailDO> getProductIssueDetailListByIssueId(Long issueId) {
        return issueDetailMapper.selectListByIssueId(issueId);
    }

    @Override
    public void deleteProductIssueDetailByIssueId(Long issueId) {
        issueDetailMapper.deleteByIssueId(issueId);
    }

    @Override
    public void deleteProductIssueDetailByLineId(Long lineId) {
        issueDetailMapper.deleteByLineId(lineId);
    }

    private void validateProductIssueDetailExists(Long id) {
        if (issueDetailMapper.selectById(id) == null) {
            throw exception(WM_PRODUCT_ISSUE_DETAIL_NOT_EXISTS);
        }
    }

    /**
     * 校验保存时的关联数据
     */
    private void validateProductIssueDetailSaveData(MesWmProductIssueDetailSaveReqVO reqVO) {
        // 校验父数据存在
        MesWmProductIssueLineDO line = issueLineService.validateProductIssueLineExists(reqVO.getLineId());
        // 校验物料匹配（明细 itemId 必须与行 itemId 一致）
        validateDetailItemMatch(reqVO.getItemId(), line.getItemId());
    }

    /**
     * 校验明细物料与行物料一致
     */
    private void validateDetailItemMatch(Long detailItemId, Long lineItemId) {
        if (ObjUtil.notEqual(detailItemId, lineItemId)) {
            throw exception(WM_PRODUCT_ISSUE_DETAIL_ITEM_MISMATCH);
        }
    }

}

