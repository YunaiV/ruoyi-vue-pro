package cn.iocoder.yudao.module.mes.service.wm.outsourceissue;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.line.MesWmOutsourceIssueLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.line.MesWmOutsourceIssueLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourceissue.MesWmOutsourceIssueLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourceissue.MesWmOutsourceIssueDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.outsourceissue.MesWmOutsourceIssueLineMapper;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderBomDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderBomService;
import cn.iocoder.yudao.module.mes.service.wm.batch.MesWmBatchService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_OUTSOURCE_ISSUE_LINE_NOT_EXISTS;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_OUTSOURCE_ISSUE_LINE_ITEM_NOT_IN_BOM;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_OUTSOURCE_ISSUE_NOT_EXISTS;

/**
 * MES 外协发料单行 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmOutsourceIssueLineServiceImpl implements MesWmOutsourceIssueLineService {

    @Resource
    private MesWmOutsourceIssueLineMapper outsourceIssueLineMapper;

    @Resource
    private MesWmOutsourceIssueDetailService outsourceIssueDetailService;
    @Resource
    @Lazy
    private MesWmOutsourceIssueService outsourceIssueService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesWmBatchService batchService;
    @Resource
    private MesProWorkOrderBomService workOrderBomService;

    @Override
    public Long createOutsourceIssueLine(MesWmOutsourceIssueLineSaveReqVO createReqVO) {
        // 校验数据
        validateOutsourceIssueLineSaveData(createReqVO);
        // 根据 batchCode 解析 batchId
        fillBatchId(createReqVO);

        // 插入
        MesWmOutsourceIssueLineDO line = BeanUtils.toBean(createReqVO, MesWmOutsourceIssueLineDO.class);
        outsourceIssueLineMapper.insert(line);
        return line.getId();
    }

    @Override
    public void updateOutsourceIssueLine(MesWmOutsourceIssueLineSaveReqVO updateReqVO) {
        // 校验存在
        validateOutsourceIssueLineExists(updateReqVO.getId());
        // 校验数据
        validateOutsourceIssueLineSaveData(updateReqVO);
        // 根据 batchCode 解析 batchId
        fillBatchId(updateReqVO);

        // 更新
        MesWmOutsourceIssueLineDO updateObj = BeanUtils.toBean(updateReqVO, MesWmOutsourceIssueLineDO.class);
        outsourceIssueLineMapper.updateById(updateObj);
    }

    @Override
    public void deleteOutsourceIssueLine(Long id) {
        // 校验存在
        validateOutsourceIssueLineExists(id);

        // 级联删除明细
        outsourceIssueDetailService.deleteOutsourceIssueDetailByLineId(id);
        // 删除行
        outsourceIssueLineMapper.deleteById(id);
    }

    @Override
    public MesWmOutsourceIssueLineDO getOutsourceIssueLine(Long id) {
        return outsourceIssueLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmOutsourceIssueLineDO> getOutsourceIssueLinePage(MesWmOutsourceIssueLinePageReqVO pageReqVO) {
        return outsourceIssueLineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesWmOutsourceIssueLineDO> getOutsourceIssueLineListByIssueId(Long issueId) {
        return outsourceIssueLineMapper.selectListByIssueId(issueId);
    }

    @Override
    public void deleteOutsourceIssueLineByIssueId(Long issueId) {
        outsourceIssueLineMapper.deleteByIssueId(issueId);
    }

    private void validateOutsourceIssueLineExists(Long id) {
        if (outsourceIssueLineMapper.selectById(id) == null) {
            throw exception(WM_OUTSOURCE_ISSUE_LINE_NOT_EXISTS);
        }
    }

    private void validateOutsourceIssueLineSaveData(MesWmOutsourceIssueLineSaveReqVO saveReqVO) {
        // 校验关联的发料单存在
        MesWmOutsourceIssueDO issue = outsourceIssueService.getOutsourceIssue(saveReqVO.getIssueId());
        if (issue == null) {
            throw exception(WM_OUTSOURCE_ISSUE_NOT_EXISTS);
        }
        // 校验关联的物料存在
        itemService.validateItemExists(saveReqVO.getItemId());
        // 校验物料是否在工单 BOM 中
        validateItemInWorkOrderBom(issue.getWorkOrderId(), saveReqVO.getItemId());
    }

    private void validateItemInWorkOrderBom(Long workOrderId, Long itemId) {
        if (workOrderId == null) {
            return;
        }
        // 获取当前外协工单的 BOM 列表
        List<MesProWorkOrderBomDO> workOrderBoms = workOrderBomService.getWorkOrderBomListByWorkOrderId(workOrderId);
        if (CollUtil.isEmpty(workOrderBoms)) {
            return;
        }
        // 检查发料的物料是否在 BOM 列表中
        MesProWorkOrderBomDO workOrderBom = CollUtil.findOne(workOrderBoms,
                bom -> bom.getItemId().equals(itemId));
        if (workOrderBom == null) {
            throw exception(WM_OUTSOURCE_ISSUE_LINE_ITEM_NOT_IN_BOM);
        }
    }

    /**
     * 根据 batchCode 解析 batchId
     */
    private void fillBatchId(MesWmOutsourceIssueLineSaveReqVO reqVO) {
        if (StrUtil.isBlank(reqVO.getBatchCode())) {
            reqVO.setBatchId(null);
            return;
        }
        MesWmBatchDO batch = batchService.getBatchByCode(reqVO.getBatchCode());
        reqVO.setBatchId(batch != null ? batch.getId() : null);
    }

}
