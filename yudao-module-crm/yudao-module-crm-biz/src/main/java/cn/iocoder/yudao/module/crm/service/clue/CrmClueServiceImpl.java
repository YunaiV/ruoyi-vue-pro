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

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CLUE_NOT_EXISTS;
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
        List<CrmClueDO> clues = getClueList(reqVO.getIds(), userId);
        if (CollUtil.isEmpty(clues)) {
            throw exception(CLUE_NOT_EXISTS);
        }
        // TODO @min：如果已经转化，则不能重复转化

        // 遍历线索(过滤掉已转化的线索)，创建对应的客户
        clues.stream().filter(clue -> ObjectUtil.notEqual(Boolean.TRUE, clue.getTransformStatus()))
                .forEach(clue -> {
                    // 1.创建客户
                    CrmCustomerSaveReqVO customerSaveReqVO = BeanUtils.toBean(clue, CrmCustomerSaveReqVO.class)
                            .setId(null);
                    Long customerId = customerService.createCustomer(customerSaveReqVO, userId);
                    // TODO @puhui999：如果有跟进记录，需要一起转过去；
                    // 2.更新线索，新建一个 CrmClueDO 去更新。尽量规避直接用原本的对象去更新。因为这样万一并发更新，会存在覆盖的问题。
                    clueMapper.updateById(BeanUtils.toBean(clue, CrmClueDO.class)
                            // 线索状态设置为已转化
                            .setTransformStatus(Boolean.TRUE)
                            // 设置关联的客户编号
                            .setCustomerId(customerId));
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
