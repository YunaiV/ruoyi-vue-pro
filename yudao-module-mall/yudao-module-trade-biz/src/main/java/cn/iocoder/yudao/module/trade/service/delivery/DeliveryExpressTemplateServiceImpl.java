package cn.iocoder.yudao.module.trade.service.delivery;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate.*;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateChargeDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateFreeDO;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryExpressTemplateChargeMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryExpressTemplateFreeMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryExpressTemplateMapper;
import cn.iocoder.yudao.module.trade.service.delivery.bo.DeliveryExpressTemplateRespBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.trade.convert.delivery.DeliveryExpressTemplateConvert.INSTANCE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.EXPRESS_TEMPLATE_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.EXPRESS_TEMPLATE_NOT_EXISTS;

/**
 * 快递运费模板 Service 实现类
 *
 * @author jason
 */
@Service
@Validated
public class DeliveryExpressTemplateServiceImpl implements DeliveryExpressTemplateService {

    @Resource
    private DeliveryExpressTemplateMapper expressTemplateMapper;
    @Resource
    private DeliveryExpressTemplateChargeMapper expressTemplateChargeMapper;
    @Resource
    private DeliveryExpressTemplateFreeMapper expressTemplateFreeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDeliveryExpressTemplate(DeliveryExpressTemplateCreateReqVO createReqVO) {
        // 校验模板名是否唯一
        validateTemplateNameUnique(createReqVO.getName(), null);

        // 插入
        DeliveryExpressTemplateDO template = INSTANCE.convert(createReqVO);
        expressTemplateMapper.insert(template);
        // 插入运费模板计费表
        if (CollUtil.isNotEmpty(createReqVO.getCharges())) {
            expressTemplateChargeMapper.insertBatch(
                    INSTANCE.convertTemplateChargeList(template.getId(), createReqVO.getChargeMode(), createReqVO.getCharges())
            );
        }
        // 插入运费模板包邮表
        if (CollUtil.isNotEmpty(createReqVO.getFrees())) {
            expressTemplateFreeMapper.insertBatch(
                    INSTANCE.convertTemplateFreeList(template.getId(), createReqVO.getFrees())
            );
        }
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryExpressTemplate(DeliveryExpressTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        validateDeliveryExpressTemplateExists(updateReqVO.getId());
        // 校验模板名是否唯一
        validateTemplateNameUnique(updateReqVO.getName(), updateReqVO.getId());

        // 更新运费从表
        updateExpressTemplateCharge(updateReqVO.getId(), updateReqVO.getChargeMode(), updateReqVO.getCharges());
        // 更新包邮从表
        updateExpressTemplateFree(updateReqVO.getId(), updateReqVO.getFrees());
        // 更新模板主表
        DeliveryExpressTemplateDO updateObj = INSTANCE.convert(updateReqVO);
        expressTemplateMapper.updateById(updateObj);
    }

    private void updateExpressTemplateFree(Long templateId, List<DeliveryExpressTemplateFreeBaseVO> frees) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<DeliveryExpressTemplateFreeDO> oldList = expressTemplateFreeMapper.selectListByTemplateId(templateId);
        List<DeliveryExpressTemplateFreeDO> newList = INSTANCE.convertTemplateFreeList(templateId, frees);
        List<List<DeliveryExpressTemplateFreeDO>> diffList = CollectionUtils.diffList(oldList, newList,
                (oldVal, newVal) -> ObjectUtil.equal(oldVal.getId(), newVal.getId()));

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            expressTemplateFreeMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            expressTemplateFreeMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            expressTemplateFreeMapper.deleteBatchIds(convertList(diffList.get(2), DeliveryExpressTemplateFreeDO::getId));
        }
    }

    private void updateExpressTemplateCharge(Long templateId, Integer chargeMode, List<DeliveryExpressTemplateChargeBaseVO> charges) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<DeliveryExpressTemplateChargeDO> oldList = expressTemplateChargeMapper.selectListByTemplateId(templateId);
        List<DeliveryExpressTemplateChargeDO> newList = INSTANCE.convertTemplateChargeList(templateId, chargeMode, charges);
        List<List<DeliveryExpressTemplateChargeDO>> diffList = diffList(oldList, newList, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getId(), newVal.getId());
            if (same) {
                newVal.setChargeMode(chargeMode); // 更新下收费模式
            }
            return same;
        });

        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            expressTemplateChargeMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            expressTemplateChargeMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            expressTemplateChargeMapper.deleteBatchIds(convertList(diffList.get(2), DeliveryExpressTemplateChargeDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeliveryExpressTemplate(Long id) {
        // 校验存在
        validateDeliveryExpressTemplateExists(id);

        // 删除主表
        expressTemplateMapper.deleteById(id);
        // 删除运费从表
        expressTemplateChargeMapper.deleteByTemplateId(id);
        // 删除包邮从表
        expressTemplateFreeMapper.deleteByTemplateId(id);
    }

    /**
     * 校验运费模板名是否唯一
     *
     * @param name 模板名称
     * @param id   运费模板编号,可以为 null
     */
    private void validateTemplateNameUnique(String name, Long id) {
        DeliveryExpressTemplateDO template = expressTemplateMapper.selectByName(name);
        if (template == null) {
            return;
        }
        // 如果 id 为空
        if (id == null) {
            throw exception(EXPRESS_TEMPLATE_NAME_DUPLICATE);
        }
        if (!template.getId().equals(id)) {
            throw exception(EXPRESS_TEMPLATE_NAME_DUPLICATE);
        }
    }

    private void validateDeliveryExpressTemplateExists(Long id) {
        if (expressTemplateMapper.selectById(id) == null) {
            throw exception(EXPRESS_TEMPLATE_NOT_EXISTS);
        }
    }

    @Override
    public DeliveryExpressTemplateDetailRespVO getDeliveryExpressTemplate(Long id) {
        List<DeliveryExpressTemplateChargeDO> chargeList = expressTemplateChargeMapper.selectListByTemplateId(id);
        List<DeliveryExpressTemplateFreeDO> freeList = expressTemplateFreeMapper.selectListByTemplateId(id);
        DeliveryExpressTemplateDO template = expressTemplateMapper.selectById(id);
        return INSTANCE.convert(template, chargeList, freeList);
    }

    @Override
    public List<DeliveryExpressTemplateDO> getDeliveryExpressTemplateList(Collection<Long> ids) {
        return expressTemplateMapper.selectBatchIds(ids);
    }

    @Override
    public List<DeliveryExpressTemplateDO> getDeliveryExpressTemplateList() {
        return expressTemplateMapper.selectList();
    }

    @Override
    public PageResult<DeliveryExpressTemplateDO> getDeliveryExpressTemplatePage(DeliveryExpressTemplatePageReqVO pageReqVO) {
        return expressTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public DeliveryExpressTemplateDO validateDeliveryExpressTemplate(Long templateId) {
        DeliveryExpressTemplateDO template = expressTemplateMapper.selectById(templateId);
        if (template == null) {
            throw exception(EXPRESS_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    @Override
    public Map<Long, DeliveryExpressTemplateRespBO> getExpressTemplateMapByIdsAndArea(Collection<Long> ids, Integer areaId) {
        Assert.notNull(areaId, "区域编号 {} 不能为空", areaId);
        // 查询 template 数组
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<DeliveryExpressTemplateDO> templateList = expressTemplateMapper.selectBatchIds(ids);
        // 查询 templateCharge 数组
        List<DeliveryExpressTemplateChargeDO> chargeList = expressTemplateChargeMapper.selectByTemplateIds(ids);
        // 查询 templateFree 数组
        List<DeliveryExpressTemplateFreeDO> freeList = expressTemplateFreeMapper.selectListByTemplateIds(ids);

        // 组合运费模板配置 RespBO
        return INSTANCE.convertMap(areaId, templateList, chargeList, freeList);
    }

}
