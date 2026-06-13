package cn.iocoder.yudao.module.mes.service.dv.checkrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.line.MesDvCheckRecordLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.line.MesDvCheckRecordLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkrecord.MesDvCheckRecordLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.dv.checkrecord.MesDvCheckRecordLineMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.DV_CHECK_RECORD_LINE_NOT_EXISTS;

/**
 * MES 设备点检记录明细 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesDvCheckRecordLineServiceImpl implements MesDvCheckRecordLineService {

    @Resource
    private MesDvCheckRecordLineMapper checkRecordLineMapper;

    @Resource
    @Lazy
    private MesDvCheckRecordService checkRecordService;

    @Override
    public Long createCheckRecordLine(MesDvCheckRecordLineSaveReqVO createReqVO) {
        // 1. 校验点检记录存在且为草稿状态
        checkRecordService.validateCheckRecordDraft(createReqVO.getRecordId());

        // 2. 插入
        MesDvCheckRecordLineDO line = BeanUtils.toBean(createReqVO, MesDvCheckRecordLineDO.class);
        checkRecordLineMapper.insert(line);
        return line.getId();
    }

    @Override
    public void updateCheckRecordLine(MesDvCheckRecordLineSaveReqVO updateReqVO) {
        // 1.1 校验行存在
        validateCheckRecordLineExists(updateReqVO.getId());
        // 1.2 校验点检记录存在且为草稿状态
        checkRecordService.validateCheckRecordDraft(updateReqVO.getRecordId());

        // 2. 更新
        MesDvCheckRecordLineDO updateObj = BeanUtils.toBean(updateReqVO, MesDvCheckRecordLineDO.class);
        checkRecordLineMapper.updateById(updateObj);
    }

    @Override
    public void deleteCheckRecordLine(Long id) {
        // 1. 校验存在
        validateCheckRecordLineExists(id);

        // 2. 校验父记录为草稿状态
        MesDvCheckRecordLineDO line = checkRecordLineMapper.selectById(id);
        checkRecordService.validateCheckRecordDraft(line.getRecordId());
        // 3. 删除
        checkRecordLineMapper.deleteById(id);
    }

    @Override
    public void deleteByRecordId(Long recordId) {
        checkRecordLineMapper.deleteByRecordId(recordId);
    }

    @Override
    public void createCheckRecordLineList(List<MesDvCheckRecordLineDO> lines) {
        checkRecordLineMapper.insertBatch(lines);
    }

    @Override
    public void validateCheckRecordLineExists(Long id) {
        if (checkRecordLineMapper.selectById(id) == null) {
            throw exception(DV_CHECK_RECORD_LINE_NOT_EXISTS);
        }
    }

    @Override
    public MesDvCheckRecordLineDO getCheckRecordLine(Long id) {
        return checkRecordLineMapper.selectById(id);
    }

    @Override
    public PageResult<MesDvCheckRecordLineDO> getCheckRecordLinePage(MesDvCheckRecordLinePageReqVO pageReqVO) {
        return checkRecordLineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesDvCheckRecordLineDO> getCheckRecordLineListByRecordId(Long recordId) {
        return checkRecordLineMapper.selectListByRecordId(recordId);
    }

}
