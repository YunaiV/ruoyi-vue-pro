package cn.iocoder.yudao.module.mes.service.pro.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskIssuePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskIssueSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskIssueDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.task.MesProTaskIssueMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.PRO_TASK_ISSUE_NOT_EXISTS;

/**
 * MES 生产任务投料 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProTaskIssueServiceImpl implements MesProTaskIssueService {

    @Resource
    private MesProTaskIssueMapper taskIssueMapper;

    @Resource
    private MesProTaskService taskService;
    @Resource
    private MesMdItemService itemService;

    @Override
    public Long createTaskIssue(MesProTaskIssueSaveReqVO createReqVO) {
        // 1. 校验关联数据存在
        taskService.validateTaskExists(createReqVO.getTaskId());
        if (createReqVO.getItemId() != null) {
            itemService.validateItemExists(createReqVO.getItemId());
        }
        // 2. 插入
        MesProTaskIssueDO taskIssue = BeanUtils.toBean(createReqVO, MesProTaskIssueDO.class);
        taskIssueMapper.insert(taskIssue);
        return taskIssue.getId();
    }

    @Override
    public void updateTaskIssue(MesProTaskIssueSaveReqVO updateReqVO) {
        // 1. 校验存在
        validateTaskIssueExists(updateReqVO.getId());
        // 2. 校验关联数据存在
        if (updateReqVO.getItemId() != null) {
            itemService.validateItemExists(updateReqVO.getItemId());
        }
        // 3. 更新
        MesProTaskIssueDO updateObj = BeanUtils.toBean(updateReqVO, MesProTaskIssueDO.class);
        taskIssueMapper.updateById(updateObj);
    }

    @Override
    public void deleteTaskIssue(Long id) {
        // 1. 校验存在
        validateTaskIssueExists(id);

        // 2. 删除
        taskIssueMapper.deleteById(id);
    }

    @Override
    public MesProTaskIssueDO getTaskIssue(Long id) {
        return taskIssueMapper.selectById(id);
    }

    @Override
    public PageResult<MesProTaskIssueDO> getTaskIssuePage(MesProTaskIssuePageReqVO pageReqVO) {
        return taskIssueMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesProTaskIssueDO> getTaskIssueListByTaskId(Long taskId) {
        return taskIssueMapper.selectListByTaskId(taskId);
    }

    @Override
    public Long getTaskIssueCountByUnitMeasureId(Long unitMeasureId) {
        return taskIssueMapper.selectCountByUnitMeasureId(unitMeasureId);
    }

    private void validateTaskIssueExists(Long id) {
        if (taskIssueMapper.selectById(id) == null) {
            throw exception(PRO_TASK_ISSUE_NOT_EXISTS);
        }
    }

}
