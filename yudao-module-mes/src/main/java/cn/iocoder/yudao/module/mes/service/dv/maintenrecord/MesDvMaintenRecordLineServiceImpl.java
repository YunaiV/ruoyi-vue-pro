package cn.iocoder.yudao.module.mes.service.dv.maintenrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.line.MesDvMaintenRecordLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.line.MesDvMaintenRecordLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.maintenrecord.MesDvMaintenRecordLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.dv.maintenrecord.MesDvMaintenRecordLineMapper;
import cn.iocoder.yudao.module.mes.service.dv.subject.MesDvSubjectService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 设备保养记录明细 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesDvMaintenRecordLineServiceImpl implements MesDvMaintenRecordLineService {

    @Resource
    private MesDvMaintenRecordLineMapper maintenRecordLineMapper;

    @Resource
    @Lazy
    private MesDvMaintenRecordService maintenRecordService;
    @Resource
    private MesDvSubjectService subjectService;

    @Override
    public Long createMaintenRecordLine(MesDvMaintenRecordLineSaveReqVO createReqVO) {
        // 1. 校验关联数据
        validateMaintenRecordLineRelation(createReqVO);

        // 2. 插入
        MesDvMaintenRecordLineDO maintenRecordLine = BeanUtils.toBean(createReqVO, MesDvMaintenRecordLineDO.class);
        maintenRecordLineMapper.insert(maintenRecordLine);
        return maintenRecordLine.getId();
    }

    @Override
    public void updateMaintenRecordLine(MesDvMaintenRecordLineSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateMaintenRecordLineExists(updateReqVO.getId());
        // 1.2 校验关联数据
        validateMaintenRecordLineRelation(updateReqVO);

        // 2. 更新
        MesDvMaintenRecordLineDO updateObj = BeanUtils.toBean(updateReqVO, MesDvMaintenRecordLineDO.class);
        maintenRecordLineMapper.updateById(updateObj);
    }

    @Override
    public void deleteMaintenRecordLine(Long id) {
        // 1.1 校验存在
        MesDvMaintenRecordLineDO line = validateMaintenRecordLineExists(id);
        // 1.2 校验保养记录为草稿状态
        maintenRecordService.validateMaintenRecordDraft(line.getRecordId());

        // 2. 删除
        maintenRecordLineMapper.deleteById(id);
    }

    private void validateMaintenRecordLineRelation(MesDvMaintenRecordLineSaveReqVO reqVO) {
        // 校验保养记录为草稿状态
        maintenRecordService.validateMaintenRecordDraft(reqVO.getRecordId());
        // 校验保养项目是否存在
        subjectService.validateSubjectExistsAndEnable(reqVO.getSubjectId());
    }

    private MesDvMaintenRecordLineDO validateMaintenRecordLineExists(Long id) {
        MesDvMaintenRecordLineDO maintenRecordLine = maintenRecordLineMapper.selectById(id);
        if (maintenRecordLine == null) {
            throw exception(MAINTEN_RECORD_LINE_NOT_EXISTS);
        }
        return maintenRecordLine;
    }

    @Override
    public MesDvMaintenRecordLineDO getMaintenRecordLine(Long id) {
        return maintenRecordLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesDvMaintenRecordLineDO> getMaintenRecordLinePage(MesDvMaintenRecordLinePageReqVO pageReqVO) {
        return maintenRecordLineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesDvMaintenRecordLineDO> getMaintenRecordLineListByRecordId(Long recordId) {
        return maintenRecordLineMapper.selectListByRecordId(recordId);
    }

    @Override
    public void deleteMaintenRecordLineByRecordId(Long recordId) {
        maintenRecordLineMapper.deleteByRecordId(recordId);
    }

}
