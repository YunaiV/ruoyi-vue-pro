package cn.iocoder.yudao.module.crm.service.receivable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.ReceivableCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.ReceivableExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.ReceivablePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.ReceivableUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.receivable.ReceivableConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivableDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.ReceivableMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.RECEIVABLE_NOT_EXISTS;

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
        // TODO @liuhongfeng：planId 是否存在，是否合法，需要去校验；
        // TODO @liuhongfeng：其它类似 customerId、contractId 也需要去校验；
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
