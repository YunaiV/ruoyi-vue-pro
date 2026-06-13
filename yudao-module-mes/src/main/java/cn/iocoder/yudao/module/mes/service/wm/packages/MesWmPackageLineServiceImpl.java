package cn.iocoder.yudao.module.mes.service.wm.packages;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.line.MesWmPackageLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.line.MesWmPackageLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.packages.MesWmPackageLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.packages.MesWmPackageLineMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_PACKAGE_LINE_NOT_EXISTS;

/**
 * MES 装箱明细 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmPackageLineServiceImpl implements MesWmPackageLineService {

    @Resource
    private MesWmPackageLineMapper packageLineMapper;

    @Resource
    @Lazy
    private MesWmPackageService packageService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesProWorkOrderService workOrderService;

    @Override
    public Long createPackageLine(MesWmPackageLineSaveReqVO createReqVO) {
        // 校验数据
        validateLineSaveData(createReqVO);

        // 插入
        MesWmPackageLineDO line = BeanUtils.toBean(createReqVO, MesWmPackageLineDO.class);
        packageLineMapper.insert(line);
        return line.getId();
    }

    @Override
    public void updatePackageLine(MesWmPackageLineSaveReqVO updateReqVO) {
        // 校验存在
        MesWmPackageLineDO line = validatePackageLineExists(updateReqVO.getId());
        // 校验数据
        updateReqVO.setPackageId(line.getPackageId());
        validateLineSaveData(updateReqVO);

        // 更新
        MesWmPackageLineDO updateObj = BeanUtils.toBean(updateReqVO, MesWmPackageLineDO.class);
        packageLineMapper.updateById(updateObj);
    }

    @Override
    public void deletePackageLine(Long id) {
        // 校验存在
        MesWmPackageLineDO line = validatePackageLineExists(id);
        // 校验装箱单状态为草稿
        packageService.validatePackageStatusDraft(line.getPackageId());

        // 删除
        packageLineMapper.deleteById(id);
    }

    @Override
    public MesWmPackageLineDO getPackageLine(Long id) {
        return packageLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmPackageLineDO> getPackageLinePage(MesWmPackageLinePageReqVO pageReqVO) {
        if (pageReqVO.getPackageId() == null) {
            return PageResult.empty();
        }
        List<Long> packageIds = packageService.getPackageAndDescendantIds(pageReqVO.getPackageId());
        return packageLineMapper.selectPage(pageReqVO, packageIds);
    }

    @Override
    public void deletePackageLineByPackageId(Long packageId) {
        packageLineMapper.deleteByPackageId(packageId);
    }

    // ========== 校验方法 ==========

    /**
     * 校验保存时的关联数据
     */
    private void validateLineSaveData(MesWmPackageLineSaveReqVO reqVO) {
        // 校验装箱单状态为草稿
        packageService.validatePackageStatusDraft(reqVO.getPackageId());
        // 校验产品物料存在
        itemService.validateItemExistsAndEnable(reqVO.getItemId());
        // 校验工单已确认
        workOrderService.validateWorkOrderConfirmed(reqVO.getWorkOrderId());
    }

    private MesWmPackageLineDO validatePackageLineExists(Long id) {
        MesWmPackageLineDO line = packageLineMapper.selectById(id);
        if (line == null) {
            throw exception(WM_PACKAGE_LINE_NOT_EXISTS);
        }
        return line;
    }

}
