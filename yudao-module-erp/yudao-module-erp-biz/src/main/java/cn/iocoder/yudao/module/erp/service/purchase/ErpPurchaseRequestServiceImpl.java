package cn.iocoder.yudao.module.erp.service.purchase;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestItemsSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.request.ErpPurchaseRequestSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpPurchaseRequestItemsDO;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestItemsMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.purchase.ErpPurchaseRequestMapper;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import cn.iocoder.yudao.module.erp.enums.SerialConstants;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import cn.iocoder.yudao.module.erp.util.SerialGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_BATCH_INSERT_ERROR;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_INSERT_ERROR;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.formatLocalDate;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP采购申请单 Service 实现类
 *
 * @author 索迈管理员
 */
@Service
@Validated
@RequiredArgsConstructor
public class ErpPurchaseRequestServiceImpl implements ErpPurchaseRequestService {
    private final ErpPurchaseRequestMapper erpPurchaseRequestMapper;
    private final ErpPurchaseRequestItemsMapper erpPurchaseRequestItemsMapper;
    private final ErpProductService productService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseRequest(ErpPurchaseRequestSaveReqVO createReqVO) {
        //获取单据日期
        LocalDateTime date = createReqVO.getRequestTime();
        //转化为LocalDate
        LocalDate dateTime = date.toLocalDate();
        //获取当日最大时间和最小时间
        LocalDateTime minTime = LocalDateTime.of(dateTime, LocalTime.MIN);
        LocalDateTime maxTime = LocalDateTime.of(dateTime, LocalTime.MAX);
        //获取当日最大的编号数字
        Integer maxSerialNum = erpPurchaseRequestMapper.getMaxSerialNum(minTime, maxTime);
        //生成新的序列数字,并且+1
        int newSerialNum = maxSerialNum + 1;
        //如果数值大于999999，返回错误 --->> {今日单据编号已超过最大值999999}
        ThrowUtil.ifThrow(newSerialNum > 999999,PURCHASE_REQUEST_SERIAL_NUM_OVERFLOW);
        //将数字转为字符串，并且不足6位的前面自动填充为0
        String serialNumStr = String.format("%06d", newSerialNum);
        //生成单据编号
        String generateSerial = SerialGeneratorUtil.generateSerial(SerialConstants.PURCHASE_REQUEST_PREFIX, formatLocalDate(dateTime),serialNumStr);
        ErpPurchaseRequestDO purchaseRequest = BeanUtils.toBean(createReqVO, ErpPurchaseRequestDO.class);
        //为单据编号和今日最大数赋值
        purchaseRequest.setNo(generateSerial);
        purchaseRequest.setNum(newSerialNum);
        //校验产品是否存在
        List<ErpPurchaseRequestItemsDO> itemsDOList = validatePurchaseRequestItems(createReqVO.getItems());
        // 插入主表的申请单数据
        ThrowUtil.ifThrow(erpPurchaseRequestMapper.insert(purchaseRequest)<=0,DB_INSERT_ERROR);
        //获取主表主键id
        Long id = purchaseRequest.getId();
        //将父id赋予子表
        itemsDOList.forEach(i -> i.setRequestId(id));
        //批量插入子表数据
        ThrowUtil.ifThrow(!erpPurchaseRequestItemsMapper.insertBatch(itemsDOList),DB_BATCH_INSERT_ERROR);
        // 返回单据id
        return id;
    }

