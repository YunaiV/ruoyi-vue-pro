package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemBoardDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemBoardMapper;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * PMS 工作项看板列 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsWorkItemBoardServiceImpl implements PmsWorkItemBoardService {

    @Resource
    private PmsWorkItemBoardMapper workItemBoardMapper;

    @Override
    public List<PmsWorkItemBoardDO> getWorkItemBoardList(Long projectId, Integer workItemType) {
        return workItemBoardMapper.selectListByProjectIdAndWorkItemType(projectId, workItemType);
    }

    @Override
    public void createWorkItemBoard(PmsWorkItemBoardDO board) {
        workItemBoardMapper.insert(board);
    }

    @Override
    public void createWorkItemBoardList(Collection<PmsWorkItemBoardDO> boards) {
        if (CollUtil.isEmpty(boards)) {
            return;
        }
        workItemBoardMapper.insertBatch(boards);
    }

    @Override
    public void updateWorkItemBoardList(Collection<PmsWorkItemBoardDO> boards) {
        if (CollUtil.isEmpty(boards)) {
            return;
        }
        workItemBoardMapper.updateBatch(boards);
    }

    @Override
    public void deleteWorkItemBoardList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        workItemBoardMapper.deleteByIds(ids);
    }

    @Override
    public void deleteWorkItemBoardListByProjectId(Long projectId) {
        workItemBoardMapper.deleteByProjectId(projectId);
    }

}
