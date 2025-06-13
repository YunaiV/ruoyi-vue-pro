package cn.iocoder.yudao.module.tms.service.transfer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.api.utils.Validation;
import cn.iocoder.yudao.module.tms.api.transfer.dto.TmsTransferStatusUpdateDTO;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.vo.*;
import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.TmsTransferDO;
import cn.iocoder.yudao.module.tms.service.bo.transfer.TmsTransferBO;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 调拨单 Service 接口
 *
 * @author wdy
 */
@Validated
public interface TmsTransferService {

    /**
     * 创建调拨单
     *
     * @param createReqVO 创建信息
     * @return 调拨单编号
     */
    Long createTransfer(@Validated(Validation.OnCreate.class) TmsTransferSaveReqVO createReqVO);

    /**
     * 更新调拨单
     *
     * @param updateReqVO 更新信息
     */
    void updateTransfer(@Validated(Validation.OnUpdate.class) TmsTransferSaveReqVO updateReqVO);

    /**
     * 删除调拨单
     *
     * @param id 调拨单编号
     */
    void deleteTransfer(Long id);

    /**
     * 获得调拨单
     *
     * @param id 调拨单编号
     * @return 调拨单
     */
    TmsTransferDO getTransfer(Long id);

    /**
     * 获得调拨单 BO
     *
     * @param id 调拨单编号
     * @return 调拨单 BO
     */
    TmsTransferBO getTransferBO(Long id);

    /**
     * 获得调拨单 VO
     *
     * @param id 调拨单编号
     * @return 调拨单 VO
     */
    TmsTransferRespVO getTransferRespVO(Long id);

    /**
     * 校验调拨单是否存在
     *
     * @param id 调拨单编号
     * @return 调拨单
     */
    TmsTransferDO validateTransferExists(Long id);

    /**
     * 获得调拨单 BO 分页
     *
     * @param pageReqVO 分页查询
     * @return 调拨单 BO 分页
     */
    PageResult<TmsTransferBO> getTransferBOPage(TmsTransferPageReqVO pageReqVO);

    /**
     * 获得调拨单 VO 分页
     *
     * @param pageReqVO 分页查询
     * @return 调拨单 VO 分页
     */
    PageResult<TmsTransferRespVO> getTmsTransferRespVOPage(TmsTransferPageReqVO pageReqVO);

    /**
     * 切换调拨单状态
     *
     * @param reqVO 状态切换请求
     */
    void switchOpen(TmsTransferOffStatusReqVO reqVO);

    /**
     * 审核调拨单
     *
     * @param reqVO 审核请求
     */
    void review(TmsTransferAuditReqVO reqVO);

    /**
     * 提交调拨单审核
     *
     * @param transferIds 调拨单编号列表
     */
    void submitAudit(@Size(min = 1, message = "提交审核的单据数量不小于1") List<Long> transferIds);

    /**
     * 更新调拨单状态,入库，出库
     *
     * @param updateDTO 更新信息
     */
    void updateTransferStatus(TmsTransferStatusUpdateDTO updateDTO);

    /**
     * 获取可售库存数量
     *
     * @param reqVO 查询参数
     * @return 可售库存数量信息
     */
    TmsTransferSellableQtyRespVO getSellableQty(TmsTransferSellableQtyReqVO reqVO);

}