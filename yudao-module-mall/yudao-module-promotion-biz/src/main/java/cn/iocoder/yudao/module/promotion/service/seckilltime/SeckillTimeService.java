package cn.iocoder.yudao.module.promotion.service.seckilltime;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.promotion.controller.admin.seckilltime.vo.*;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckilltime.SeckillTimeDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 秒杀时段 Service 接口
 *
 * @author 芋道源码
 */
public interface SeckillTimeService {

    /**
     * 创建秒杀时段
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSeckillTime(@Valid SeckillTimeCreateReqVO createReqVO);

    /**
     * 更新秒杀时段
     *
     * @param updateReqVO 更新信息
     */
    void updateSeckillTime(@Valid SeckillTimeUpdateReqVO updateReqVO);

    /**
     * 删除秒杀时段
     *
     * @param id 编号
     */
    void deleteSeckillTime(Long id);

    /**
     * 获得秒杀时段
     *
     * @param id 编号
     * @return 秒杀时段
     */
    SeckillTimeDO getSeckillTime(Long id);

    /**
     * 获得所有秒杀时段列表
     *
     * @return 所有秒杀时段列表
     */
    List<SeckillTimeDO> getSeckillTimeList();

//    /**
//     * 获得秒杀时段分页
//     *
//     * @param pageReqVO 分页查询
//     * @return 秒杀时段分页
//     */
//    PageResult<SeckillTimeDO> getSeckillTimePage(SeckillTimePageReqVO pageReqVO);

    /**
     * 获得秒杀时段列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 秒杀时段列表
     */
    List<SeckillTimeDO> getSeckillTimeList(SeckillTimeExportReqVO exportReqVO);

}
