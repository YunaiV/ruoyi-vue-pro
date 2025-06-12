package cn.iocoder.yudao.module.srm.service.purchase.impl;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.payment.term.vo.SrmPaymentTermRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier.SrmSupplierPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier.SrmSupplierRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.supplier.SrmSupplierSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.payment.term.SrmPaymentTermDO;
import cn.iocoder.yudao.module.srm.dal.mysql.purchase.SrmSupplierMapper;
import cn.iocoder.yudao.module.srm.enums.SrmChannelEnum;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import cn.iocoder.yudao.module.srm.service.purchase.payment.term.SrmPaymentTermService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.srm.enums.SrmErrorCodeConstants.*;
import static cn.iocoder.yudao.module.srm.tool.TmsStreamXTool.assemble;
import static java.util.Collections.emptyList;

/**
 * ERP 供应商 Service 实现类
 *
 * @author wdy
 */
@Service
@Validated
public class SrmSupplierServiceImpl implements SrmSupplierService {

    @Resource
    private SrmSupplierMapper supplierMapper;

    @Resource(name = SrmChannelEnum.SUPPLIER)
    private MessageChannel supplierChannel;
    @Autowired
    private SrmPaymentTermService srmPaymentTermService;

    @Override
    public Long createSupplier(SrmSupplierSaveReqVO createReqVO) {
        SrmSupplierDO supplier = BeanUtils.toBean(createReqVO, SrmSupplierDO.class);
        // 校验供应商名称是否重复
        validateSupplierNameDuplicate(supplier.getName(), null);
        supplierMapper.insert(supplier);
        // 发送消息到通道
        supplierChannel.send(MessageBuilder.withPayload(Collections.singletonList(supplier.getId())).build());
        return supplier.getId();
    }

    @Override
    public void updateSupplier(SrmSupplierSaveReqVO updateReqVO) {
        // 校验存在
        validateSupplierExists(updateReqVO.getId());
        // 校验供应商名称是否重复
        validateSupplierNameDuplicate(updateReqVO.getName(), updateReqVO.getId());
        // 更新
        SrmSupplierDO updateObj = BeanUtils.toBean(updateReqVO, SrmSupplierDO.class);
        supplierMapper.updateById(updateObj);
        // 发送消息到通道
        supplierChannel.send(MessageBuilder.withPayload(Collections.singletonList(updateObj.getId())).build());
    }

    @Override
    public void deleteSupplier(Long id) {
        // 校验存在
        validateSupplierExists(id);
        // 删除
        supplierMapper.deleteById(id);
    }

    private void validateSupplierExists(Long id) {
        if(supplierMapper.selectById(id) == null) {
            throw exception(SUPPLIER_NOT_EXISTS);
        }
    }

    @Override
    public SrmSupplierDO getSupplier(Long id) {
        return supplierMapper.selectById(id);
    }

    @Override
    public SrmSupplierDO validateSupplier(Long id) {
        SrmSupplierDO supplier = supplierMapper.selectById(id);
        if(supplier == null) {
            throw exception(SUPPLIER_NOT_EXISTS);
        }
        if (CommonStatusEnum.isDisable(supplier.getOpenStatus())) {
            throw exception(SUPPLIER_NOT_ENABLE, supplier.getName());
        }
        return supplier;
    }

    @Override
    public List<SrmSupplierDO> getSupplierList(Collection<Long> ids) {
        //ids是空集合
        if(CollectionUtils.isEmpty(ids)) {
            return emptyList();
        }
        return supplierMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SrmSupplierDO> getSupplierPage(SrmSupplierPageReqVO pageReqVO) {
        return supplierMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SrmSupplierDO> getSupplierListByStatus(Integer status) {
        return supplierMapper.selectListByStatus(status);
    }

    /**
     * 校验供应商名称是否重复
     *
     * @param name 供应商名称
     * @param id   供应商编号
     */
    private void validateSupplierNameDuplicate(String name, Long id) {
        SrmSupplierDO supplier = supplierMapper.selectByName(name);
        if (supplier != null && !supplier.getId().equals(id)) {
            throw exception(SUPPLIER_NAME_DUPLICATE, name);
        }
    }

    @Override
    public void assemblePaymentTerms(List<SrmSupplierRespVO> supplierList) {
        assemble(
            supplierList,
            SrmSupplierRespVO::getPaymentTermsId,
            ids -> srmPaymentTermService.getPaymentTermList(ids.stream().distinct().toList()).stream().collect(Collectors.toMap(SrmPaymentTermDO::getId, Function.identity())),
            SrmSupplierRespVO::setSrmPaymentTermsResp,
            p -> BeanUtils.toBean(p, SrmPaymentTermRespVO.class)
        );
        //额外
        supplierList.forEach(e -> Optional.ofNullable(e.getSrmPaymentTermsResp()).ifPresent(p -> e.setPaymentTerms(e.getSrmPaymentTermsResp().getPaymentTermZh())));
    }
}