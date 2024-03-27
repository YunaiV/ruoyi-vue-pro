package cn.iocoder.yudao.module.infra.service.demo.demo02;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.infra.controller.admin.demo.demo02.vo.Demo02CategoryListReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo.demo02.vo.Demo02CategorySaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo02.Demo02CategoryDO;
import cn.iocoder.yudao.module.infra.dal.mysql.demo.demo02.Demo02CategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;

/**
 * 示例分类 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class Demo02CategoryServiceImpl implements Demo02CategoryService {

    @Resource
    private Demo02CategoryMapper demo02CategoryMapper;

    @Override
    public Long createDemo02Category(Demo02CategorySaveReqVO createReqVO) {
        // 校验父级编号的有效性
        validateParentDemo02Category(null, createReqVO.getParentId());
        // 校验名字的唯一性
        validateDemo02CategoryNameUnique(null, createReqVO.getParentId(), createReqVO.getName());

        // 插入
        Demo02CategoryDO demo02Category = BeanUtils.toBean(createReqVO, Demo02CategoryDO.class);
        demo02CategoryMapper.insert(demo02Category);
        // 返回
        return demo02Category.getId();
    }

    @Override
    public void updateDemo02Category(Demo02CategorySaveReqVO updateReqVO) {
        // 校验存在
        validateDemo02CategoryExists(updateReqVO.getId());
        // 校验父级编号的有效性
        validateParentDemo02Category(updateReqVO.getId(), updateReqVO.getParentId());
        // 校验名字的唯一性
        validateDemo02CategoryNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());

        // 更新
        Demo02CategoryDO updateObj = BeanUtils.toBean(updateReqVO, Demo02CategoryDO.class);
        demo02CategoryMapper.updateById(updateObj);
    }

    @Override
    public void deleteDemo02Category(Long id) {
        // 校验存在
        validateDemo02CategoryExists(id);
        // 校验是否有子示例分类
        if (demo02CategoryMapper.selectCountByParentId(id) > 0) {
            throw exception(DEMO02_CATEGORY_EXITS_CHILDREN);
        }
        // 删除
        demo02CategoryMapper.deleteById(id);
    }

    private void validateDemo02CategoryExists(Long id) {
        if (demo02CategoryMapper.selectById(id) == null) {
            throw exception(DEMO02_CATEGORY_NOT_EXISTS);
        }
    }

    private void validateParentDemo02Category(Long id, Long parentId) {
        if (parentId == null || Demo02CategoryDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父示例分类
        if (Objects.equals(id, parentId)) {
            throw exception(DEMO02_CATEGORY_PARENT_ERROR);
        }
        // 2. 父示例分类不存在
        Demo02CategoryDO parentDemo02Category = demo02CategoryMapper.selectById(parentId);
        if (parentDemo02Category == null) {
            throw exception(DEMO02_CATEGORY_PARENT_NOT_EXITS);
        }
        // 3. 递归校验父示例分类，如果父示例分类是自己的子示例分类，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentDemo02Category.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(DEMO02_CATEGORY_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父示例分类
            if (parentId == null || Demo02CategoryDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentDemo02Category = demo02CategoryMapper.selectById(parentId);
            if (parentDemo02Category == null) {
                break;
            }
        }
    }

    private void validateDemo02CategoryNameUnique(Long id, Long parentId, String name) {
        Demo02CategoryDO demo02Category = demo02CategoryMapper.selectByParentIdAndName(parentId, name);
        if (demo02Category == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的示例分类
        if (id == null) {
            throw exception(DEMO02_CATEGORY_NAME_DUPLICATE);
        }
        if (!Objects.equals(demo02Category.getId(), id)) {
            throw exception(DEMO02_CATEGORY_NAME_DUPLICATE);
        }
    }

    @Override
    public Demo02CategoryDO getDemo02Category(Long id) {
        return demo02CategoryMapper.selectById(id);
    }

    @Override
    public List<Demo02CategoryDO> getDemo02CategoryList(Demo02CategoryListReqVO listReqVO) {
        return demo02CategoryMapper.selectList(listReqVO);
    }

}