package cn.iocoder.yudao.module.promotion.service.seckilltime;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.promotion.controller.admin.seckilltime.vo.*;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckilltime.SeckillTimeDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.promotion.convert.seckilltime.SeckillTimeConvert;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckilltime.SeckillTimeMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;

/**
 * 秒杀时段 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SeckillTimeServiceImpl implements SeckillTimeService {

    @Resource
    private SeckillTimeMapper seckillTimeMapper;

    @Override
    public Long createSeckillTime(SeckillTimeCreateReqVO createReqVO) {
        // 插入
        SeckillTimeDO seckillTime = SeckillTimeConvert.INSTANCE.convert(createReqVO);
        seckillTimeMapper.insert(seckillTime);
        // 返回
        return seckillTime.getId();
    }

    @Override
    public void updateSeckillTime(SeckillTimeUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSeckillTimeExists(updateReqVO.getId());
        // 更新
        SeckillTimeDO updateObj = SeckillTimeConvert.INSTANCE.convert(updateReqVO);
        seckillTimeMapper.updateById(updateObj);
    }

    @Override
    public void deleteSeckillTime(Long id) {
        // 校验存在
        this.validateSeckillTimeExists(id);
        // 删除
        seckillTimeMapper.deleteById(id);
    }

    private void validateSeckillTimeExists(Long id) {
        if (seckillTimeMapper.selectById(id) == null) {
            throw exception(SECKILL_TIME_NOT_EXISTS);
        }
    }

    @Override
    public SeckillTimeDO getSeckillTime(Long id) {
        return seckillTimeMapper.selectById(id);
    }

    @Override
    public List<SeckillTimeDO> getSeckillTimeList() {
        return seckillTimeMapper.selectList();
    }

//    @Override
//    public PageResult<SeckillTimeDO> getSeckillTimePage(SeckillTimePageReqVO pageReqVO) {
//        return seckillTimeMapper.selectPage(pageReqVO);
//    }

    @Override
    public List<SeckillTimeDO> getSeckillTimeList(SeckillTimeExportReqVO exportReqVO) {
        return seckillTimeMapper.selectList(exportReqVO);
    }

}
