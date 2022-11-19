package cn.iocoder.yudao.module.promotion.service.seckillactivity;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.promotion.controller.admin.seckillactivity.vo.*;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckillactivity.SeckillActivityDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.promotion.convert.seckillactivity.SeckillActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.mysql.seckillactivity.SeckillActivityMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.*;

/**
 * 秒杀活动 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class SeckillActivityServiceImpl implements SeckillActivityService {

    @Resource
    private SeckillActivityMapper seckillActivityMapper;

    @Override
    public Long createSeckillActivity(SeckillActivityCreateReqVO createReqVO) {
        // 插入
        SeckillActivityDO seckillActivity = SeckillActivityConvert.INSTANCE.convert(createReqVO);
        seckillActivityMapper.insert(seckillActivity);
        // 返回
        return seckillActivity.getId();
    }

    @Override
    public void updateSeckillActivity(SeckillActivityUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateSeckillActivityExists(updateReqVO.getId());
        // 更新
        SeckillActivityDO updateObj = SeckillActivityConvert.INSTANCE.convert(updateReqVO);
        seckillActivityMapper.updateById(updateObj);
    }

    @Override
    public void deleteSeckillActivity(Long id) {
        // 校验存在
        this.validateSeckillActivityExists(id);
        // 删除
        seckillActivityMapper.deleteById(id);
    }

    private void validateSeckillActivityExists(Long id) {
        if (seckillActivityMapper.selectById(id) == null) {
            throw exception(SECKILL_ACTIVITY_NOT_EXISTS);
        }
    }

    @Override
    public SeckillActivityDO getSeckillActivity(Long id) {
        return seckillActivityMapper.selectById(id);
    }

    @Override
    public List<SeckillActivityDO> getSeckillActivityList(Collection<Long> ids) {
        return seckillActivityMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<SeckillActivityDO> getSeckillActivityPage(SeckillActivityPageReqVO pageReqVO) {
        return seckillActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<SeckillActivityDO> getSeckillActivityList(SeckillActivityExportReqVO exportReqVO) {
        return seckillActivityMapper.selectList(exportReqVO);
    }

}
