package cn.iocoder.yudao.module.trade.service.delivery;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate.DeliveryExpressTemplateCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate.DeliveryExpressTemplateDetailRespVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate.DeliveryExpressTemplatePageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.expresstemplate.DeliveryExpressTemplateUpdateReqVO;
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

import javax.annotation.Resource;
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
        DeliveryExpressTemplateDO deliveryExpressTemplate = INSTANCE.convert(createReqVO);
        expressTemplateMapper.insert(deliveryExpressTemplate);
        // 插入运费模板计费表
        if (CollUtil.isNotEmpty(createReqVO.getTemplateCharge())) {
            expressTemplateChargeMapper.insertBatch(
                    INSTANCE.convertTemplateChargeList(deliveryExpressTemplate.getId(), createReqVO.getChargeMode(), createReqVO.getTemplateCharge())
            );
        }
        // 插入运费模板包邮表
        if (CollUtil.isNotEmpty(createReqVO.getTemplateFree())) {
            expressTemplateFreeMapper.insertBatch(
                    INSTANCE.convertTemplateFreeList(deliveryExpressTemplate.getId(), createReqVO.getTemplateFree())
            );
        }
        return deliveryExpressTemplate.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeliveryExpressTemplate(DeliveryExpressTemplateUpdateReqVO updateReqVO) {
        // 校验存在
        validateDeliveryExpressTemplateExists(updateReqVO.getId());
        // 校验模板名是否唯一
        validateTemplateNameUnique(updateReqVO.getName(), updateReqVO.getId());

        // 更新运费从表
        updateExpressTemplateCharge(updateReqVO);
        // 更新包邮从表
        updateExpressTemplateFree(updateReqVO);
        // 更新模板主表
        DeliveryExpressTemplateDO updateObj = INSTANCE.convert(updateReqVO);
        expressTemplateMapper.updateById(updateObj);
    }

    private void updateExpressTemplateFree(DeliveryExpressTemplateUpdateReqVO updateReqVO) {
        // 1.1 获得新增/修改的区域列表
        List<DeliveryExpressTemplateFreeDO> oldFreeList = expressTemplateFreeMapper.selectListByTemplateId(updateReqVO.getId());
        List<DeliveryExpressTemplateUpdateReqVO.ExpressTemplateFreeUpdateVO> newFreeList = updateReqVO.getTemplateFree();
        List<DeliveryExpressTemplateFreeDO> addFreeList = new ArrayList<>(newFreeList.size()); // 新增包邮区域列表
        List<DeliveryExpressTemplateFreeDO> updateFreeList = new ArrayList<>(newFreeList.size()); // 更新包邮区域列表
        for (DeliveryExpressTemplateUpdateReqVO.ExpressTemplateFreeUpdateVO item : newFreeList) {
            if (Objects.nonNull(item.getId())) {
                updateFreeList.add(INSTANCE.convertTemplateFree(item));
            } else {
                item.setTemplateId(updateReqVO.getId());
                addFreeList.add(INSTANCE.convertTemplateFree(item));
            }
        }
        // 1.2 新增
        if (CollUtil.isNotEmpty(addFreeList)) {
            expressTemplateFreeMapper.insertBatch(addFreeList);
        }
        // 1.3 修改
        if (CollUtil.isNotEmpty(updateFreeList)) {
            expressTemplateFreeMapper.updateBatch(updateFreeList);
        }

        // 2. 删除
        Set<Long> deleteFreeIds = convertSet(oldFreeList, DeliveryExpressTemplateFreeDO::getId);
        deleteFreeIds.removeAll(convertSet(updateFreeList, DeliveryExpressTemplateFreeDO::getId));
        if (CollUtil.isNotEmpty(deleteFreeIds)) {
            expressTemplateFreeMapper.deleteBatchIds(deleteFreeIds);
        }
    }

    private void updateExpressTemplateCharge(DeliveryExpressTemplateUpdateReqVO updateReqVO) {
        // 1.1 获得新增/修改的区域列表
        List<DeliveryExpressTemplateChargeDO> oldChargeList = expressTemplateChargeMapper.selectListByTemplateId(updateReqVO.getId());
        List<DeliveryExpressTemplateUpdateReqVO.ExpressTemplateChargeUpdateVO> newChargeList = updateReqVO.getTemplateCharge();
        List<DeliveryExpressTemplateChargeDO> addList = new ArrayList<>(newChargeList.size()); // 新增运费区域列表
        List<DeliveryExpressTemplateChargeDO> updateList = new ArrayList<>(newChargeList.size()); // 更新运费区域列表
        for (DeliveryExpressTemplateUpdateReqVO.ExpressTemplateChargeUpdateVO item : newChargeList) {
            if (item.getId() != null) {
                // 计费模式以主表为准
                item.setChargeMode(updateReqVO.getChargeMode());
                updateList.add(INSTANCE.convertTemplateCharge(item));
            } else {
                item.setTemplateId(updateReqVO.getId());
                item.setChargeMode(updateReqVO.getChargeMode());
                addList.add(INSTANCE.convertTemplateCharge(item));
            }
        }
        // 1.2 新增
        if (CollUtil.isNotEmpty(addList)) {
            expressTemplateChargeMapper.insertBatch(addList);
        }
        // 1.3 修改
        if (CollUtil.isNotEmpty(updateList)) {
            expressTemplateChargeMapper.updateBatch(updateList);
        }

        // 2. 删除
        Set<Long> deleteChargeIds = convertSet(oldChargeList, DeliveryExpressTemplateChargeDO::getId);
        deleteChargeIds.removeAll(convertSet(updateList, DeliveryExpressTemplateChargeDO::getId));
        if (CollUtil.isNotEmpty(deleteChargeIds)) {
            expressTemplateChargeMapper.deleteBatchIds(deleteChargeIds);
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
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<DeliveryExpressTemplateDO> templateList = expressTemplateMapper.selectBatchIds(ids);
        // 查询 templateCharge
        List<DeliveryExpressTemplateChargeDO> templeChargeList = expressTemplateChargeMapper.selectByTemplateIds(ids);
        Map<Long, List<DeliveryExpressTemplateChargeDO>> templateChargeMap = convertMultiMap(templeChargeList,
                DeliveryExpressTemplateChargeDO::getTemplateId);
        // 查询 templateFree
        List<DeliveryExpressTemplateFreeDO> templateFreeList = expressTemplateFreeMapper.selectListByTemplateIds(ids);
        Map<Long, List<DeliveryExpressTemplateFreeDO>> templateFreeMap = convertMultiMap(templateFreeList,
                DeliveryExpressTemplateFreeDO::getTemplateId);
        // 组合运费模板配置 RespBO
        Map<Long, DeliveryExpressTemplateRespBO> result = new HashMap<>(templateList.size());
        templateList.forEach(item -> {
            DeliveryExpressTemplateRespBO bo = new DeliveryExpressTemplateRespBO()
                    .setChargeMode(item.getChargeMode())
                    .setTemplateCharge(findMatchExpressTemplateCharge(templateChargeMap.get(item.getId()), areaId))
                    .setTemplateFree(findMatchExpressTemplateFree(templateFreeMap.get(item.getId()), areaId));
            result.put(item.getId(), bo);
        });
        return result;
    }

    private DeliveryExpressTemplateRespBO.DeliveryExpressTemplateChargeBO findMatchExpressTemplateCharge(
            List<DeliveryExpressTemplateChargeDO> templateChargeList, Integer areaId) {
        return INSTANCE.convertTemplateCharge(findFirst(templateChargeList, item -> item.getAreaIds().contains(areaId)));
    }

    private DeliveryExpressTemplateRespBO.DeliveryExpressTemplateFreeBO findMatchExpressTemplateFree(
            List<DeliveryExpressTemplateFreeDO> templateFreeList, Integer areaId) {
        return INSTANCE.convertTemplateFree(findFirst(templateFreeList, item -> item.getAreaIds().contains(areaId)));
    }

}
