package cn.iocoder.yudao.module.crm.service.clue;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.CrmCluePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.CrmClueSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.CrmClueTransferReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.CrmClueTranslateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerSaveReqVO;
import cn.iocoder.yudao.module.crm.convert.clue.CrmClueConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import cn.iocoder.yudao.module.crm.dal.mysql.clue.CrmClueMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.framework.permission.core.annotations.CrmPermission;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.customer.bo.CrmCustomerCreateReqBO;
import cn.iocoder.yudao.module.crm.service.followup.CrmFollowUpRecordService;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmFollowUpCreateReqBO;
import cn.iocoder.yudao.module.crm.service.followup.bo.CrmUpdateFollowUpReqBO;
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

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.enums.LogRecordConstants.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;
import static java.util.Collections.singletonList;

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
    private CrmFollowUpRecordService followUpRecordService;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_LEADS_TYPE, subType = CRM_LEADS_CREATE_SUB_TYPE, bizNo = "{{#clue.id}}",
            success = CRM_LEADS_CREATE_SUCCESS)
    public Long createClue(CrmClueSaveReqVO createReqVO, Long userId) {
        // 1.1 校验关联数据
        validateRelationDataExists(createReqVO);
        // 1.2 校验负责人是否存在
        if (createReqVO.getOwnerUserId() != null) {
            adminUserApi.validateUserList(singletonList(createReqVO.getOwnerUserId()));
        } else {
            createReqVO.setOwnerUserId(userId); // 如果没有设置负责人那么默认操作人为负责人
        }

        // 2. 插入
        CrmClueDO clue = BeanUtils.toBean(createReqVO, CrmClueDO.class).setId(null);
        clueMapper.insert(clue);

        // 3. 创建数据权限
        CrmPermissionCreateReqBO createReqBO = new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_LEADS.getType())
                .setBizId(clue.getId()).setUserId(clue.getOwnerUserId()).setLevel(CrmPermissionLevelEnum.OWNER.getLevel());
        crmPermissionService.createPermission(createReqBO);

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("clue", clue);
        return clue.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_LEADS_TYPE, subType = CRM_LEADS_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_LEADS_UPDATE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_LEADS, bizId = "#updateReq.id", level = CrmPermissionLevelEnum.OWNER)
    public void updateClue(CrmClueSaveReqVO updateReq) {
        Assert.notNull(updateReq.getId(), "线索编号不能为空");
        // 1. 校验线索是否存在
        CrmClueDO oldClue = validateClueExists(updateReq.getId());
        // 2. 校验关联数据
        validateRelationDataExists(updateReq);

        // 3. 更新
        CrmClueDO updateObj = BeanUtils.toBean(updateReq, CrmClueDO.class);
        clueMapper.updateById(updateObj);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldClue, CrmCustomerSaveReqVO.class));
        LogRecordContext.putVariable("clueName", oldClue.getName());
    }

    @Override
    @LogRecord(type = CRM_LEADS_TYPE, subType = CRM_LEADS_UPDATE_SUB_TYPE, bizNo = "{{#updateReq.bizId}",
            success = CRM_LEADS_UPDATE_SUCCESS)
    public void updateClueFollowUp(CrmUpdateFollowUpReqBO updateReq) {
        // 校验线索是否存在
        CrmClueDO oldClue = validateClueExists(updateReq.getBizId());

        // 更新
        clueMapper.updateById(BeanUtils.toBean(updateReq, CrmClueDO.class).setId(updateReq.getBizId()));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldClue, CrmUpdateFollowUpReqBO.class));
        LogRecordContext.putVariable("clueName", oldClue.getName());

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

        // 4. 删除跟进
        followUpRecordService.deleteFollowUpRecordByBiz(CrmBizTypeEnum.CRM_LEADS.getType(), id);

        // 记录操作日志上下文
        LogRecordContext.putVariable("clueName", clue.getName());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_LEADS_TYPE, subType = CRM_LEADS_TRANSFER_SUB_TYPE, bizNo = "{{#reqVO.id}}",
            success = CRM_LEADS_TRANSFER_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_LEADS, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void transferClue(CrmClueTransferReqVO reqVO, Long userId) {
        // 1 校验线索是否存在
        CrmClueDO clue = validateClueExists(reqVO.getId());

        // 2.1 数据权限转移
        crmPermissionService.transferPermission(CrmClueConvert.INSTANCE.convert(reqVO, userId).setBizType(CrmBizTypeEnum.CRM_LEADS.getType()));
        // 2.2 设置新的负责人
        clueMapper.updateOwnerUserIdById(reqVO.getId(), reqVO.getNewOwnerUserId());

        // 3. 记录转移日志
        LogRecordContext.putVariable("clue", clue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_LEADS, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void translateCustomer(CrmClueTranslateReqVO reqVO, Long userId) {
        // 1.1 校验线索都存在
        Set<Long> clueIds = reqVO.getIds();
        List<CrmClueDO> clues = getClueList(clueIds, userId);
        if (CollUtil.isEmpty(clues) || ObjectUtil.notEqual(clues.size(), clueIds.size())) {
            clueIds.removeAll(convertSet(clues, CrmClueDO::getId));
            throw exception(CLUE_ANY_CLUE_NOT_EXISTS, clueIds);
        }
        // 1.2 存在已经转化的，直接提示哈。避免操作的用户，以为都转化成功了
        List<CrmClueDO> translatedClues = filterList(clues,
                clue -> ObjectUtil.equal(Boolean.TRUE, clue.getTransformStatus()));
        if (CollUtil.isNotEmpty(translatedClues)) {
            throw exception(CLUE_ANY_CLUE_ALREADY_TRANSLATED, convertSet(translatedClues, CrmClueDO::getId));
        }

        // 2. 遍历线索(未转化的线索)，创建对应的客户
        // TODO @puhui999：这里不用过滤了；
        List<CrmClueDO> translateClues = filterList(clues, clue -> ObjUtil.equal(Boolean.FALSE, clue.getTransformStatus()));
        List<CrmCustomerDO> customers = customerService.createCustomerBatch(convertList(translateClues, clue ->
                BeanUtils.toBean(clue, CrmCustomerCreateReqBO.class)), userId);

        // TODO @puhui999：这里不用搞一个 clueCustomerIdMap 出来；可以考虑逐个创建，然后把 customerId 设置回 CrmClueDO；避免 name 匹配，极端会有问题哈；
        // TODO 是不是就直接 foreach 处理好了；因为本身量不大，for 处理性能 ok，可阅读性好
        Map<Long, Long> clueCustomerIdMap = new HashMap<>(translateClues.size());
        // 2.1 更新线索
        clueMapper.updateBatch(convertList(customers, customer -> {
            CrmClueDO firstClue = findFirst(translateClues, clue -> ObjUtil.equal(clue.getName(), customer.getName()));
            clueCustomerIdMap.put(firstClue.getId(), customer.getId());
            return new CrmClueDO().setId(firstClue.getId()).setTransformStatus(Boolean.TRUE).setCustomerId(customer.getId());
        }));
        // 2.3 复制跟进
        updateFollowUpRecords(clueCustomerIdMap);

        // 3. 记录操作日志
        for (CrmClueDO clue : translateClues) {
            // TODO @puhui999：这里优化下，translate 操作日志
            getSelf().receiveClueLog(clue);
        }
    }

    private void updateFollowUpRecords(Map<Long, Long> clueCustomerIdMap) {
        List<CrmFollowUpRecordDO> followUpRecords = followUpRecordService.getFollowUpRecordByBiz(
                CrmBizTypeEnum.CRM_LEADS.getType(), clueCustomerIdMap.keySet());
        if (CollUtil.isEmpty(followUpRecords)) {
            return;
        }

        // 创建跟进
        followUpRecordService.createFollowUpRecordBatch(convertList(followUpRecords, followUpRecord ->
                BeanUtils.toBean(followUpRecord, CrmFollowUpCreateReqBO.class).setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                        .setBizId(clueCustomerIdMap.get(followUpRecord.getBizId()))));
    }

    @LogRecord(type = CRM_LEADS_TYPE, subType = CRM_LEADS_TRANSLATE_SUB_TYPE, bizNo = "{{#clue.id}}",
            success = CRM_LEADS_TRANSLATE_SUCCESS)
    public void receiveClueLog(CrmClueDO clue) {
        // 记录操作日志上下文
        LogRecordContext.putVariable("clue", clue);
    }

    private void validateRelationDataExists(CrmClueSaveReqVO reqVO) {
        // 校验负责人
        if (Objects.nonNull(reqVO.getOwnerUserId()) &&
                Objects.isNull(adminUserApi.getUser(reqVO.getOwnerUserId()))) {
            throw exception(USER_NOT_EXISTS);
        }
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

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private CrmClueServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
