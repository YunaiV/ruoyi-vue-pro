package cn.iocoder.yudao.module.crm.service.business;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.type.CrmBusinessStatusTypePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.type.CrmBusinessStatusTypeQueryVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.type.CrmBusinessStatusTypeSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.dal.mysql.business.CrmBusinessStatusMapper;
import cn.iocoder.yudao.module.crm.dal.mysql.business.CrmBusinessStatusTypeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_STATUS_TYPE_NOT_EXISTS;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_STATUS_TYPE_NAME_EXISTS;

/**
 * 商机状态类型 Service 实现类
 *
 * @author ljlleo
 */
@Service
@Validated
public class CrmBusinessStatusTypeServiceImpl implements CrmBusinessStatusTypeService {

    @Resource
    private CrmBusinessStatusTypeMapper businessStatusTypeMapper;

    @Resource
    private CrmBusinessStatusMapper businessStatusMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBusinessStatusType(CrmBusinessStatusTypeSaveReqVO createReqVO) {
        //检验名称是否存在
        validateBusinessStatusTypeExists(createReqVO.getName(), null);
        // 插入类型
        CrmBusinessStatusTypeDO businessStatusType = BeanUtils.toBean(createReqVO, CrmBusinessStatusTypeDO.class);
        businessStatusTypeMapper.insert(businessStatusType);
        // 插入状态
        if (CollUtil.isNotEmpty(createReqVO.getStatusList())) {
            createReqVO.getStatusList().forEach(status -> status.setTypeId(businessStatusType.getId()));
            businessStatusMapper.insertBatch(BeanUtils.toBean(createReqVO.getStatusList(), CrmBusinessStatusDO.class));
        }
        return businessStatusType.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBusinessStatusType(CrmBusinessStatusTypeSaveReqVO updateReqVO) {
        // 校验存在
        validateBusinessStatusTypeExists(updateReqVO.getId());
        // 校验名称是否存在
        validateBusinessStatusTypeExists(updateReqVO.getName(), updateReqVO.getId());
        // 更新类型
        CrmBusinessStatusTypeDO updateObj = BeanUtils.toBean(updateReqVO, CrmBusinessStatusTypeDO.class);
        businessStatusTypeMapper.updateById(updateObj);
        // 更新状态（删除 + 更新）
        // TODO @ljlleo 可以参考 DeliveryExpressTemplateServiceImpl 的 updateExpressTemplateFree 方法；主要没变化的，还是不删除了哈。
        businessStatusMapper.delete(updateReqVO.getId());
        updateReqVO.getStatusList().forEach(status -> status.setTypeId(updateReqVO.getId()));
        businessStatusMapper.insertBatch(BeanUtils.toBean(updateReqVO.getStatusList(), CrmBusinessStatusDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBusinessStatusType(Long id) {
        // TODO 待添加被引用校验
        //...

        // 校验存在
        validateBusinessStatusTypeExists(id);
        // 删除类型
        businessStatusTypeMapper.deleteById(id);
        // 删除状态
        businessStatusMapper.delete(id);
    }

    private void validateBusinessStatusTypeExists(Long id) {
        if (businessStatusTypeMapper.selectById(id) == null) {
            throw exception(BUSINESS_STATUS_TYPE_NOT_EXISTS);
        }
    }

    // TODO @ljlleo 这个方法，这个参考 validateDeptNameUnique 实现。
    private void validateBusinessStatusTypeExists(String name, Long id) {
        LambdaQueryWrapper<CrmBusinessStatusTypeDO> wrapper = new LambdaQueryWrapperX<>();
        if(null != id) {
            wrapper.ne(CrmBusinessStatusTypeDO::getId, id);
        }
        long cnt = businessStatusTypeMapper.selectCount(wrapper.eq(CrmBusinessStatusTypeDO::getName, name));
        if (cnt > 0) {
            throw exception(BUSINESS_STATUS_TYPE_NAME_EXISTS);
        }
    }

    @Override
    public CrmBusinessStatusTypeDO getBusinessStatusType(Long id) {
        return businessStatusTypeMapper.selectById(id);
    }

    @Override
    public PageResult<CrmBusinessStatusTypeDO> getBusinessStatusTypePage(CrmBusinessStatusTypePageReqVO pageReqVO) {
        return businessStatusTypeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmBusinessStatusTypeDO> selectList(CrmBusinessStatusTypeQueryVO queryVO) {
        return businessStatusTypeMapper.selectList(queryVO);
    }

}
