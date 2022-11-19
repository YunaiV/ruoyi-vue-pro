package cn.iocoder.yudao.module.promotion.service.seckillactivity;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo.*;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 秒杀活动 Service 接口
 *
 * @author 芋道源码
 */
public interface SeckillActivityService {

    /**
     * 创建秒杀活动
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSeckillActivity(@Valid SeckillActivityCreateReqVO createReqVO);

    /**
     * 更新秒杀活动
     *
     * @param updateReqVO 更新信息
     */
    void updateSeckillActivity(@Valid SeckillActivityUpdateReqVO updateReqVO);

    /**
     * 删除秒杀活动
     *
     * @param id 编号
     */
    void deleteSeckillActivity(Long id);

    /**
     * 获得秒杀活动
     *
     * @param id 编号
     * @return 秒杀活动
     */
    SeckillActivityDO getSeckillActivity(Long id);

    /**
     * 获得秒杀活动列表
     *
     * @param ids 编号
     * @return 秒杀活动列表
     */
    List<SeckillActivityDO> getSeckillActivityList(Collection<Long> ids);

    /**
     * 获得秒杀活动分页
     *
     * @param pageReqVO 分页查询
     * @return 秒杀活动分页
     */
    PageResult<SeckillActivityDO> getSeckillActivityPage(SeckillActivityPageReqVO pageReqVO);

    /**
     * 获得秒杀活动列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 秒杀活动列表
     */
    List<SeckillActivityDO> getSeckillActivityList(SeckillActivityExportReqVO exportReqVO);

}
