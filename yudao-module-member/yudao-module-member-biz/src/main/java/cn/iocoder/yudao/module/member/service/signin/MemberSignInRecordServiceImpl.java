package cn.iocoder.yudao.module.member.service.signin;

import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordExportReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.MemberSignInRecordUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.member.convert.signin.MemberSignInRecordConvert;
import cn.iocoder.yudao.module.member.dal.mysql.signin.MemberSignInRecordMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.*;

/**
 * 用户签到积分 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MemberSignInRecordServiceImpl implements MemberSignInRecordService {

    @Resource
    private MemberSignInRecordMapper memberSignInRecordMapper;

    @Override
    public void deleteSignInRecord(Long id) {
        // 校验存在
        validateSignInRecordExists(id);
        // 删除
        memberSignInRecordMapper.deleteById(id);
    }

    private void validateSignInRecordExists(Long id) {
        if (memberSignInRecordMapper.selectById(id) == null) {
            throw exception(SIGN_IN_RECORD_NOT_EXISTS);
        }
    }

    @Override
    public MemberSignInRecordDO getSignInRecord(Long id) {
        return memberSignInRecordMapper.selectById(id);
    }

    @Override
    public List<MemberSignInRecordDO> getSignInRecordList(Collection<Long> ids) {
        return memberSignInRecordMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<MemberSignInRecordDO> getSignInRecordPage(MemberSignInRecordPageReqVO pageReqVO) {
        return memberSignInRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MemberSignInRecordDO> getSignInRecordList(MemberSignInRecordExportReqVO exportReqVO) {
        return memberSignInRecordMapper.selectList(exportReqVO);
    }

}
