package cn.iocoder.yudao.module.bpm.service.cc;

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
}
