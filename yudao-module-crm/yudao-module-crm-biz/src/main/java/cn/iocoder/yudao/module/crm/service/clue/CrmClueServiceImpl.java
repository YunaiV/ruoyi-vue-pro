package cn.iocoder.yudao.module.crm.service.clue;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
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
import cn.iocoder.yudao.module.crm.service.clue.bo.CrmClueUpdateFollowUpReqBO;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.permission.CrmPermissionService;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.enums.LogRecordConstants.*;
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
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_LEADS_TYPE, subType = CRM_LEADS_CREATE_SUB_TYPE, bizNo = "{{#clue.id}}",
            success = CRM_LEADS_CREATE_SUCCESS)
    // TODO @min：补充相关几个方法的操作日志；
    public Long createClue(CrmClueSaveReqVO createReqVO) {
        // 1. 校验关联数据
        validateRelationDataExists(createReqVO);

        // 2. 插入
        CrmClueDO clue = BeanUtils.toBean(createReqVO, CrmClueDO.class);
        clueMapper.insert(clue);

        // 3. 创建数据权限
        CrmPermissionCreateReqBO createReqBO = new CrmPermissionCreateReqBO()
                .setBizType(CrmBizTypeEnum.CRM_LEADS.getType())
                .setBizId(clue.getId())
                // 设置当前操作的人为负责人
                .setUserId(getLoginUserId())
                .setLevel(CrmPermissionLevelEnum.OWNER.getLevel());
        crmPermissionService.createPermission(createReqBO);

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("clue", clue);
        return clue.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_LEADS_TYPE, subType = CRM_LEADS_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_LEADS_UPDATE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_LEADS, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateClue(CrmClueSaveReqVO updateReqVO) {
        Assert.notNull(updateReqVO.getId(), "线索编号不能为空");
        // 1. 校验线索是否存在
        CrmClueDO oldClue = validateClueExists(updateReqVO.getId());
        // 2. 校验关联数据
        validateRelationDataExists(updateReqVO);

        // 3. 更新
        CrmClueDO updateObj = BeanUtils.toBean(updateReqVO, CrmClueDO.class);
        clueMapper.updateById(updateObj);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldClue, CrmCustomerSaveReqVO.class));
        LogRecordContext.putVariable("clueName", oldClue.getName());

    }

    @Override
    public void updateClueFollowUp(CrmClueUpdateFollowUpReqBO clueUpdateFollowUpReqBO) {
        clueMapper.updateById(BeanUtils.toBean(clueUpdateFollowUpReqBO, CrmClueDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_LEADS_TYPE, subType = CRM_LEADS_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_LEADS_DELETE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_LEADS, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteClue(Long id) {
        // 1. 校验存在
        CrmClueDO clue = validateClueExists(id);

        // 2. 删除
        clueMapper.deleteById(id);

        // 3. 删除数据权限
        crmPermissionService.deletePermission(CrmBizTypeEnum.CRM_LEADS.getType(), id);

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("clueName", clue.getName());
    }

    private CrmClueDO validateClueExists(Long id) {
        CrmClueDO crmClueDO = clueMapper.selectById(id);
        if (crmClueDO == null) {
            throw exception(CLUE_NOT_EXISTS);
        }
        return crmClueDO;
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
            clueIds.removeAll(convertSet(clues, CrmClueDO::getId));
            throw exception(CLUE_ANY_CLUE_NOT_EXISTS, StrUtil.join(",", clueIds));
        }

        // 过滤出未转化的客户
        List<CrmClueDO> unTransformClues = CollectionUtils.filterList(clues,
                clue -> ObjectUtil.notEqual(Boolean.TRUE, clue.getTransformStatus()));
        // 存在已经转化的，直接提示哈。避免操作的用户，以为都转化成功了
        if (ObjectUtil.notEqual(clues.size(), unTransformClues.size())) {
            clueIds.removeAll(convertSet(unTransformClues, CrmClueDO::getId));
            throw exception(CLUE_ANY_CLUE_ALREADY_TRANSLATED, StrUtil.join(",", clueIds));
        }

        // 遍历线索(未转化的线索)，创建对应的客户
        unTransformClues.forEach(clue -> {
            // 1. 创建客户
            CrmCustomerSaveReqVO customerSaveReqVO = BeanUtils.toBean(clue, CrmCustomerSaveReqVO.class).setId(null);
            Long customerId = customerService.createCustomer(customerSaveReqVO, userId);
            // TODO @puhui999：如果有跟进记录，需要一起转过去；提问：艿艿这里是复制线索所有的跟进吗？还是直接把线索相关的跟进 bizType、bizId 全改为关联客户？
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
