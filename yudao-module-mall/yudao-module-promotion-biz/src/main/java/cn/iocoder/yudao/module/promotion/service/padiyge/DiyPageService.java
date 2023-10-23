package cn.iocoder.yudao.module.promotion.service.padiyge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.padiyge.vo.DiyPageCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.padiyge.vo.DiyPagePageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.padiyge.vo.DiyPageUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.padiyge.DiyPageDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 装修页面 Service 接口
 *
 * @author owen
 */
public interface DiyPageService {

    /**
     * 创建装修页面
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDiyPage(@Valid DiyPageCreateReqVO createReqVO);

    /**
     * 更新装修页面
     *
     * @param updateReqVO 更新信息
     */
    void updateDiyPage(@Valid DiyPageUpdateReqVO updateReqVO);

    /**
     * 删除装修页面
     *
     * @param id 编号
     */
    void deleteDiyPage(Long id);

    /**
     * 获得装修页面
     *
     * @param id 编号
     * @return 装修页面
     */
    DiyPageDO getDiyPage(Long id);

    /**
     * 获得装修页面列表
     *
     * @param ids 编号
     * @return 装修页面列表
     */
    List<DiyPageDO> getDiyPageList(Collection<Long> ids);

    /**
     * 获得装修页面分页
     *
     * @param pageReqVO 分页查询
     * @return 装修页面分页
     */
    PageResult<DiyPageDO> getDiyPagePage(DiyPagePageReqVO pageReqVO);

}
