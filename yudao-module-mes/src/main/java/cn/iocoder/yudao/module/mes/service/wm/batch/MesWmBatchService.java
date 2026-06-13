package cn.iocoder.yudao.module.mes.service.wm.batch;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.batch.vo.MesWmBatchGenerateReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.batch.vo.MesWmBatchPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;

import java.util.List;

/**
 * 批次管理 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmBatchService {

    /**
     * 获得批次分页
     *
     * @param pageReqVO 分页查询
     * @return 批次分页
     */
    PageResult<MesWmBatchDO> getBatchPage(MesWmBatchPageReqVO pageReqVO);

    /**
     * 获得批次
     *
     * @param id 批次编号
     * @return 批次记录
     */
    MesWmBatchDO getBatch(Long id);

    /**
     * 根据批次号获得批次
     *
     * @param code 批次号
     * @return 批次记录
     */
    MesWmBatchDO getBatchByCode(String code);

    /**
     * 获取或生成批次编码
     * <p>
     * 根据物料批次配置，查询或生成批次记录
     *
     * @param reqVO 批次参数（包含 itemId 及其他可选属性）
     * @return 批次记录（如果物料未启用批次管理则返回 null）
     */
    MesWmBatchDO getOrGenerateBatchCode(MesWmBatchGenerateReqVO reqVO);

    /**
     * 批次向前追溯（递归查询）
     * <p>
     * 查询当前批次被哪些工单的哪些批次产品消耗
     *
     * @param code 批次编码
     * @return 批次列表
     */
    List<MesWmBatchDO> getForwardBatchList(String code);

    /**
     * 批次向后追溯（递归查询）
     * <p>
     * 查询当前批次的产品使用了哪些批次的物资
     *
     * @param code 批次编码
     * @return 批次列表
     */
    List<MesWmBatchDO> getBackwardBatchList(String code);

    /**
     * 校验批次存在，并校验批次与物料的归属关系
     *
     * @param batchId 批次ID
     * @param itemId  物料ID
     * @return 批次记录
     */
    MesWmBatchDO validateBatchExists(Long batchId, Long itemId);

    /**
     * 校验批次存在，并校验批次与物料、客户/供应商的归属关系
     *
     * @param batchId  批次ID
     * @param itemId   物料ID
     * @param clientId 客户ID（可选，不为空时校验）
     * @param vendorId 供应商ID（可选，不为空时校验）
     * @return 批次记录
     */
    MesWmBatchDO validateBatchExists(Long batchId, Long itemId, Long clientId, Long vendorId);

    /**
     * 获取指定工具的批次数量
     *
     * @param toolId 工具编号
     * @return 批次数量
     */
    Long getBatchCountByToolId(Long toolId);

}
