package cn.iocoder.yudao.module.mes.service.wm.outsourceissue;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.detail.MesWmOutsourceIssueDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourceissue.MesWmOutsourceIssueDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourceissue.MesWmOutsourceIssueLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.outsourceissue.MesWmOutsourceIssueDetailMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

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
    private MesWmOutsourceIssueLineService outsourceIssueLineService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesWmWarehouseAreaService warehouseAreaService;
    @Resource
    private MesWmMaterialStockService materialStockService;

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
        // 校验父数据（行）存在
        MesWmOutsourceIssueLineDO line = outsourceIssueLineService.getOutsourceIssueLine(saveReqVO.getLineId());
        if (line == null) {
            throw exception(WM_OUTSOURCE_ISSUE_LINE_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(line.getIssueId(), saveReqVO.getIssueId())) {
            throw exception(WM_OUTSOURCE_ISSUE_DETAIL_LINE_NOT_MATCH);
        }
        // 校验物料存在
        itemService.validateItemExistsAndEnable(saveReqVO.getItemId());
        if (ObjUtil.notEqual(line.getItemId(), saveReqVO.getItemId())) {
            throw exception(WM_OUTSOURCE_ISSUE_DETAIL_ITEM_MISMATCH);
        }
        // 校验仓库、库区、库位的关联关系
        warehouseAreaService.validateWarehouseAreaExists(
                saveReqVO.getWarehouseId(), saveReqVO.getLocationId(), saveReqVO.getAreaId());
        // 校验库存记录存在且物料一致
        materialStockService.validateSelectedStock(
                saveReqVO.getMaterialStockId(), saveReqVO.getItemId(), saveReqVO.getBatchId(), null,
                saveReqVO.getWarehouseId(), saveReqVO.getLocationId(), saveReqVO.getAreaId(), saveReqVO.getQuantity());
    }

}
