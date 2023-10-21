package cn.iocoder.yudao.module.crm.service.receivable;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import java.util.*;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivableDO;

import cn.iocoder.yudao.module.crm.convert.receivable.ReceivableConvert;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.ReceivableMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;

/**
 * 回款管理 Service 实现类
 *
 * @author 赤焰
 */
@Service
@Validated
public class ReceivableServiceImpl implements ReceivableService {

    @Resource
    private ReceivableMapper receivableMapper;

    @Override
    public Long createReceivable(ReceivableCreateReqVO createReqVO) {
        // 插入
        ReceivableDO receivable = ReceivableConvert.INSTANCE.convert(createReqVO);
        receivableMapper.insert(receivable);
        // 返回
        return receivable.getId();
    }

    @Override
    public void updateReceivable(ReceivableUpdateReqVO updateReqVO) {
        // 校验存在
        validateReceivableExists(updateReqVO.getId());
        // 更新
        ReceivableDO updateObj = ReceivableConvert.INSTANCE.convert(updateReqVO);
        receivableMapper.updateById(updateObj);
    }

    @Override
    public void deleteReceivable(Long id) {
        // 校验存在
        validateReceivableExists(id);
        // 删除
        receivableMapper.deleteById(id);
    }

    private void validateReceivableExists(Long id) {
        if (receivableMapper.selectById(id) == null) {
            throw exception(RECEIVABLE_NOT_EXISTS);
        }
    }

    @Override
    public ReceivableDO getReceivable(Long id) {
        return receivableMapper.selectById(id);
    }

    @Override
    public List<ReceivableDO> getReceivableList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return receivableMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ReceivableDO> getReceivablePage(ReceivablePageReqVO pageReqVO) {
        return receivableMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ReceivableDO> getReceivableList(ReceivableExportReqVO exportReqVO) {
        return receivableMapper.selectList(exportReqVO);
    }

}
