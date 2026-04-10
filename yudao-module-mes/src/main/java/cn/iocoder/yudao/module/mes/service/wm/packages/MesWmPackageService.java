package cn.iocoder.yudao.module.mes.service.wm.packages;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.MesWmPackagePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.packages.vo.MesWmPackageSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.packages.MesWmPackageDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 装箱单 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmPackageService {

    /**
     * 创建装箱单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPackage(@Valid MesWmPackageSaveReqVO createReqVO);

    /**
     * 修改装箱单
     *
     * @param updateReqVO 修改信息
     */
    void updatePackage(@Valid MesWmPackageSaveReqVO updateReqVO);

    /**
     * 删除装箱单
     *
     * @param id 编号
     */
    void deletePackage(Long id);

    /**
     * 获得装箱单
     *
     * @param id 编号
     * @return 装箱单
     */
    MesWmPackageDO getPackage(Long id);

    /**
     * 获得装箱单分页
     *
     * @param pageReqVO 分页查询
     * @return 分页结果
     */
    PageResult<MesWmPackageDO> getPackagePage(MesWmPackagePageReqVO pageReqVO);

    /**
     * 完成装箱单
     *
     * @param id 编号
     */
    void finishPackage(Long id);

    /**
     * 校验装箱单状态为草稿
     *
     * @param packageId 装箱单 ID
     */
    void validatePackageStatusDraft(Long packageId);

    /**
     * 添加子箱
     *
     * @param parentId 父箱 ID
     * @param childId  子箱 ID
     */
    void addChildPackage(Long parentId, Long childId);

    /**
     * 移除子箱
     *
     * @param childId 子箱 ID
     */
    void removeChildPackage(Long childId);

    /**
     * 可添加为子箱的装箱单列表（无父箱 + 已完成状态）
     *
     * @return 装箱单列表
     */
    List<MesWmPackageDO> getChildablePackageList();

    /**
     * 获得装箱单精简列表
     *
     * @return 装箱单列表
     */
    List<MesWmPackageDO> getPackageSimpleList();

    /**
     * 获取指定装箱单及其所有子孙箱的 ID 列表
     *
     * @param packageId 装箱单 ID
     * @return 装箱单及其所有子孙箱的 ID 集合
     */
    List<Long> getPackageAndDescendantIds(Long packageId);

}
