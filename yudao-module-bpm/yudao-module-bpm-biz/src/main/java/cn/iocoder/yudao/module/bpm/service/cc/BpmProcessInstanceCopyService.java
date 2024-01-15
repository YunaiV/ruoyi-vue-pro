package cn.iocoder.yudao.module.bpm.service.cc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCCMyPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCCPageItemRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCCReqVO;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfo;

/**
 * 流程抄送Service接口
 *
 * 现在是在审批的时候进行流程抄送
 */
public interface BpmProcessInstanceCopyService {

    /**
     * 查询流程抄送
     *
     * @param copyId 流程抄送主键
     * @return 流程抄送
     */
    BpmProcessInstanceCopyVO queryById(Long copyId);

    /**
     * 抄送
     * @param sourceInfo 抄送源信息，方便抄送处理
     * @return
     */
    boolean makeCopy(BpmCandidateSourceInfo sourceInfo);

    /**
     * 流程实例的抄送
     * @param loginUserId 当前登录用户
     * @param createReqVO 创建的抄送请求
     * @return 是否抄送成功，抄送成功则返回true
     */
    boolean ccProcessInstance(Long loginUserId, BpmProcessInstanceCCReqVO createReqVO);

    /**
     * 抄送的流程
     * @param loginUserId 登录用户id
     * @param pageReqVO 分页请求
     * @return 抄送的分页结果
     */
    PageResult<BpmProcessInstanceCCPageItemRespVO> getMyProcessInstanceCCPage(Long loginUserId, BpmProcessInstanceCCMyPageReqVO pageReqVO);
}
