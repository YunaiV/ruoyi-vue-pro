package cn.iocoder.yudao.module.infra.service.demo.demo02;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.infra.controller.admin.demo.demo02.vo.Demo02CategoryListReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo.demo02.vo.Demo02CategorySaveReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo.demo02.Demo02CategoryDO;

/**
 * 示例分类 Service 接口
 *
 * @author 芋道源码
 */
public interface Demo02CategoryService {

    /**
     * 创建示例分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDemo02Category(@Valid Demo02CategorySaveReqVO createReqVO);

    /**
     * 更新示例分类
     *
     * @param updateReqVO 更新信息
     */
    void updateDemo02Category(@Valid Demo02CategorySaveReqVO updateReqVO);

    /**
     * 删除示例分类
     *
     * @param id 编号
     */
    void deleteDemo02Category(Long id);

    /**
     * 获得示例分类
     *
     * @param id 编号
     * @return 示例分类
     */
    Demo02CategoryDO getDemo02Category(Long id);

    /**
     * 获得示例分类列表
     *
     * @param listReqVO 查询条件
     * @return 示例分类列表
     */
    List<Demo02CategoryDO> getDemo02CategoryList(Demo02CategoryListReqVO listReqVO);

}