    private List<ErpPurchaseRequestItemsDO> validatePurchaseRequestItems(List<ErpPurchaseRequestItemsSaveReqVO> items) {
        // 1. 校验产品存在
        productService.validProductList(convertSet(items, ErpPurchaseRequestItemsSaveReqVO::getProductId));
        // 2. 转化为 ErpPurchaseRequestItemsDO 列表
        return convertList(items, o -> BeanUtils.toBean(o, ErpPurchaseRequestItemsDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePurchaseRequest(ErpPurchaseRequestSaveReqVO updateReqVO) {
        // 校验存在
        ErpPurchaseRequestDO erpPurchaseRequestDO = validatePurchaseRequestExists(updateReqVO.getId());
        //判断申请单是否应被审核
        ThrowUtil.ifThrow(erpPurchaseRequestDO.getStatus().equals(ErpAuditStatus.APPROVE.getStatus()),PURCHASE_REQUEST_UPDATE_FAIL_APPROVE,erpPurchaseRequestDO.getNo());
        //校验产品是否存在
        List<ErpPurchaseRequestItemsDO> itemsDOList = validatePurchaseRequestItems(updateReqVO.getItems());
        // 2.1 更新订单
        ErpPurchaseRequestDO updateObj = BeanUtils.toBean(updateReqVO, ErpPurchaseRequestDO.class);
        erpPurchaseRequestMapper.updateById(updateObj);
        // 2.2 更新订单项
        updatePurchaseRequestItemList(updateReqVO.getId(), itemsDOList);
    }

    private void updatePurchaseRequestItemList(Long id, List<ErpPurchaseRequestItemsDO> itemsDOList) {
        // 第一步，对比新老数据，获得添加、修改、删除的列表
        List<ErpPurchaseRequestItemsDO> oldList = erpPurchaseRequestItemsMapper.selectListByRequestId(id);
        // id 不同，就认为是不同的记录
        List<List<ErpPurchaseRequestItemsDO>> diffList = diffList(oldList, itemsDOList,
                (oldVal, newVal) -> oldVal.getId().equals(newVal.getId()));
        // 第二步，批量添加、修改、删除
        if (CollUtil.isNotEmpty(diffList.get(0))) {
            diffList.get(0).forEach(o -> o.setRequestId(id));
            erpPurchaseRequestItemsMapper.insertBatch(diffList.get(0));
        }
        if (CollUtil.isNotEmpty(diffList.get(1))) {
            erpPurchaseRequestItemsMapper.updateBatch(diffList.get(1));
        }
        if (CollUtil.isNotEmpty(diffList.get(2))) {
            erpPurchaseRequestItemsMapper.deleteBatchIds(convertList(diffList.get(2), ErpPurchaseRequestItemsDO::getId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseRequest(List<Long> ids) {
        // 1. 校验不处于已审批
        List<ErpPurchaseRequestDO> purchaseRequestDOs = erpPurchaseRequestMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(purchaseRequestDOs)) {
            return;
        }
        purchaseRequestDOs.forEach(erpPurchaseRequestDO -> {
            ThrowUtil.ifThrow(!erpPurchaseRequestDO.getStatus().equals(ErpAuditStatus.APPROVE.getStatus()),PURCHASE_REQUEST_DELETE_FAIL_APPROVE,erpPurchaseRequestDO.getNo());
        });

        // 2. 遍历删除，并记录操作日志
        purchaseRequestDOs.forEach(erpPurchaseRequest -> {
            //获取主表id
            Long id = erpPurchaseRequest.getId();
            // 2.1 删除订单
            erpPurchaseRequestMapper.deleteById(id);
            // 2.2 删除订单项
            erpPurchaseRequestItemsMapper.deleteByRequestId(id);

        });
    }

    private ErpPurchaseRequestDO validatePurchaseRequestExists(Long id) {
        ErpPurchaseRequestDO erpPurchaseRequestDO = erpPurchaseRequestMapper.selectById(id);
        if (erpPurchaseRequestDO == null) {
            throw exception(PURCHASE_REQUEST_NOT_EXISTS);
        }
        return erpPurchaseRequestDO;
    }

    @Override
    public ErpPurchaseRequestDO getPurchaseRequest(Long id) {
        return erpPurchaseRequestMapper.selectById(id);
    }

    @Override
    public PageResult<ErpPurchaseRequestDO> getPurchaseRequestPage(ErpPurchaseRequestPageReqVO pageReqVO) {
        return erpPurchaseRequestMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpPurchaseRequestItemsDO> getPurchaseRequestItemListByOrderId(Long requestId) {
        return erpPurchaseRequestItemsMapper.selectListByRequestId(requestId);
    }

}