package cn.iocoder.yudao.module.point.service.pointrecord;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.point.controller.admin.pointrecord.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.pointrecord.PointRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.point.convert.pointrecord.PointRecordConvert;
import cn.iocoder.yudao.module.point.dal.mysql.pointrecord.PointRecordMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.point.enums.ErrorCodeConstants.*;

/**
 * 用户积分记录 Service 实现类
 *
 * @author QingX
 */
@Service
@Validated
public class PointRecordServiceImpl implements PointRecordService {

    @Resource
    private PointRecordMapper recordMapper;

    @Override
    public Long createRecord(PointRecordCreateReqVO createReqVO) {
        // 插入
        PointRecordDO record = PointRecordConvert.INSTANCE.convert(createReqVO);
        recordMapper.insert(record);
        // 返回
        return record.getId();
    }

    @Override
    public void updateRecord(PointRecordUpdateReqVO updateReqVO) {
        // 校验存在
        validateRecordExists(updateReqVO.getId());
        // 更新
        PointRecordDO updateObj = PointRecordConvert.INSTANCE.convert(updateReqVO);
        recordMapper.updateById(updateObj);
    }

    @Override
    public void deleteRecord(Long id) {
        // 校验存在
        validateRecordExists(id);
        // 删除
        recordMapper.deleteById(id);
    }

    private void validateRecordExists(Long id) {
        if (recordMapper.selectById(id) == null) {
            throw exception(RECORD_NOT_EXISTS);
        }
    }

    @Override
    public PointRecordDO getRecord(Long id) {
        return recordMapper.selectById(id);
    }

    @Override
    public List<PointRecordDO> getRecordList(Collection<Long> ids) {
        return recordMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PointRecordDO> getRecordPage(PointRecordPageReqVO pageReqVO) {
        return recordMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PointRecordDO> getRecordList(PointRecordExportReqVO exportReqVO) {
        return recordMapper.selectList(exportReqVO);
    }

}
