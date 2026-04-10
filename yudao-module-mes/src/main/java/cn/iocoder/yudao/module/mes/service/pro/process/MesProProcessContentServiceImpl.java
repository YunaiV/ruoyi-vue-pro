package cn.iocoder.yudao.module.mes.service.pro.process;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.process.vo.content.MesProProcessContentSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessContentDO;
import cn.iocoder.yudao.module.mes.dal.mysql.pro.process.MesProProcessContentMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.PRO_PROCESS_CONTENT_NOT_EXISTS;

/**
 * MES 生产工序内容 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesProProcessContentServiceImpl implements MesProProcessContentService {

    @Resource
    private MesProProcessContentMapper processContentMapper;

    @Resource
    @Lazy
    private MesProProcessService processService;

    @Override
    public Long createProcessContent(MesProProcessContentSaveReqVO createReqVO) {
        // 校验工序存在
        processService.validateProcessExists(createReqVO.getProcessId());

        // 插入
        MesProProcessContentDO content = BeanUtils.toBean(createReqVO, MesProProcessContentDO.class);
        processContentMapper.insert(content);
        return content.getId();
    }

    @Override
    public void updateProcessContent(MesProProcessContentSaveReqVO updateReqVO) {
        // 校验存在
        validateProcessContentExists(updateReqVO.getId());

        // 更新
        MesProProcessContentDO updateObj = BeanUtils.toBean(updateReqVO, MesProProcessContentDO.class);
        processContentMapper.updateById(updateObj);
    }

    @Override
    public void deleteProcessContent(Long id) {
        // 校验存在
        validateProcessContentExists(id);

        // 删除
        processContentMapper.deleteById(id);
    }

    private void validateProcessContentExists(Long id) {
        if (processContentMapper.selectById(id) == null) {
            throw exception(PRO_PROCESS_CONTENT_NOT_EXISTS);
        }
    }

    @Override
    public MesProProcessContentDO getProcessContent(Long id) {
        return processContentMapper.selectById(id);
    }

    @Override
    public List<MesProProcessContentDO> getProcessContentListByProcessId(Long processId) {
        return processContentMapper.selectListByProcessId(processId);
    }

    @Override
    public void deleteProcessContentByProcessId(Long processId) {
        processContentMapper.deleteByProcessId(processId);
    }

}
