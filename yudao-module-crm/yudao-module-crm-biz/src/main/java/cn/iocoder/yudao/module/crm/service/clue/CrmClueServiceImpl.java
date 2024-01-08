package cn.iocoder.yudao.module.crm.service.clue;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.*;
import cn.iocoder.yudao.module.crm.convert.clue.CrmClueConvert;
import cn.iocoder.yudao.module.crm.convert.customer.CrmCustomerConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.dal.mysql.clue.CrmClueMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CLUE_NOT_EXISTS;

/**
 * 线索 Service 实现类
 *
 * @author Wanwan
 */
@Service
@Validated
public class CrmClueServiceImpl implements CrmClueService {

    @Resource
    private CrmClueMapper clueMapper;

    @Resource
    private CrmCustomerService customerService;
    @Resource
    private CrmPermissionService crmPermissionService;

    @Override
    public Long createClue(CrmClueCreateReqVO createReqVO) {
        // 如果传入客户，校验客户是否存在
        if (Objects.nonNull(createReqVO.getCustomerId())) {
            customerService.validateCustomer(createReqVO.getCustomerId());
        }
        // 插入
        CrmClueDO clue = CrmClueConvert.INSTANCE.convert(createReqVO);
        clueMapper.insert(clue);
        System.out.println(1);
        // 返回
        return clue.getId();
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_LEADS, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateClue(CrmClueUpdateReqVO updateReqVO) {
        // 校验存在
        validateClueExists(updateReqVO.getId());
        // 如果传入客户，校验客户是否存在
        if (Objects.nonNull(updateReqVO.getCustomerId())) {
            customerService.validateCustomer(updateReqVO.getCustomerId());
        }

        // 更新
        CrmClueDO updateObj = CrmClueConvert.INSTANCE.convert(updateReqVO);
        clueMapper.updateById(updateObj);
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_LEADS, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteClue(Long id) {
        // 校验存在
        validateClueExists(id);
        // 删除
        clueMapper.deleteById(id);
        // 删除数据权限
        crmPermissionService.deletePermission(CrmBizTypeEnum.CRM_LEADS.getType(), id);
    }

    private void validateClueExists(Long id) {
        if (clueMapper.selectById(id) == null) {
            throw exception(CLUE_NOT_EXISTS);
        }
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_LEADS, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmClueDO getClue(Long id) {
        return clueMapper.selectById(id);
    }

    @Override
    public List<CrmClueDO> getClueList(Collection<Long> ids, Long userId) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return clueMapper.selectBatchIds(ids, userId);
    }

    @Override
    public PageResult<CrmClueDO> getCluePage(CrmCluePageReqVO pageReqVO, Long userId) {
        return clueMapper.selectPage(pageReqVO, userId);
    }

    @Override
    public void transferClue(CrmClueTransferReqVO reqVO, Long userId) {
        // 1 校验线索是否存在
        validateClueExists(reqVO.getId());

        // 2.1 数据权限转移
        crmPermissionService.transferPermission(CrmClueConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_LEADS.getType()));
        // 2.2 设置新的负责人
        clueMapper.updateOwnerUserIdById(reqVO.getId(), reqVO.getNewOwnerUserId());

        // 3. TODO 记录转移日志
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void translate(CrmClueTransformReqVO reqVO, Long userId) {
        List<CrmClueDO> clues = getClueList(reqVO.getIds(), userId);
        // 不存在抛出异常
        if (CollUtil.isEmpty(clues)) {
            throw exception(CLUE_NOT_EXISTS);
        }
        // 遍历线索，创建对应的客户
        clues.forEach(clueDO -> {
            // 创建客户
            customerService.createCustomer(CrmCustomerConvert.INSTANCE.convert(clueDO), userId);
            // 更新线索状态
            clueDO.setTransformStatus(Boolean.TRUE);
            clueMapper.updateById(clueDO);
        });
    }
}
