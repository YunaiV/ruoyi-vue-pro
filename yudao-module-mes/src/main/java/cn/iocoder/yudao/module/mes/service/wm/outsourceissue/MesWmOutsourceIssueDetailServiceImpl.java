package cn.iocoder.yudao.module.mes.service.wm.outsourceissue;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.detail.MesWmOutsourceIssueDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourceissue.MesWmOutsourceIssueDetailDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.outsourceissue.MesWmOutsourceIssueDetailMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_OUTSOURCE_ISSUE_DETAIL_NOT_EXISTS;

/**
 * MES 外协发料单明细 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmOutsourceIssueDetailServiceImpl implements MesWmOutsourceIssueDetailService {

    @Resource
    private MesWmOutsourceIssueDetailMapper outsourceIssueDetailMapper;
    @Resource
    @Lazy
    private MesWmOutsourceIssueService outsourceIssueService;
    @Resource
    private MesMdItemService itemService;

    @Override
    public Long createOutsourceIssueDetail(MesWmOutsourceIssueDetailSaveReqVO createReqVO) {
        // 校验数据
        validateOutsourceIssueDetailSaveData(createReqVO);

        // 插入
        MesWmOutsourceIssueDetailDO detail = BeanUtils.toBean(createReqVO, MesWmOutsourceIssueDetailDO.class);
        outsourceIssueDetailMapper.insert(detail);
        return detail.getId();
    }

    @Override
    public void updateOutsourceIssueDetail(MesWmOutsourceIssueDetailSaveReqVO updateReqVO) {
        // 校验存在
        validateOutsourceIssueDetailExists(updateReqVO.getId());
        // 校验数据
        validateOutsourceIssueDetailSaveData(updateReqVO);

        // 更新
        MesWmOutsourceIssueDetailDO updateObj = BeanUtils.toBean(updateReqVO, MesWmOutsourceIssueDetailDO.class);
        outsourceIssueDetailMapper.updateById(updateObj);
    }

    @Override
    public void deleteOutsourceIssueDetail(Long id) {
        // 校验存在
        validateOutsourceIssueDetailExists(id);
        // 删除
        outsourceIssueDetailMapper.deleteById(id);
    }

    @Override
    public MesWmOutsourceIssueDetailDO getOutsourceIssueDetail(Long id) {
        return outsourceIssueDetailMapper.selectById(id);
    }

    @Override
    public List<MesWmOutsourceIssueDetailDO> getOutsourceIssueDetailListByIssueId(Long issueId) {
        return outsourceIssueDetailMapper.selectListByIssueId(issueId);
    }

    @Override
    public List<MesWmOutsourceIssueDetailDO> getOutsourceIssueDetailListByLineId(Long lineId) {
        return outsourceIssueDetailMapper.selectListByLineId(lineId);
    }

    @Override
    public void deleteOutsourceIssueDetailByIssueId(Long issueId) {
        outsourceIssueDetailMapper.deleteByIssueId(issueId);
    }

    @Override
    public void deleteOutsourceIssueDetailByLineId(Long lineId) {
        outsourceIssueDetailMapper.deleteByLineId(lineId);
    }

    private void validateOutsourceIssueDetailExists(Long id) {
        if (outsourceIssueDetailMapper.selectById(id) == null) {
            throw exception(WM_OUTSOURCE_ISSUE_DETAIL_NOT_EXISTS);
        }
    }

    private void validateOutsourceIssueDetailSaveData(MesWmOutsourceIssueDetailSaveReqVO saveReqVO) {
        // 校验关联的发料单存在
        outsourceIssueService.getOutsourceIssue(saveReqVO.getIssueId());
        // 校验关联的物料存在
        itemService.validateItemExists(saveReqVO.getItemId());
    }

}
