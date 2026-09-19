package cn.iocoder.yudao.module.oa.service.seal;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.seal.vo.apply.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.seal.OaSealApplyDO;

import javax.validation.Valid;

/**
 * 用印申请 Service 接口
 *
 * @author 芋道源码
 */
public interface OaSealApplyService {

    /**
     * 创建用印草稿
     *
     * @param reqVO 申请信息
     * @param userId 申请人
     * @return 申请编号
     */
    Long createSealApply(@Valid OaSealApplySaveReqVO reqVO, Long userId);

    /**
     * 更新本人用印草稿
     *
     * @param reqVO 申请信息
     * @param userId 申请人
     */
    void updateSealApply(@Valid OaSealApplySaveReqVO reqVO, Long userId);

    /**
     * 删除本人用印草稿
     *
     * @param id 申请编号
     * @param userId 申请人
     */
    void deleteSealApply(Long id, Long userId);

    /**
     * 提交用印申请
     *
     * @param id 申请编号
     * @param userId 申请人
     * @return 流程实例编号
     */
    String submitSealApply(Long id, Long userId);

    /**
     * 撤销审批中的用印申请
     *
     * @param id 申请编号
     * @param userId 申请人
     */
    void cancelSealApply(Long id, Long userId);

    /**
     * 回写用印审批结果
     *
     * @param id 申请编号
     * @param status 审批结果
     */
    void updateSealApplyStatus(Long id, Integer status);

    /**
     * 获得用印申请
     *
     * @param id 申请编号
     * @return 用印申请
     */
    OaSealApplyDO getSealApply(Long id);

    /**
     * 获得本人申请分页
     *
     * @param userId 申请人
     * @param reqVO 分页条件
     * @return 申请分页
     */
    PageResult<OaSealApplyDO> getSealApplyPage(Long userId, OaSealApplyPageReqVO reqVO);

    /**
     * 获得引用印章的单据数量
     *
     * @param id 印章编号
     * @return 单据数量
     */
    Long getSealApplyCountBySealId(Long id);

}
