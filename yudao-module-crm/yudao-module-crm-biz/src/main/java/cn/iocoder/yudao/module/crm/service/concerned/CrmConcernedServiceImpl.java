package cn.iocoder.yudao.module.crm.service.concerned;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.dal.dataobject.concerned.CrmConcernedDO;
import cn.iocoder.yudao.module.crm.dal.mysql.concerned.CrmConcernedMapper;
import cn.iocoder.yudao.module.crm.service.concerned.bo.CrmConcernedCreateReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CRM_CONCERNED_NOT_EXISTS;

/**
 * CRM 关注的数据 Service 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class CrmConcernedServiceImpl implements CrmConcernedService {

    @Resource
    private CrmConcernedMapper concernedMapper;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConcerned(CrmConcernedCreateReqBO createReqBO) {
        // 1. 校验用户是否存在
        adminUserApi.validateUserList(Collections.singletonList(createReqBO.getUserId()));

        // 2. 创建
        CrmConcernedDO concerned = BeanUtils.toBean(createReqBO, CrmConcernedDO.class);
        concernedMapper.insert(concerned);
        return concerned.getId();
    }

    @Override
    public void createConcernedBatch(Collection<CrmConcernedCreateReqBO> createReqBO) {
        // 1. 校验用户是否存在
        adminUserApi.validateUserList(convertSet(createReqBO, CrmConcernedCreateReqBO::getUserId));

        // 2. 创建
        List<CrmConcernedDO> concernedList = convertList(createReqBO, item -> BeanUtils.toBean(item, CrmConcernedDO.class));
        concernedMapper.insertBatch(concernedList);
    }

    @Override
    public void deleteConcerned(Integer bizType, Collection<Long> bizIds, Long userId) {
        // 1. 查询关注数据
        List<CrmConcernedDO> concernedList = concernedMapper.selectList(bizType, bizIds, userId);
        if (ObjUtil.notEqual(bizIds.size(), concernedList.size())) {
            throw exception(CRM_CONCERNED_NOT_EXISTS);
        }

        // 2. 删除
        concernedMapper.deleteBatchIds(convertList(concernedList, CrmConcernedDO::getId));
    }

}
