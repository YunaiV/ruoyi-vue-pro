package cn.iocoder.yudao.module.mes.service.wm.returnvendor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.line.MesWmReturnVendorLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnvendor.vo.line.MesWmReturnVendorLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnvendor.MesWmReturnVendorLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnvendor.MesWmReturnVendorDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.returnvendor.MesWmReturnVendorLineMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.batch.MesWmBatchService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_RETURN_VENDOR_LINE_NOT_EXISTS;

/**
 * MES 供应商退货单行 Service 实现类
 */
@Service
@Validated
public class MesWmReturnVendorLineServiceImpl implements MesWmReturnVendorLineService {

    @Resource
    private MesWmReturnVendorLineMapper returnVendorLineMapper;

    @Resource
    @Lazy
    private MesWmReturnVendorService returnVendorService;
    @Resource
    @Lazy
    private MesWmReturnVendorDetailService returnVendorDetailService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesWmBatchService batchService;

    @Override
    public Long createReturnVendorLine(MesWmReturnVendorLineSaveReqVO createReqVO) {
        // 校验数据
        validateReturnVendorLineSaveData(createReqVO);

        // 插入
        MesWmReturnVendorLineDO line = BeanUtils.toBean(createReqVO, MesWmReturnVendorLineDO.class);
        returnVendorLineMapper.insert(line);
        return line.getId();
    }

    @Override
    public void updateReturnVendorLine(MesWmReturnVendorLineSaveReqVO updateReqVO) {
        // 校验存在
        MesWmReturnVendorLineDO oldLine = validateReturnVendorLineExists(updateReqVO.getId());
        // 固定父单 ID，防止通过接口篡改
        updateReqVO.setReturnId(oldLine.getReturnId());
        // 校验数据
        validateReturnVendorLineSaveData(updateReqVO);

        // 更新
        MesWmReturnVendorLineDO updateObj = BeanUtils.toBean(updateReqVO, MesWmReturnVendorLineDO.class);
        returnVendorLineMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReturnVendorLine(Long id) {
        // 1.1 校验存在
        MesWmReturnVendorLineDO line = validateReturnVendorLineExists(id);
        // 1.2 校验父数据为草稿状态
        returnVendorService.validateReturnVendorExistsAndPrepare(line.getReturnId());

        // 2.1 级联删除该行下的明细
        returnVendorDetailService.deleteReturnVendorDetailByLineId(id);
        // 2.2 删除行
        returnVendorLineMapper.deleteById(id);
    }

    @Override
    public MesWmReturnVendorLineDO getReturnVendorLine(Long id) {
        return returnVendorLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesWmReturnVendorLineDO> getReturnVendorLinePage(MesWmReturnVendorLinePageReqVO pageReqVO) {
        return returnVendorLineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesWmReturnVendorLineDO> getReturnVendorLineListByReturnId(Long returnId) {
        return returnVendorLineMapper.selectListByReturnId(returnId);
    }

    @Override
    public void deleteReturnVendorLineByReturnId(Long returnId) {
        returnVendorLineMapper.deleteByReturnId(returnId);
    }

    @Override
    public MesWmReturnVendorLineDO validateReturnVendorLineExists(Long id) {
        MesWmReturnVendorLineDO line = returnVendorLineMapper.selectById(id);
        if (line == null) {
            throw exception(WM_RETURN_VENDOR_LINE_NOT_EXISTS);
        }
        return line;
    }

    private void validateReturnVendorLineSaveData(MesWmReturnVendorLineSaveReqVO reqVO) {
        // 校验父数据存在且为草稿状态
        MesWmReturnVendorDO returnVendor = returnVendorService.validateReturnVendorExistsAndPrepare(reqVO.getReturnId());
        // 校验物料存在
        itemService.validateItemExistsAndEnable(reqVO.getItemId());
        // 校验批次存在且属于当前物料和供应商
        if (reqVO.getBatchId() != null) {
            batchService.validateBatchExists(reqVO.getBatchId(), reqVO.getItemId(), null, returnVendor.getVendorId());
        }
    }

}
