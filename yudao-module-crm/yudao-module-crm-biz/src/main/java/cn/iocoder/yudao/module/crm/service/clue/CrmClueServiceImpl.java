package cn.iocoder.yudao.module.crm.service.clue;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.crm.controller.admin.clue.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.clue.CrmClueDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.crm.convert.clue.CrmClueConvert;
import cn.iocoder.yudao.module.crm.dal.mysql.clue.CrmClueMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;

/**
 * 线索 Service 实现类
 *
 * @author Wanwan
 */
@Service
@Validated
public class CrmClueServiceImpl implements CrmClueService {

    @Resource
    private CrmClueMapper clueMapper;

    @Override
    public Long createClue(CrmClueCreateReqVO createReqVO) {
        // 插入
        CrmClueDO clue = CrmClueConvert.INSTANCE.convert(createReqVO);
        clueMapper.insert(clue);
        // 返回
        return clue.getId();
    }

    @Override
    public void updateClue(CrmClueUpdateReqVO updateReqVO) {
        // 校验存在
        validateClueExists(updateReqVO.getId());
        // 更新
        CrmClueDO updateObj = CrmClueConvert.INSTANCE.convert(updateReqVO);
        clueMapper.updateById(updateObj);
    }

    @Override
    public void deleteClue(Long id) {
        // 校验存在
        validateClueExists(id);
        // 删除
        clueMapper.deleteById(id);
    }

    private void validateClueExists(Long id) {
        if (clueMapper.selectById(id) == null) {
            throw exception(CLUE_NOT_EXISTS);
        }
    }

    @Override
    public CrmClueDO getClue(Long id) {
        return clueMapper.selectById(id);
    }

    @Override
    public List<CrmClueDO> getClueList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return clueMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmClueDO> getCluePage(CrmCluePageReqVO pageReqVO) {
        return clueMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmClueDO> getClueList(CrmClueExportReqVO exportReqVO) {
        return clueMapper.selectList(exportReqVO);
    }

}
