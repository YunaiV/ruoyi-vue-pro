package cn.iocoder.yudao.module.crm.service.clue;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.CrmCluePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.CrmClueSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.CrmClueTransferReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.CrmClueTransformReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerSaveReqVO;
import cn.iocoder.yudao.module.crm.convert.clue.CrmClueConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.dal.mysql.clue.CrmClueMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;

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

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    // TODO @min：补充相关几个方法的操作日志；
    public Long createClue(CrmClueSaveReqVO createReqVO) {
        // 校验关联数据
        validateRelationDataExists(createReqVO);

        // 插入
        CrmClueDO clue = BeanUtils.toBean(createReqVO, CrmClueDO.class);
        clueMapper.insert(clue);
        // 返回
        return clue.getId();
    }

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_LEADS, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateClue(CrmClueSaveReqVO updateReqVO) {
        // 校验线索是否存在
        validateClueExists(updateReqVO.getId());
        // 校验关联数据
        validateRelationDataExists(updateReqVO);

        // 更新
        CrmClueDO updateObj = BeanUtils.toBean(updateReqVO, CrmClueDO.class);
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
    public void translateCustomer(CrmClueTransformReqVO reqVO, Long userId) {
        // 校验线索都存在
        Set<Long> clueIds = reqVO.getIds();
        List<CrmClueDO> clues = getClueList(clueIds, userId);
        if (CollUtil.isEmpty(clues) || ObjectUtil.notEqual(clues.size(), clueIds.size())) {
            // 提示不存在的线索编号
            clueIds.removeAll(convertSet(clues, CrmClueDO::getId));
            throw exception(ANY_CLUE_NOT_EXISTS, clueIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }

        // 过滤出未转化的客户
        List<CrmClueDO> unTransformClues = clues.stream()
                .filter(clue -> ObjectUtil.notEqual(Boolean.TRUE, clue.getTransformStatus())).toList();
        // 传入的线索中包含已经转化的情况，抛出业务异常
        if (ObjectUtil.notEqual(clues.size(), unTransformClues.size())) {
            // 提示已经转化的线索编号
            clueIds.removeAll(convertSet(unTransformClues, CrmClueDO::getId));
            throw exception(ANY_CLUE_ALREADY_TRANSLATED, clueIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }

        // 遍历线索(未转化的线索)，创建对应的客户
        unTransformClues.forEach(clue -> {
            // 1. 创建客户
            CrmCustomerSaveReqVO customerSaveReqVO = BeanUtils.toBean(clue, CrmCustomerSaveReqVO.class).setId(null);
            Long customerId = customerService.createCustomer(customerSaveReqVO, userId);
            // TODO @puhui999：如果有跟进记录，需要一起转过去；
            // 2. 更新线索
            clueMapper.updateById(new CrmClueDO().setId(clue.getId())
                    .setTransformStatus(Boolean.TRUE).setCustomerId(customerId));
        });
    }

    private void validateRelationDataExists(CrmClueSaveReqVO reqVO) {
        // 校验负责人
        if (Objects.nonNull(reqVO.getOwnerUserId()) &&
                Objects.isNull(adminUserApi.getUser(reqVO.getOwnerUserId()))) {
            throw exception(USER_NOT_EXISTS);
        }
    }

}
