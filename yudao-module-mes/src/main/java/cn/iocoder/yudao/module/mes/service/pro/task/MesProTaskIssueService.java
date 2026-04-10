package cn.iocoder.yudao.module.mes.service.pro.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskIssuePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskIssueSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskIssueDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 生产任务投料 Service 接口
 *
 * @author 芋道源码
 */
public interface MesProTaskIssueService {

    /**
     * 创建生产任务投料
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTaskIssue(@Valid MesProTaskIssueSaveReqVO createReqVO);

    /**
     * 更新生产任务投料
     *
     * @param updateReqVO 更新信息
     */
    void updateTaskIssue(@Valid MesProTaskIssueSaveReqVO updateReqVO);

    /**
     * 删除生产任务投料
     *
     * @param id 编号
     */
    void deleteTaskIssue(Long id);

    /**
     * 获得生产任务投料
     *
     * @param id 编号
     * @return 生产任务投料
     */
    MesProTaskIssueDO getTaskIssue(Long id);

    /**
     * 获得生产任务投料分页
     *
     * @param pageReqVO 分页查询
     * @return 生产任务投料分页
     */
    PageResult<MesProTaskIssueDO> getTaskIssuePage(MesProTaskIssuePageReqVO pageReqVO);

    /**
     * 根据任务编号，获得投料列表
     *
     * @param taskId 任务编号
     * @return 投料列表
     */
    List<MesProTaskIssueDO> getTaskIssueListByTaskId(Long taskId);

}
