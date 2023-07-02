package cn.iocoder.yudao.module.member.service.point;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.recrod.MemberPointRecordUpdateReqVO;
import cn.iocoder.yudao.module.member.convert.point.MemberPointRecordConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointRecordDO;
import cn.iocoder.yudao.module.member.dal.mysql.point.MemberPointRecordMapper;
import cn.iocoder.yudao.module.member.enums.ErrorCodeConstants;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;


/**
 * 用户积分记录 Service 实现类
 *
 * @author QingX
 */
@Service
@Validated
public class MemberPointRecordServiceImpl implements MemberPointRecordService {

    @Resource
    private MemberPointRecordMapper recordMapper;

    @Override
    public void updateRecord(MemberPointRecordUpdateReqVO updateReqVO) {
        // 校验存在
        validateRecordExists(updateReqVO.getId());
        // 更新
        MemberPointRecordDO updateObj = MemberPointRecordConvert.INSTANCE.convert(updateReqVO);
        recordMapper.updateById(updateObj);
    }

    private void validateRecordExists(Long id) {
        if (recordMapper.selectById(id) == null) {
            throw exception(ErrorCodeConstants.RECORD_NOT_EXISTS);
        }
    }

    @Override
    public MemberPointRecordDO getRecord(Long id) {
        return recordMapper.selectById(id);
    }

    @Override
    public PageResult<MemberPointRecordDO> getRecordPage(MemberPointRecordPageReqVO pageReqVO) {
        return recordMapper.selectPage(pageReqVO);
    }

}
