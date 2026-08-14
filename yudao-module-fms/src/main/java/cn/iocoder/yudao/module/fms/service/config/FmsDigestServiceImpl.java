package cn.iocoder.yudao.module.fms.service.config;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.digest.FmsDigestSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsDigestDO;
import cn.iocoder.yudao.module.fms.dal.mysql.config.FmsDigestMapper;
import cn.iocoder.yudao.module.fms.service.config.FmsAccountSetService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.fms.enums.ErrorCodeConstants.DIGEST_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_DIGEST_CREATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_DIGEST_CREATE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_DIGEST_DELETE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_DIGEST_DELETE_SUCCESS;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_DIGEST_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_DIGEST_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.fms.enums.LogRecordConstants.FMS_DIGEST_UPDATE_SUCCESS;

/**
 * FMS 常用摘要 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class FmsDigestServiceImpl implements FmsDigestService {

    @Resource
    private FmsDigestMapper digestMapper;

    @Resource
    private FmsAccountSetService accountSetService;

    @Override
    @LogRecord(type = FMS_DIGEST_TYPE, subType = FMS_DIGEST_CREATE_SUB_TYPE,
            bizNo = "{{#digestId}}", success = FMS_DIGEST_CREATE_SUCCESS)
    public Long createDigest(FmsDigestSaveReqVO createReqVO, Long userId) {
        // 1. 校验账套写权限
        accountSetService.validateAccountSetWritePermission(createReqVO.getAccountSetId(), userId);

        // 2. 创建常用摘要
        FmsDigestDO digest = BeanUtils.toBean(createReqVO, FmsDigestDO.class)
                .setId(null);
        digestMapper.insert(digest);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("digestId", digest.getId());
        return digest.getId();
    }

    @Override
    @LogRecord(type = FMS_DIGEST_TYPE, subType = FMS_DIGEST_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = FMS_DIGEST_UPDATE_SUCCESS)
    public void updateDigest(FmsDigestSaveReqVO updateReqVO, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(updateReqVO.getAccountSetId(), userId);
        // 1.2 校验常用摘要
        validateDigestExists(updateReqVO.getAccountSetId(), updateReqVO.getId());

        // 2. 更新常用摘要
        digestMapper.updateById(BeanUtils.toBean(updateReqVO, FmsDigestDO.class));
    }

    @Override
    @LogRecord(type = FMS_DIGEST_TYPE, subType = FMS_DIGEST_DELETE_SUB_TYPE,
            bizNo = "{{#id}}", success = FMS_DIGEST_DELETE_SUCCESS)
    public void deleteDigest(Long accountSetId, Long id, Long userId) {
        // 1.1 校验账套写权限
        accountSetService.validateAccountSetWritePermission(accountSetId, userId);
        // 1.2 校验常用摘要
        FmsDigestDO digest = validateDigestExists(accountSetId, id);

        // 2. 删除常用摘要
        digestMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("digest", digest);
    }

    @Override
    public List<FmsDigestDO> getDigestList(Long accountSetId, Long userId) {
        // 1. 校验账套读权限
        accountSetService.validateAccountSetReadPermission(accountSetId, userId);

        // 2. 查询常用摘要列表
        return digestMapper.selectListByAccountSetId(accountSetId);
    }

    @Override
    public FmsDigestDO validateDigestExists(Long accountSetId, Long id) {
        FmsDigestDO digest = digestMapper.selectById(id);
        if (digest == null || ObjUtil.notEqual(digest.getAccountSetId(), accountSetId)) {
            throw exception(DIGEST_NOT_EXISTS);
        }
        return digest;
    }

}
