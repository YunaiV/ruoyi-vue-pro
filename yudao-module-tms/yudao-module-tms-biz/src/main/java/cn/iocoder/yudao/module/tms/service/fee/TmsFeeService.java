package cn.iocoder.yudao.module.tms.service.fee;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeePageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeeSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.fee.TmsFeeDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 出运订单费用明细 Service 接口
 *
 * @author wdy
 */
public interface TmsFeeService {

    /**
     * 根据费用查询条件获取头程单ID列表
     *
     * @param reqVO 费用查询条件
     * @return 头程单ID列表
     */
    List<Long> selectFirstMileIdsByFeePageReqVO(TmsFeePageReqVO reqVO);


    /**
     * 创建出运订单费用明细
     *
     * @param createReqVO 创建信息
     * @param sourceType  源类型
     * @return 编号
     */
    Long createFee(@Valid TmsFeeSaveReqVO createReqVO, Integer sourceType);

    /**
     * 更新出运订单费用明细
     *
     * @param updateReqVO 更新信息
     * @param sourceType  源类型
     */
    void updateFee(@Valid TmsFeeSaveReqVO updateReqVO, Integer sourceType);

    /**
     * 删除出运订单费用明细
     *
     * @param id         编号
     * @param sourceType 源类型
     */
    void deleteFee(Long id, Integer sourceType);

    /**
     * 获得出运订单费用明细
     *
     * @param sourceId   原单ID
     * @param sourceType 源类型
     * @return 出运订单费用明细
     */
    List<TmsFeeDO> getFee(Long sourceId, Integer sourceType);

    /**
     * 获得出运订单费用明细分页
     *
     * @param pageReqVO 分页查询
     * @return 出运订单费用明细分页
     */
    PageResult<TmsFeeDO> getFeePage(TmsFeePageReqVO pageReqVO);

    /**
     * 批量创建出运订单费用明细
     *
     * @param feeList    创建信息列表
     * @param sourceType 源类型
     * @return 编号列表
     */
    List<Long> createFeeList(List<TmsFeeDO> feeList, Integer sourceType);

    /**
     * 批量更新出运订单费用明细
     *
     * @param sourceId   源单ID
     * @param sourceType 原单类型
     * @param list       更新信息列表
     */
    void updateFeeList(@NotNull(message = "更新时源单ID不能为空") Long sourceId, @NotNull(message = "更新时源单类型不能为空") Integer sourceType, List<? extends TmsFeeSaveReqVO> list);

    /**
     * 批量删除出运订单费用明细
     *
     * @param ids        fee表IDs
     * @param sourceType 源类型
     */
    void deleteFeeList(List<Long> ids, Integer sourceType);

    /**
     * 批量删除出运订单费用明细,通过原单ID，原单类型
     *
     * @param sourceId   费用ID
     * @param sourceType 源类型
     */
    Integer deleteFeeListBySourceIdAndSourceType(Long sourceId, Integer sourceType);
}