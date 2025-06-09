package cn.iocoder.yudao.module.tms.service.fee;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeePageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeeSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.fee.TmsFeeDO;
import cn.iocoder.yudao.module.tms.dal.mysql.fee.TmsFeeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.tms.enums.TmsErrorCodeConstants.FEE_NOT_EXISTS;

/**
 * 出运订单费用明细 Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
public class TmsFeeServiceImpl implements TmsFeeService {

    @Resource
    private TmsFeeMapper feeMapper;

    @Override
    public Long createFee(TmsFeeSaveReqVO createReqVO, Integer sourceType) {
        // 插入
        TmsFeeDO fee = BeanUtils.toBean(createReqVO, TmsFeeDO.class);
        fee.setUpstreamType(sourceType);
        feeMapper.insert(fee);
        // 返回
        return fee.getId();
    }

    @Override
    public void updateFee(TmsFeeSaveReqVO updateReqVO, Integer sourceType) {
        // 校验存在
        validateFeeExists(updateReqVO.getId(), sourceType);
        // 更新
        TmsFeeDO updateObj = BeanUtils.toBean(updateReqVO, TmsFeeDO.class, peek -> peek.setUpstreamType(sourceType));
        feeMapper.updateById(updateObj);
    }

    @Override
    public void deleteFee(Long id, Integer sourceType) {
        // 校验存在
        validateFeeExists(id, sourceType);
        // 删除
        feeMapper.deleteByIdAndType(id, sourceType);
    }

    private void validateFeeExists(Long id, Integer sourceType) {
        if (feeMapper.selectBySourceIdAndSourceType(id, sourceType) == null) {
            throw exception(FEE_NOT_EXISTS, id, Objects.requireNonNull(BillType.parse(sourceType)).getLabel());
        }
    }

    @Override
    public List<TmsFeeDO> getFee(Long sourceId, Integer sourceType) {
        return feeMapper.selectBySourceIdAndSourceType(sourceId, sourceType);
    }

    @Override
    public PageResult<TmsFeeDO> getFeePage(TmsFeePageReqVO pageReqVO) {
        return feeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<Long> selectFirstMileIdsByFeePageReqVO(TmsFeePageReqVO reqVO) {
        return feeMapper.selectFirstMileIdsByFeePageReqVO(reqVO);
    }

    @Override
    public List<Long> createFeeList(List<TmsFeeDO> feeList, Integer sourceType) {
        if (CollUtil.isEmpty(feeList)) {
            return Collections.emptyList();
        }
        // 设置源类型
        feeList.forEach(fee -> fee.setUpstreamType(sourceType));
        // 批量插入
        feeMapper.insertBatch(feeList);
        // 返回 ID 列表
        return feeList.stream().map(TmsFeeDO::getId).toList();
    }

    private void updateFeeList(List<TmsFeeDO> feeList, Integer sourceType) {
        if (CollUtil.isEmpty(feeList)) {
            return;
        }
        // 校验存在
        feeList.forEach(fee -> validateFeeExists(fee.getId(), sourceType));
        // 设置源类型
        feeList.forEach(fee -> fee.setUpstreamType(sourceType));
        // 批量更新
        feeMapper.updateBatch(feeList);
    }

    @Override
    public void updateFeeList(Long sourceId, Integer sourceType, List<? extends TmsFeeSaveReqVO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<TmsFeeDO> oldList = this.getFee(sourceId, sourceType);
        List<TmsFeeDO> newList = BeanUtils.toBean(list, TmsFeeDO.class, peek -> peek.setUpstreamType(sourceType));

        //截取ID来区分新增、更新、删除
        List<List<TmsFeeDO>> diffedList = CollectionUtils.diffList(oldList, newList, (oldVal, newVal) -> ObjectUtil.equal(oldVal.getId(), newVal.getId()));

        if (CollUtil.isNotEmpty(diffedList.get(0))) {
            this.createFeeList(diffedList.get(0), sourceType);
        }
        if (CollUtil.isNotEmpty(diffedList.get(1))) {
            this.updateFeeList(diffedList.get(1), sourceType);
        }
        if (CollUtil.isNotEmpty(diffedList.get(2))) {
            List<Long> deleteIds = CollectionUtils.convertList(diffedList.get(2), TmsFeeDO::getId);
            this.deleteFeeList(deleteIds, sourceType);
        }
    }

    @Override
    public void deleteFeeList(List<Long> ids, Integer sourceType) {
        ids = ids.stream().distinct().toList();
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 校验存在
        ids.forEach(id -> validateFeeExists(id, sourceType));
        // 批量删除
        feeMapper.deleteBatchIdsBySourceType(ids, sourceType);
    }

    @Override
    public Integer deleteFeeListBySourceIdAndSourceType(Long sourceId, Integer sourceType) {
        return feeMapper.deleteBySourceIdAndSourceType(sourceId, sourceType);
    }
}