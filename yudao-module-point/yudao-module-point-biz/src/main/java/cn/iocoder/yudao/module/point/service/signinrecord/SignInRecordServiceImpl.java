package cn.iocoder.yudao.module.point.service.signinrecord;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.point.controller.admin.signinrecord.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.signinrecord.SignInRecordDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.point.convert.signinrecord.SignInRecordConvert;
import cn.iocoder.yudao.module.point.dal.mysql.signinrecord.SignInRecordMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.point.enums.ErrorCodeConstants.*;

/**
 * 用户签到积分 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SignInRecordServiceImpl implements SignInRecordService {

    @Resource
    private SignInRecordMapper signInRecordMapper;

    @Override
    public Long createSignInRecord(SignInRecordCreateReqVO createReqVO) {
        // 插入
        SignInRecordDO signInRecord = SignInRecordConvert.INSTANCE.convert(createReqVO);
        signInRecordMapper.insert(signInRecord);
        // 返回
        return signInRecord.getId();
    }

    @Override
    public void updateSignInRecord(SignInRecordUpdateReqVO updateReqVO) {
        // 校验存在
        validateSignInRecordExists(updateReqVO.getId());
        // 更新
        SignInRecordDO updateObj = SignInRecordConvert.INSTANCE.convert(updateReqVO);
        signInRecordMapper.updateById(updateObj);
    }

    @Override
    public void deleteSignInRecord(Long id) {
        // 校验存在
        validateSignInRecordExists(id);
        // 删除
        signInRecordMapper.deleteById(id);
    }

    private void validateSignInRecordExists(Long id) {
        if (signInRecordMapper.selectById(id) == null) {
            throw exception(SIGN_IN_RECORD_NOT_EXISTS);
        }
    }

    @Override
    public SignInRecordDO getSignInRecord(Long id) {
        return signInRecordMapper.selectById(id);
    }

    @Override
    public List<SignInRecordDO> getSignInRecordList(Collection<Long> ids) {
        return signInRecordMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SignInRecordDO> getSignInRecordPage(SignInRecordPageReqVO pageReqVO) {
        return signInRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SignInRecordDO> getSignInRecordList(SignInRecordExportReqVO exportReqVO) {
        return signInRecordMapper.selectList(exportReqVO);
    }

}
