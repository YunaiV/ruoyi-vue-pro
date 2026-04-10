package cn.iocoder.yudao.module.mes.service.wm.miscissue;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.MesWmMiscIssuePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.MesWmMiscIssueSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscissue.MesWmMiscIssueDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscissue.MesWmMiscIssueLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.miscissue.MesWmMiscIssueMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmMiscIssueStatusEnum;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 杂项出库单 Service 实现类
 */
@Service
@Validated
@Slf4j
public class MesWmMiscIssueServiceImpl implements MesWmMiscIssueService {

    @Resource
    private MesWmMiscIssueMapper miscIssueMapper;

    @Resource
    private MesWmMiscIssueLineService miscIssueLineService;
    @Resource
    private MesWmTransactionService wmTransactionService;

    @Override
    public Long createMiscIssue(MesWmMiscIssueSaveReqVO createReqVO) {
        // 校验编码唯一
        validateCodeUnique(null, createReqVO.getCode());

        // 插入
        MesWmMiscIssueDO issue = BeanUtils.toBean(createReqVO, MesWmMiscIssueDO.class);
        issue.setStatus(MesWmMiscIssueStatusEnum.PREPARE.getStatus());
        miscIssueMapper.insert(issue);
        return issue.getId();
    }

    @Override
    public void updateMiscIssue(MesWmMiscIssueSaveReqVO updateReqVO) {
        // 校验存在 + 草稿状态
        validateMiscIssueExistsAndDraft(updateReqVO.getId());
        // 校验编码唯一
        validateCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 更新
        MesWmMiscIssueDO updateObj = BeanUtils.toBean(updateReqVO, MesWmMiscIssueDO.class);
        miscIssueMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMiscIssue(Long id) {
        // 校验存在 + 草稿状态
        validateMiscIssueExistsAndDraft(id);

        // 级联删除行
        miscIssueLineService.deleteMiscIssueLineByIssueId(id);
        // 删除
        miscIssueMapper.deleteById(id);
    }

    @Override
    public MesWmMiscIssueDO getMiscIssue(Long id) {
        return miscIssueMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmMiscIssueDO> getMiscIssuePage(MesWmMiscIssuePageReqVO pageReqVO) {
        return miscIssueMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitMiscIssue(Long id) {
        // 校验存在 + 草稿状态
        validateMiscIssueExistsAndDraft(id);
        // 校验至少有一条行
        List<MesWmMiscIssueLineDO> lines = miscIssueLineService.getMiscIssueLineListByIssueId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_MISC_ISSUE_NO_LINE);
        }

        // 提交（草稿 → 待执行出库）
        miscIssueMapper.updateById(new MesWmMiscIssueDO()
                .setId(id).setStatus(MesWmMiscIssueStatusEnum.APPROVED.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishMiscIssue(Long id) {
        // 1.1 校验存在
        MesWmMiscIssueDO issue = validateMiscIssueExists(id);
        if (ObjUtil.notEqual(MesWmMiscIssueStatusEnum.APPROVED.getStatus(), issue.getStatus())) {
            throw exception(WM_MISC_ISSUE_STATUS_INVALID);
        }
        // 1.2 校验至少有一条行
        List<MesWmMiscIssueLineDO> lines = miscIssueLineService.getMiscIssueLineListByIssueId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_MISC_ISSUE_NO_LINE);
        }

        // 2. 遍历所有行，创建库存事务（扣减库存 + 记录流水）
        createTransactionList(issue);

        // 3. 更新出库单状态（待执行出库 → 已完成）
        miscIssueMapper.updateById(new MesWmMiscIssueDO()
                .setId(id).setStatus(MesWmMiscIssueStatusEnum.FINISHED.getStatus()));
    }

    private void createTransactionList(MesWmMiscIssueDO issue) {
        List<MesWmMiscIssueLineDO> lines = miscIssueLineService.getMiscIssueLineListByIssueId(issue.getId());
        wmTransactionService.createTransactionList(convertList(lines, line -> new MesWmTransactionSaveReqDTO()
                .setType(MesWmTransactionTypeEnum.OUT.getType()).setItemId(line.getItemId())
                .setQuantity(line.getQuantity().negate()) // 出库数量为负数
                .setWarehouseId(line.getWarehouseId()).setLocationId(line.getLocationId()).setAreaId(line.getAreaId())
                .setBizType(MesBizTypeConstants.WM_MISC_ISSUE).setBizId(issue.getId()).setBizCode(issue.getCode()).setBizLineId(line.getId())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelMiscIssue(Long id) {
        // 校验存在
        MesWmMiscIssueDO issue = validateMiscIssueExists(id);
        // 已完成和已取消不允许取消
        if (ObjectUtils.equalsAny(issue.getStatus(),
                MesWmMiscIssueStatusEnum.FINISHED.getStatus(),
                MesWmMiscIssueStatusEnum.CANCELED.getStatus())) {
            throw exception(WM_MISC_ISSUE_CANCEL_NOT_ALLOWED);
        }
        // 取消
        miscIssueMapper.updateById(new MesWmMiscIssueDO()
                .setId(id).setStatus(MesWmMiscIssueStatusEnum.CANCELED.getStatus()));
    }

    @Override
    public MesWmMiscIssueDO validateMiscIssueEditable(Long id) {
        MesWmMiscIssueDO issue = validateMiscIssueExists(id);
        if (ObjUtil.notEqual(issue.getStatus(), MesWmMiscIssueStatusEnum.PREPARE.getStatus())) {
            throw exception(WM_MISC_ISSUE_STATUS_INVALID);
        }
        return issue;
    }

    private MesWmMiscIssueDO validateMiscIssueExists(Long id) {
        MesWmMiscIssueDO issue = miscIssueMapper.selectById(id);
        if (issue == null) {
            throw exception(WM_MISC_ISSUE_NOT_EXISTS);
        }
        return issue;
    }

    /**
     * 校验杂项出库单存在且为草稿状态
     */
    private MesWmMiscIssueDO validateMiscIssueExistsAndDraft(Long id) {
        MesWmMiscIssueDO issue = validateMiscIssueExists(id);
        if (ObjUtil.notEqual(MesWmMiscIssueStatusEnum.PREPARE.getStatus(), issue.getStatus())) {
            throw exception(WM_MISC_ISSUE_STATUS_INVALID);
        }
        return issue;
    }

    private void validateCodeUnique(Long id, String code) {
        MesWmMiscIssueDO issue = miscIssueMapper.selectByCode(code);
        if (issue == null) {
            return;
        }
        if (ObjUtil.notEqual(id, issue.getId())) {
            throw exception(WM_MISC_ISSUE_CODE_DUPLICATE);
        }
    }

}
