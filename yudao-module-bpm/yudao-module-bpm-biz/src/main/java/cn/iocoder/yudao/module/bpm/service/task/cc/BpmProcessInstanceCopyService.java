package cn.iocoder.yudao.module.bpm.service.task.cc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyCreateReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance.BpmProcessInstanceCopyMyPageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.cc.BpmProcessInstanceCopyDO;

/**
 * 流程抄送 Service 接口
 *
 * 现在是在审批的时候进行流程抄送
 */
public interface BpmProcessInstanceCopyService {

    /**
     * 流程实例的抄送
     *
     * @param userId      当前登录用户
     * @param createReqVO 创建的抄送请求
     */
    void createProcessInstanceCopy(Long userId, BpmProcessInstanceCopyCreateReqVO createReqVO);

    /**
     * 抄送的流程的分页
     * @param userId 当前登录用户
     * @param pageReqVO 分页请求
     * @return 抄送的分页结果
     */
    PageResult<BpmProcessInstanceCopyDO> getMyProcessInstanceCopyPage(Long userId,
                                                                      BpmProcessInstanceCopyMyPageReqVO pageReqVO);
}
