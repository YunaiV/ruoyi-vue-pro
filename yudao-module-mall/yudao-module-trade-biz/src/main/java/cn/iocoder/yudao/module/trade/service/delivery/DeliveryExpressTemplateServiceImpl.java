package cn.iocoder.yudao.module.trade.service.delivery;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.trade.controller.admin.delivery.vo.*;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateChargeDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateFreeDO;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryExpressTemplateChargeMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryExpressTemplateFreeMapper;
import cn.iocoder.yudao.module.trade.dal.mysql.delivery.DeliveryExpressTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.trade.convert.delivery.DeliveryExpressTemplateConvert.INSTANCE;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.*;

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
    // TODO  @jason：应该不用 BatchInsertMapper 拉，直接走 expressTemplateChargeMapper.insertBatch
    @Resource
    private DeliveryExpressTemplateChargeMapper.BatchInsertMapper  expressTemplateChargeBatchMapper;
    @Resource
    private DeliveryExpressTemplateFreeMapper.BatchInsertMapper expressTemplateFreeBatchMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDeliveryExpressTemplate(DeliveryExpressTemplateCreateReqVO createReqVO) {
        // TODO @jason：中英文之间，要有空格哈。例如说， // 校验模板名是否唯一
        //校验模板名是否唯一
        validateTemplateNameUnique(createReqVO.getName(), null);

        // 插入
        DeliveryExpressTemplateDO deliveryExpressTemplate = INSTANCE.convert(createReqVO);
        expressTemplateMapper.insert(deliveryExpressTemplate);
        //插入运费模板计费表
        // TODO @jason：if (，中间要有空格
        if(CollUtil.isNotEmpty(createReqVO.getTemplateCharge())) {
            expressTemplateChargeBatchMapper.saveBatch(
                INSTANCE.convertTemplateChargeList(deliveryExpressTemplate.getId(), createReqVO.getChargeMode(), createReqVO.getTemplateCharge())
            );
        }
        //插入运费模板包邮表
        if(CollUtil.isNotEmpty(createReqVO.getTemplateFree())) {
            expressTemplateFreeBatchMapper.saveBatch(
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
        //校验模板名是否唯一
        validateTemplateNameUnique(updateReqVO.getName(), updateReqVO.getId());

        //更新运费从表
        updateExpressTemplateCharge(updateReqVO);
        //更新包邮从表
        updateExpressTemplateFree(updateReqVO);
        //更新模板主表
        DeliveryExpressTemplateDO updateObj = INSTANCE.convert(updateReqVO);
        expressTemplateMapper.updateById(updateObj);
    }

    private void updateExpressTemplateFree(DeliveryExpressTemplateUpdateReqVO updateReqVO) {
        List<DeliveryExpressTemplateFreeDO> oldFreeList = expressTemplateFreeMapper.selectListByTemplateId(updateReqVO.getId());
        List<ExpressTemplateFreeUpdateVO> newFreeList = updateReqVO.getTemplateFree();
        //新增包邮区域列表
        List<DeliveryExpressTemplateFreeDO> addFreeList = new ArrayList<>(newFreeList.size());
        //更新包邮区域列表
        List<DeliveryExpressTemplateFreeDO> updateFreeList = new ArrayList<>(newFreeList.size());
        for (ExpressTemplateFreeUpdateVO item : newFreeList) {
            if (Objects.nonNull(item.getId())) {
                updateFreeList.add(INSTANCE.convertTemplateFree(item));
            }else{
                item.setTemplateId(updateReqVO.getId());
                addFreeList.add(INSTANCE.convertTemplateFree(item));
            }
        }
        //删除的包邮区域id
        Set<Long> deleteFreeIds = CollectionUtils.convertSet(oldFreeList, DeliveryExpressTemplateFreeDO::getId);
        deleteFreeIds.removeAll(CollectionUtils.convertSet(updateFreeList, DeliveryExpressTemplateFreeDO::getId));
        //新增
        if (CollUtil.isNotEmpty(addFreeList)) {
            expressTemplateFreeBatchMapper.saveBatch(addFreeList);
        }
        //修改
        if (CollUtil.isNotEmpty(updateFreeList)) {
            expressTemplateFreeBatchMapper.saveOrUpdateBatch(updateFreeList);
        }
        //删除
        if (CollUtil.isNotEmpty(deleteFreeIds)) {
            expressTemplateFreeMapper.deleteBatchIds(deleteFreeIds);
        }
    }

    private void updateExpressTemplateCharge(DeliveryExpressTemplateUpdateReqVO updateReqVO) {
        List<DeliveryExpressTemplateChargeDO> oldChargeList = expressTemplateChargeMapper.selectListByTemplateId(updateReqVO.getId());
        List<ExpressTemplateChargeUpdateVO> newChargeList = updateReqVO.getTemplateCharge();
        //新增运费区域列表
        List<DeliveryExpressTemplateChargeDO> addList = new ArrayList<>(newChargeList.size());
        //更新运费区域列表
        List<DeliveryExpressTemplateChargeDO> updateList = new ArrayList<>(newChargeList.size());
        for (ExpressTemplateChargeUpdateVO item : newChargeList) {
            if (Objects.nonNull(item.getId())) { // TODO @jason：null 的判断，还是用 item.getId() != null 好一点。一般数组用方法，主要考虑 null + length = 0；
                //计费模式以主表为准
                item.setChargeMode(updateReqVO.getChargeMode());
                updateList.add(INSTANCE.convertTemplateCharge(item));
            }else{
                item.setTemplateId(updateReqVO.getId());
                item.setChargeMode(updateReqVO.getChargeMode());
                addList.add(INSTANCE.convertTemplateCharge(item));
            }
        }
        //删除的运费区域id TODO @jason：这块放到删除部分的那块逻辑会好点（149  - 152 行)，主要变量要贴相应的逻辑近一点哈。
        Set<Long> deleteChargeIds = CollectionUtils.convertSet(oldChargeList, DeliveryExpressTemplateChargeDO::getId);
        deleteChargeIds.removeAll(CollectionUtils.convertSet(updateList, DeliveryExpressTemplateChargeDO::getId));
        //新增
        if (CollUtil.isNotEmpty(addList)) {
            expressTemplateChargeBatchMapper.saveBatch(addList);
        }
        //修改
        if (CollUtil.isNotEmpty(updateList)) {
            expressTemplateChargeBatchMapper.saveOrUpdateBatch(updateList);
        }
        //删除
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
     * 校验运费模板名是否唯一 // TODO @jason：方法注释，和参数，要空一行。
     * @param name 模板名称
     * @param id 运费模板编号, 可以为null // TODO @jason：中英文之间，要空一行；其它地方也看看哈
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
    public DeliveryExpressTemplateRespVO getDeliveryExpressTemplate(Long id) {
        List<DeliveryExpressTemplateChargeDO> chargeList = expressTemplateChargeMapper.selectListByTemplateId(id);
        List<DeliveryExpressTemplateFreeDO> freeList = expressTemplateFreeMapper.selectListByTemplateId(id);
        DeliveryExpressTemplateDO template = expressTemplateMapper.selectById(id);
        return INSTANCE.convert(template, chargeList,freeList);
    }

    @Override
    public List<DeliveryExpressTemplateDO> getDeliveryExpressTemplateList(Collection<Long> ids) {
        return expressTemplateMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<DeliveryExpressTemplateDO> getDeliveryExpressTemplatePage(DeliveryExpressTemplatePageReqVO pageReqVO) {
        return expressTemplateMapper.selectPage(pageReqVO);
    }

    @Override
    public List<DeliveryExpressTemplateDO> getDeliveryExpressTemplateList(DeliveryExpressTemplateExportReqVO exportReqVO) {
        return expressTemplateMapper.selectList(exportReqVO);
    }

}
