package cn.iocoder.yudao.module.promotion.service.combination;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.CombinationActivityCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.CombinationActivityExportReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.CombinationActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.combination.vo.CombinationActivityUpdateReqVO;
import cn.iocoder.yudao.module.promotion.convert.combination.CombinationActivityConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.combination.CombinationActivityDO;
import cn.iocoder.yudao.module.promotion.dal.mysql.combination.CombinationActivityMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.promotion.enums.ErrorCodeConstants.COMBINATION_ACTIVITY_NOT_EXISTS;

/**
 * 拼团活动 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class CombinationActivityServiceImpl implements CombinationActivityService {

    @Resource
    private CombinationActivityMapper combinationActivityMapper;

    @Override
    public Long createCombinationActivity(CombinationActivityCreateReqVO createReqVO) {
        // 插入
        CombinationActivityDO combinationActivity = CombinationActivityConvert.INSTANCE.convert(createReqVO);
        combinationActivityMapper.insert(combinationActivity);
        // 返回
        return combinationActivity.getId();
    }

    @Override
    public void updateCombinationActivity(CombinationActivityUpdateReqVO updateReqVO) {
        // 校验存在
        validateCombinationActivityExists(updateReqVO.getId());
        // 更新
        CombinationActivityDO updateObj = CombinationActivityConvert.INSTANCE.convert(updateReqVO);
        combinationActivityMapper.updateById(updateObj);
    }

    @Override
    public void deleteCombinationActivity(Long id) {
        // 校验存在
        validateCombinationActivityExists(id);
        // 删除
        combinationActivityMapper.deleteById(id);
    }

    private void validateCombinationActivityExists(Long id) {
        if (combinationActivityMapper.selectById(id) == null) {
            throw exception(COMBINATION_ACTIVITY_NOT_EXISTS);
        }
    }

    @Override
    public CombinationActivityDO getCombinationActivity(Long id) {
        return combinationActivityMapper.selectById(id);
    }

    @Override
    public List<CombinationActivityDO> getCombinationActivityList(Collection<Long> ids) {
        return combinationActivityMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CombinationActivityDO> getCombinationActivityPage(CombinationActivityPageReqVO pageReqVO) {
        return combinationActivityMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CombinationActivityDO> getCombinationActivityList(CombinationActivityExportReqVO exportReqVO) {
        return combinationActivityMapper.selectList(exportReqVO);
    }

}
