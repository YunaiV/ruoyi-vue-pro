package cn.iocoder.yudao.module.bpm.service.cc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCCMyPageReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCCPageItemRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCCReqVO;
import cn.iocoder.yudao.module.bpm.service.candidate.BpmCandidateSourceInfo;

// TODO @kyle：这个 Service 要不挪到 task 包下；保持统一，task 下有流程、任务、抄送等；
// TODO @kyle：中文和英文之间，有个空格，会更清晰点；例如说；流程抄送 Service 接口；中文写作习惯~
/**
 * 流程抄送Service接口
 *
 * 现在是在审批的时候进行流程抄送
 */
public interface BpmProcessInstanceCopyService {

    // TODO @kyle：无用的方法，可以去掉哈；另外，考虑到避免过多的 VO，这里就可以返回 BpmProcessInstanceCopyDO
    /**
     * 查询流程抄送
     *
     * @param copyId 流程抄送主键
     * @return 流程抄送
     */
    BpmProcessInstanceCopyVO queryById(Long copyId);

    // TODO 芋艿：这块要 review 下；思考下~~
    /**
     * 抄送
     * @param sourceInfo 抄送源信息，方便抄送处理
     * @return
     */
    boolean makeCopy(BpmCandidateSourceInfo sourceInfo);

    // TODO @kyle：可以方法名改成 createProcessInstanceCopy；现在项目一般新增都用 create 为主；
    /**
     * 流程实例的抄送
     *
     * @param userId 当前登录用户
     * @param createReqVO 创建的抄送请求
     * @return 是否抄送成功，抄送成功则返回true TODO @kyle：这里可以不用返回哈；目前一般是失败，就抛出业务异常；
     */
    boolean ccProcessInstance(Long userId, BpmProcessInstanceCCReqVO createReqVO);

    /**
     * 抄送的流程
     * @param loginUserId 登录用户id
     * @param pageReqVO 分页请求
     * @return 抄送的分页结果
     */
    PageResult<BpmProcessInstanceCCPageItemRespVO> getMyProcessInstanceCCPage(Long loginUserId,
                                                                              BpmProcessInstanceCCMyPageReqVO pageReqVO);
}
