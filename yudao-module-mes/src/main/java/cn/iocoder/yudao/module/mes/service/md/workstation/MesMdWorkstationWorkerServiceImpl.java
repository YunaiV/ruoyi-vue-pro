package cn.iocoder.yudao.module.mes.service.md.workstation;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.worker.MesMdWorkstationWorkerSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationWorkerDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.workstation.MesMdWorkstationWorkerMapper;
import cn.iocoder.yudao.module.system.api.dept.PostApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 人力资源 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdWorkstationWorkerServiceImpl implements MesMdWorkstationWorkerService {

    @Resource
    private MesMdWorkstationWorkerMapper workstationWorkerMapper;

    @Resource
    private PostApi postApi;

    @Override
    public Long createWorkstationWorker(MesMdWorkstationWorkerSaveReqVO createReqVO) {
        // 校验数据
        validateWorkstationWorkerSaveData(null, createReqVO);

        // 插入
        MesMdWorkstationWorkerDO worker = BeanUtils.toBean(createReqVO, MesMdWorkstationWorkerDO.class);
        workstationWorkerMapper.insert(worker);
        return worker.getId();
    }

    @Override
    public void updateWorkstationWorker(MesMdWorkstationWorkerSaveReqVO updateReqVO) {
        // 校验存在
        validateWorkstationWorkerExists(updateReqVO.getId());
        // 校验数据
        validateWorkstationWorkerSaveData(updateReqVO.getId(), updateReqVO);

        // 更新
        MesMdWorkstationWorkerDO updateObj = BeanUtils.toBean(updateReqVO, MesMdWorkstationWorkerDO.class);
        workstationWorkerMapper.updateById(updateObj);
    }

    private void validateWorkstationWorkerSaveData(Long id, MesMdWorkstationWorkerSaveReqVO reqVO) {
        // 校验岗位是否存在
        postApi.validPostList(Collections.singleton(reqVO.getPostId()));
        // 校验同一工作站下岗位不重复（排除自身）
        MesMdWorkstationWorkerDO existing = workstationWorkerMapper.selectByWorkstationIdAndPostId(
                reqVO.getWorkstationId(), reqVO.getPostId());
        if (existing != null && ObjUtil.notEqual(existing.getId(), id)) {
            throw exception(MD_WORKSTATION_WORKER_POST_EXISTS);
        }
    }

    @Override
    public void deleteWorkstationWorker(Long id) {
        // 校验存在
        validateWorkstationWorkerExists(id);

        // 删除
        workstationWorkerMapper.deleteById(id);
    }

    private void validateWorkstationWorkerExists(Long id) {
        if (workstationWorkerMapper.selectById(id) == null) {
            throw exception(MD_WORKSTATION_WORKER_NOT_EXISTS);
        }
    }

    @Override
    public List<MesMdWorkstationWorkerDO> getWorkstationWorkerListByWorkstationId(Long workstationId) {
        return workstationWorkerMapper.selectListByWorkstationId(workstationId);
    }

    @Override
    public void deleteWorkstationWorkerByWorkstationId(Long workstationId) {
        workstationWorkerMapper.deleteByWorkstationId(workstationId);
    }

}
