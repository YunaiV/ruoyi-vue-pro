package cn.iocoder.yudao.module.mes.service.wm.miscissue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.line.MesWmMiscIssueLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscissue.vo.line.MesWmMiscIssueLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscissue.MesWmMiscIssueLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.miscissue.MesWmMiscIssueLineMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_MISC_ISSUE_LINE_NOT_EXISTS;

/**
 * MES 杂项出库单行 Service 实现类
 */
@Service
@Validated
public class MesWmMiscIssueLineServiceImpl implements MesWmMiscIssueLineService {

    @Resource
    private MesWmMiscIssueLineMapper miscIssueLineMapper;

    @Resource
    @Lazy
    private MesWmMiscIssueService miscIssueService;
    @Resource
    @Lazy
    private MesWmMiscIssueDetailService miscIssueDetailService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesWmWarehouseAreaService warehouseAreaService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMiscIssueLine(MesWmMiscIssueLineSaveReqVO createReqVO) {
        // 1. 校验数据
        validateMiscIssueLineSaveData(createReqVO.getIssueId(), createReqVO);

        // 2. 新增行
        MesWmMiscIssueLineDO line = BeanUtils.toBean(createReqVO, MesWmMiscIssueLineDO.class);
        miscIssueLineMapper.insert(line);

        // 3.自动创建明细
        createReqVO.setId(line.getId());
        miscIssueDetailService.createMiscIssueDetail(createReqVO);
        return line.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMiscIssueLine(MesWmMiscIssueLineSaveReqVO updateReqVO) {
        // 1.1 校验存在
        MesWmMiscIssueLineDO line = validateAndGetMiscIssueLine(updateReqVO.getId());
        // 1.2 校验数据
        validateMiscIssueLineSaveData(line.getIssueId(), updateReqVO);

        // 2. 更新行
        MesWmMiscIssueLineDO updateObj = BeanUtils.toBean(updateReqVO, MesWmMiscIssueLineDO.class);
        miscIssueLineMapper.updateById(updateObj);

        // 3. 更新明细（基于行信息）
        miscIssueDetailService.updateMiscIssueDetail(updateReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMiscIssueLine(Long id) {
        // 校验存在
        MesWmMiscIssueLineDO line = validateAndGetMiscIssueLine(id);
        // 校验父单据存在且为可编辑状态
        miscIssueService.validateMiscIssueEditable(line.getIssueId());

        // 删除行
        miscIssueLineMapper.deleteById(id);
        // 删除明细
        miscIssueDetailService.deleteMiscIssueDetailByLineId(id);
    }

    @Override
    public MesWmMiscIssueLineDO getMiscIssueLine(Long id) {
        return miscIssueLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmMiscIssueLineDO> getMiscIssueLinePage(MesWmMiscIssueLinePageReqVO pageReqVO) {
        return miscIssueLineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesWmMiscIssueLineDO> getMiscIssueLineListByIssueId(Long issueId) {
        return miscIssueLineMapper.selectListByIssueId(issueId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMiscIssueLineByIssueId(Long issueId) {
        // 删除明细
        miscIssueDetailService.deleteMiscIssueDetailByIssueId(issueId);
        // 删除行
        miscIssueLineMapper.deleteByIssueId(issueId);
    }

    @Override
    public void validateMiscIssueLineExists(Long id) {
        if (miscIssueLineMapper.selectById(id) == null) {
            throw exception(WM_MISC_ISSUE_LINE_NOT_EXISTS);
        }
    }

    private MesWmMiscIssueLineDO validateAndGetMiscIssueLine(Long id) {
        MesWmMiscIssueLineDO line = miscIssueLineMapper.selectById(id);
        if (line == null) {
            throw exception(WM_MISC_ISSUE_LINE_NOT_EXISTS);
        }
        return line;
    }

    private void validateMiscIssueLineSaveData(Long issueId, MesWmMiscIssueLineSaveReqVO reqVO) {
        // 校验父单据存在且为可编辑状态
        miscIssueService.validateMiscIssueEditable(issueId);
        // 校验物料存在
        itemService.validateItemExistsAndEnable(reqVO.getItemId());
        // 校验仓库、库区、库位的父子关系
        warehouseAreaService.validateWarehouseAreaExists(reqVO.getWarehouseId(),
                reqVO.getLocationId(), reqVO.getAreaId());
    }

}
