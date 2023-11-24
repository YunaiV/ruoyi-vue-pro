package cn.iocoder.yudao.module.crm.service.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.CrmBusinessStatusTypePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.CrmBusinessStatusTypeQueryVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.CrmBusinessStatusTypeSaveReqVO;
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
        // 插入
        CrmBusinessStatusTypeDO businessStatusType = BeanUtils.toBean(createReqVO, CrmBusinessStatusTypeDO.class);
        businessStatusTypeMapper.insert(businessStatusType);
        createReqVO.getStatusList().stream().forEach(status -> {
            status.setTypeId(businessStatusType.getId());
        });
        //插入状态
        businessStatusMapper.insertBatch(BeanUtils.toBean(createReqVO.getStatusList(), CrmBusinessStatusDO.class));
        // 返回
        return businessStatusType.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBusinessStatusType(CrmBusinessStatusTypeSaveReqVO updateReqVO) {
        // 校验存在
        validateBusinessStatusTypeExists(updateReqVO.getId());
        // 校验名称是否存在
        validateBusinessStatusTypeExists(updateReqVO.getName(), updateReqVO.getId());
        // 更新
        CrmBusinessStatusTypeDO updateObj = BeanUtils.toBean(updateReqVO, CrmBusinessStatusTypeDO.class);
        businessStatusTypeMapper.updateById(updateObj);
        //删除状态
        businessStatusMapper.delete(updateReqVO.getId());
        //插入状态
        updateReqVO.getStatusList().stream().forEach(status -> {
            status.setTypeId(updateReqVO.getId());
        });
        businessStatusMapper.insertBatch(BeanUtils.toBean(updateReqVO.getStatusList(), CrmBusinessStatusDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBusinessStatusType(Long id) {
        //TODO 待添加被引用校验
        //...

        // 校验存在
        validateBusinessStatusTypeExists(id);
        // 删除
        businessStatusTypeMapper.deleteById(id);
        //删除状态
        businessStatusMapper.delete(id);
    }

    private void validateBusinessStatusTypeExists(Long id) {
        if (businessStatusTypeMapper.selectById(id) == null) {
            throw exception(BUSINESS_STATUS_TYPE_NOT_EXISTS);
        }
    }

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
