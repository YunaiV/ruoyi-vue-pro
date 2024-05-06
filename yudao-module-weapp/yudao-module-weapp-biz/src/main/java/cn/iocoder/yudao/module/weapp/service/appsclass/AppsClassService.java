package cn.iocoder.yudao.module.weapp.service.appsclass;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.weapp.controller.admin.appsclass.vo.*;
import cn.iocoder.yudao.module.weapp.dal.dataobject.appsclass.AppsClassDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 小程序分类 Service 接口
 *
 * @author 芋道源码
 */
public interface AppsClassService {

    /**
     * 创建小程序分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createAppsClass(@Valid AppsClassSaveReqVO createReqVO);

    /**
     * 更新小程序分类
     *
     * @param updateReqVO 更新信息
     */
    void updateAppsClass(@Valid AppsClassSaveReqVO updateReqVO);

    /**
     * 删除小程序分类
     *
     * @param id 编号
     */
    void deleteAppsClass(Integer id);

    /**
     * 获得小程序分类
     *
     * @param id 编号
     * @return 小程序分类
     */
    AppsClassDO getAppsClass(Integer id);

    /**
     * 获得小程序分类分页
     *
     * @param pageReqVO 分页查询
     * @return 小程序分类分页
     */
    PageResult<AppsClassDO> getAppsClassPage(AppsClassPageReqVO pageReqVO);

}