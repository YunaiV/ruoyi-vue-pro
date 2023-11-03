package cn.iocoder.yudao.module.crm.service.receivable;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.ReceivablePlanCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.ReceivablePlanExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.ReceivablePlanPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.ReceivablePlanUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.receivable.ReceivablePlanConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.ReceivablePlanDO;
import cn.iocoder.yudao.module.crm.dal.mysql.receivable.ReceivablePlanMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.RECEIVABLE_PLAN_NOT_EXISTS;

/**
 * 回款计划 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ReceivablePlanServiceImpl implements ReceivablePlanService {

    @Resource
    private ReceivablePlanMapper receivablePlanMapper;

    @Override
    public Long createReceivablePlan(ReceivablePlanCreateReqVO createReqVO) {
        // 插入
        ReceivablePlanDO receivablePlan = ReceivablePlanConvert.INSTANCE.convert(createReqVO);
        // TODO @liuhongfeng：空格要注释；if (ObjectUtil.isNull(receivablePlan.getStatus())) {
        if(ObjectUtil.isNull(receivablePlan.getStatus())){
            receivablePlan.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        receivablePlanMapper.insert(receivablePlan);
        // 返回
        return receivablePlan.getId();
    }

    @Override
    public void updateReceivablePlan(ReceivablePlanUpdateReqVO updateReqVO) {
        // 校验存在
        validateReceivablePlanExists(updateReqVO.getId());

        // 更新
        ReceivablePlanDO updateObj = ReceivablePlanConvert.INSTANCE.convert(updateReqVO);
        receivablePlanMapper.updateById(updateObj);
    }

    @Override
    public void deleteReceivablePlan(Long id) {
        // 校验存在
        validateReceivablePlanExists(id);
        // 删除
        receivablePlanMapper.deleteById(id);
    }

    private void validateReceivablePlanExists(Long id) {
        if (receivablePlanMapper.selectById(id) == null) {
            throw exception(RECEIVABLE_PLAN_NOT_EXISTS);
        }
    }

    @Override
    public ReceivablePlanDO getReceivablePlan(Long id) {
        return receivablePlanMapper.selectById(id);
    }

    @Override
    public List<ReceivablePlanDO> getReceivablePlanList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return receivablePlanMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ReceivablePlanDO> getReceivablePlanPage(ReceivablePlanPageReqVO pageReqVO) {
        return receivablePlanMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ReceivablePlanDO> getReceivablePlanList(ReceivablePlanExportReqVO exportReqVO) {
        return receivablePlanMapper.selectList(exportReqVO);
    }

}
