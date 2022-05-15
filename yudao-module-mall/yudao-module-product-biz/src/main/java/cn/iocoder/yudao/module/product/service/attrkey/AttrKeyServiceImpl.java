package cn.iocoder.yudao.module.product.service.attrkey;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.product.controller.admin.attrkey.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.attrkey.AttrKeyDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.product.convert.attrkey.AttrKeyConvert;
import cn.iocoder.yudao.module.product.dal.mysql.attrkey.AttrKeyMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;

/**
 * 规格名称 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class AttrKeyServiceImpl implements AttrKeyService {

    @Resource
    private AttrKeyMapper attrKeyMapper;

    @Override
    public Integer createAttrKey(AttrKeyCreateReqVO createReqVO) {
        // 插入
        AttrKeyDO attrKey = AttrKeyConvert.INSTANCE.convert(createReqVO);
        attrKeyMapper.insert(attrKey);
        // 返回
        return attrKey.getId();
    }

    @Override
    public void updateAttrKey(AttrKeyUpdateReqVO updateReqVO) {
        // 校验存在
        this.validateAttrKeyExists(updateReqVO.getId());
        // 更新
        AttrKeyDO updateObj = AttrKeyConvert.INSTANCE.convert(updateReqVO);
        attrKeyMapper.updateById(updateObj);
    }

    @Override
    public void deleteAttrKey(Integer id) {
        // 校验存在
        this.validateAttrKeyExists(id);
        // 删除
        attrKeyMapper.deleteById(id);
    }

    private void validateAttrKeyExists(Integer id) {
        if (attrKeyMapper.selectById(id) == null) {
            throw exception(ATTR_KEY_NOT_EXISTS);
        }
    }

    @Override
    public AttrKeyDO getAttrKey(Integer id) {
        return attrKeyMapper.selectById(id);
    }

    @Override
    public List<AttrKeyDO> getAttrKeyList(Collection<Integer> ids) {
        return attrKeyMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<AttrKeyDO> getAttrKeyPage(AttrKeyPageReqVO pageReqVO) {
        return attrKeyMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AttrKeyDO> getAttrKeyList(AttrKeyExportReqVO exportReqVO) {
        return attrKeyMapper.selectList(exportReqVO);
    }

}
