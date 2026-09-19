package cn.iocoder.yudao.module.oa.service.regular;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplySaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplySubmitReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.regular.OaRegularApplyDO;

/**
 * 转正申请 Service 接口
 *
 * @author 芋道源码
 */
public interface OaRegularApplyService {

    /**
     * 创建转正申请草稿
     *
     * @param saveReqVO 申请内容
     * @return 申请编号
     */
    Long createRegularApply(OaRegularApplySaveReqVO saveReqVO);

    /**
     * 更新本人草稿
     *
     * @param saveReqVO 申请内容
     * @param userId 当前用户编号
     */
    void updateRegularApply(OaRegularApplySaveReqVO saveReqVO, Long userId);

    /**
     * 提交本人草稿并发起审批
     *
     * @param submitReqVO 申请编号和自选审批人
     * @param userId 当前用户编号
     */
    void submitRegularApply(OaRegularApplySubmitReqVO submitReqVO, Long userId);

    /**
     * 获得转正申请
     *
     * @param id 申请编号
     * @return 转正申请
     */
    OaRegularApplyDO getRegularApply(Long id);

    /**
     * 获得本人转正申请分页
     *
     * @param userId 申请人编号
     * @param pageReqVO 分页条件
     * @return 申请分页
     */
    PageResult<OaRegularApplyDO> getRegularApplyPage(Long userId, OaRegularApplyPageReqVO pageReqVO);

    /**
     * 更新转正申请审批状态
     *
     * @param id 申请编号
     * @param status 审批状态
     */
    void updateRegularApplyStatus(Long id, Integer status);

}
