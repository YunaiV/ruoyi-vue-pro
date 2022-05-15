package cn.iocoder.yudao.module.product.service.attrvalue;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.product.controller.admin.attrvalue.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.attrvalue.AttrValueDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.product.convert.attrvalue.AttrValueConvert;
import cn.iocoder.yudao.module.product.dal.mysql.attrvalue.AttrValueMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 规格值 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AttrValueServiceImpl implements AttrValueService {

    @Resource
    private AttrValueMapper attrValueMapper;

    @Override
    public Integer createAttrValue(AttrValueCreateReqVO createReqVO) {
        // 插入
        AttrValueDO attrValue = AttrValueConvert.INSTANCE.convert(createReqVO);
        attrValueMapper.insert(attrValue);
        // 返回
        return attrValue.getId();
    }

    @Override
    public void updateAttrValue(AttrValueUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateAttrValueExists(updateReqVO.getId());
        // 更新
        AttrValueDO updateObj = AttrValueConvert.INSTANCE.convert(updateReqVO);
        attrValueMapper.updateById(updateObj);
    }

    @Override
    public void deleteAttrValue(Integer id) {
        // 校验存在
        this.validateAttrValueExists(id);
        // 删除
        attrValueMapper.deleteById(id);
    }

    private void validateAttrValueExists(Integer id) {
        if (attrValueMapper.selectById(id) == null) {
            throw exception(ATTR_VALUE_NOT_EXISTS);
        }
    }

    @Override
    public AttrValueDO getAttrValue(Integer id) {
        return attrValueMapper.selectById(id);
    }

    @Override
    public List<AttrValueDO> getAttrValueList(Collection<Integer> ids) {
        return attrValueMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<AttrValueDO> getAttrValuePage(AttrValuePageReqVO pageReqVO) {
        return attrValueMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AttrValueDO> getAttrValueList(AttrValueExportReqVO exportReqVO) {
        return attrValueMapper.selectList(exportReqVO);
    }

}
