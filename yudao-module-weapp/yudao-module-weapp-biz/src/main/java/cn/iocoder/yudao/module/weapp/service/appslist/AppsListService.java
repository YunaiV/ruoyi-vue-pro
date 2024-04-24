package cn.iocoder.yudao.module.weapp.service.appslist;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.weapp.controller.admin.appslist.vo.*;
import cn.iocoder.yudao.module.weapp.dal.dataobject.appslist.AppsListDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 小程序清单 Service 接口
 *
 * @author 芋道源码
 */
public interface AppsListService {

    /**
     * 创建小程序清单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createAppsList(@Valid AppsListSaveReqVO createReqVO);

    /**
     * 更新小程序清单
     *
     * @param updateReqVO 更新信息
     */
    void updateAppsList(@Valid AppsListSaveReqVO updateReqVO);

    /**
     * 删除小程序清单
     *
     * @param id 编号
     */
    void deleteAppsList(Integer id);

    /**
     * 获得小程序清单
     *
     * @param id 编号
     * @return 小程序清单
     */
    AppsListDO getAppsList(Integer id);

    /**
     * 获得小程序清单分页
     *
     * @param pageReqVO 分页查询
     * @return 小程序清单分页
     */
    PageResult<AppsListDO> getAppsListPage(AppsListPageReqVO pageReqVO);

}