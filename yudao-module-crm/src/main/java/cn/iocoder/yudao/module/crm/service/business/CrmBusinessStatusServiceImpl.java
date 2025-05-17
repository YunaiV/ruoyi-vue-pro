package cn.iocoder.yudao.module.crm.service.business;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.dal.mysql.business.CrmBusinessStatusMapper;
import cn.iocoder.yudao.module.crm.dal.mysql.business.CrmBusinessStatusTypeMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.diffList;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

/**
 * 商机状态 Service 实现类
 *
 * @author ljlleo
 */
@Service
@Validated
public class CrmBusinessStatusServiceImpl implements CrmBusinessStatusService {

    @Resource
    private CrmBusinessStatusTypeMapper businessStatusTypeMapper;
    @Resource
    private CrmBusinessStatusMapper businessStatusMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private CrmBusinessService businessService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBusinessStatus(CrmBusinessStatusSaveReqVO createReqVO) {
        // 1.1 检验名称是否存在
        validateBusinessStatusTypeNameUnique(createReqVO.getName(), null);
        // 1.2 设置状态的排序
        int sort = 0;
        for (CrmBusinessStatusSaveReqVO.Status status : createReqVO.getStatuses()) {
            status.setSort(sort++);
        }

        // 2.1 插入类型
        CrmBusinessStatusTypeDO statusType = BeanUtils.toBean(createReqVO, CrmBusinessStatusTypeDO.class);
        businessStatusTypeMapper.insert(statusType);
        // 2.2 插入状态
        List<CrmBusinessStatusDO> statuses = BeanUtils.toBean(createReqVO.getStatuses(), CrmBusinessStatusDO.class,
                status -> status.setTypeId(statusType.getId()));
        businessStatusMapper.insertBatch(statuses);
        return statusType.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBusinessStatus(CrmBusinessStatusSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateBusinessStatusTypeExists(updateReqVO.getId());
        // 1.2 校验名称是否存在
        validateBusinessStatusTypeNameUnique(updateReqVO.getName(), updateReqVO.getId());
        // 1.3 设置状态的排序
        int sort = 0;
        for (CrmBusinessStatusSaveReqVO.Status status : updateReqVO.getStatuses()) {
            status.setSort(sort++);
        }
        // 1.4 已经使用，无法更新
        if (businessService.getBusinessCountByStatusTypeId(updateReqVO.getId()) > 0) {
            throw exception(BUSINESS_STATUS_UPDATE_FAIL_USED);
        }

        // 2.1 更新类型
        CrmBusinessStatusTypeDO updateObj = BeanUtils.toBean(updateReqVO, CrmBusinessStatusTypeDO.class);
        businessStatusTypeMapper.updateById(updateObj);
        // 2.2 更新状态
        updateBusinessStatus(updateReqVO.getId(), BeanUtils.toBean(updateReqVO.getStatuses(), CrmBusinessStatusDO.class));
    }

    private void updateBusinessStatus(Long id, List<CrmBusinessStatusDO> newList) {
        List<CrmBusinessStatusDO> oldList = businessStatusMapper.selectListByTypeId(id);
        List<List<CrmBusinessStatusDO>> diffList = diffList(oldList, newList, // id 不同，就认为是不同的记录
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setTypeId(id));
            businessStatusMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            businessStatusMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            businessStatusMapper.deleteBatchIds(convertSet(diffList.get(2), CrmBusinessStatusDO::getId));
        }
    }

    private void validateBusinessStatusTypeExists(Long id) {
        if (businessStatusTypeMapper.selectById(id) == null) {
            throw exception(BUSINESS_STATUS_TYPE_NOT_EXISTS);
        }
    }

    private void validateBusinessStatusTypeNameUnique(String name, Long id) {
        CrmBusinessStatusTypeDO statusType = businessStatusTypeMapper.selectByName(name);
        if (statusType == null
                || statusType.getId().equals(id)) {
            return;
        }
        throw exception(BUSINESS_STATUS_TYPE_NAME_EXISTS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBusinessStatusType(Long id) {
        // 1.1 校验存在
        validateBusinessStatusTypeExists(id);
        // 1.2 已经使用，无法更新
        if (businessService.getBusinessCountByStatusTypeId(id) > 0) {
            throw exception(BUSINESS_STATUS_DELETE_FAIL_USED);
        }

        // 2.1 删除类型
        businessStatusTypeMapper.deleteById(id);
        // 2.2 删除状态
        businessStatusMapper.deleteByTypeId(id);
    }

    @Override
    public CrmBusinessStatusTypeDO getBusinessStatusType(Long id) {
        return businessStatusTypeMapper.selectById(id);
    }

    @Override
    public void validateBusinessStatusType(Long id) {
        validateBusinessStatusTypeExists(id);
    }

    @Override
    public List<CrmBusinessStatusTypeDO> getBusinessStatusTypeList() {
        return businessStatusTypeMapper.selectList();
    }

    @Override
    public PageResult<CrmBusinessStatusTypeDO> getBusinessStatusTypePage(PageParam pageReqVO) {
        return businessStatusTypeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmBusinessStatusTypeDO> getBusinessStatusTypeList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return businessStatusTypeMapper.selectBatchIds(ids);
    }

    @Override
    public List<CrmBusinessStatusDO> getBusinessStatusListByTypeId(Long typeId) {
        List<CrmBusinessStatusDO> list = businessStatusMapper.selectListByTypeId(typeId);
        list.sort(Comparator.comparingInt(CrmBusinessStatusDO::getSort));
        return list;
    }

    @Override
    public List<CrmBusinessStatusDO> getBusinessStatusList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return businessStatusMapper.selectBatchIds(ids);
    }

    @Override
    public CrmBusinessStatusDO getBusinessStatus(Long id) {
        return businessStatusMapper.selectById(id);
    }

    @Override
    public CrmBusinessStatusDO validateBusinessStatus(Long statusTypeId, Long statusId) {
        CrmBusinessStatusDO status = businessStatusMapper.selectByTypeIdAndId(statusTypeId, statusId);
        if (status == null) {
            throw exception(BUSINESS_STATUS_NOT_EXISTS);
        }
        return status;
    }

}
