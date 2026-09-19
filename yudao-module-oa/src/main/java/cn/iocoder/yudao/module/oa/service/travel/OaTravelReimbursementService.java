package cn.iocoder.yudao.module.oa.service.travel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import jakarta.validation.Valid;
import cn.iocoder.yudao.module.oa.controller.admin.travel.vo.reimbursement.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.travel.OaTravelReimbursementDO;

/**
 * 出差报销 Service 接口
 *
 * @author 芋道源码
 */
public interface OaTravelReimbursementService {

    /**
     * 创建出差报销草稿
     *
     * @param reqVO 单据内容
     * @param userId 当前用户编号
     * @return 单据编号
     */
    Long createTravelReimbursement(@Valid OaTravelReimbursementSaveReqVO reqVO, Long userId);

    /**
     * 更新出差报销草稿
     *
     * @param reqVO 单据内容
     * @param userId 当前用户编号
     */
    void updateTravelReimbursement(@Valid OaTravelReimbursementSaveReqVO reqVO, Long userId);

    /**
     * 提交已保存的出差报销
     *
     * @param submitReqVO 单据编号及发起人选择的审批人
     * @param userId 当前用户编号
     */
    void submitTravelReimbursement(OaTravelReimbursementSubmitReqVO submitReqVO, Long userId);

    /**
     * 撤回本人的审批中出差报销
     *
     * @param id 单据编号
     * @param userId 当前用户编号
     */
    void cancelTravelReimbursement(Long id, Long userId);

    /**
     * 删除本人允许删除状态的出差报销
     *
     * @param id 单据编号
     * @param userId 当前用户编号
     */
    void deleteTravelReimbursement(Long id, Long userId);

    /**
     * 获得出差报销详情
     *
     * @param id 单据编号
     * @return 单据
     */
    OaTravelReimbursementDO getTravelReimbursement(Long id);

    /**
     * 获得本人出差报销分页
     *
     * @param userId 当前用户编号
     * @param reqVO 分页条件
     * @return 本人单据分页
     */
    PageResult<OaTravelReimbursementDO> getTravelReimbursementPage(Long userId, OaTravelReimbursementPageReqVO reqVO);

    /**
     * 更新出差报销审批状态
     *
     * @param id 单据编号
     * @param status 审批状态
     */
    void updateTravelReimbursementStatus(Long id, Integer status);

}
