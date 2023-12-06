package cn.iocoder.yudao.module.crm.service.concerned;

import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.service.concerned.bo.CrmConcernedCreateReqBO;

import javax.validation.Valid;
import java.util.Collection;

/**
 * CRM 关注的数据 Service 接口
 *
 * @author HUIHUI
 */
public interface CrmConcernedService {

    /**
     * 创建关注数据
     *
     * @param createReqBO 创建请求
     * @return 编号
     */
    Long createConcerned(@Valid CrmConcernedCreateReqBO createReqBO);

    /**
     * 批量创建关注数据
     *
     * @param createReqBO 创建请求
     */
    void createConcernedBatch(@Valid Collection<CrmConcernedCreateReqBO> createReqBO);

    /**
     * 删除关注数据
     *
     * @param bizType 数据类型，关联 {@link CrmBizTypeEnum}
     * @param bizIds  数据编号，关联 {@link CrmBizTypeEnum} 对应模块 DO#getId()
     * @param userId  用户编号
     */
    void deleteConcerned(Integer bizType, Collection<Long> bizIds, Long userId);

}
