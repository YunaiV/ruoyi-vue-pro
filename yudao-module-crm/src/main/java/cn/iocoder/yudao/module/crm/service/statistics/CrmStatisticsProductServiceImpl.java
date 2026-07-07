package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.category.CrmProductCategoryListReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.product.CrmStatisticsProductCategoryRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.product.CrmStatisticsProductReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.product.CrmStatisticsProductSalesRespVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductCategoryDO;
import cn.iocoder.yudao.module.crm.dal.mysql.statistics.CrmStatisticsProductMapper;
import cn.iocoder.yudao.module.crm.service.product.CrmProductCategoryService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

/**
 * CRM 产品分析 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CrmStatisticsProductServiceImpl implements CrmStatisticsProductService {

    @Resource
    private CrmStatisticsProductMapper productMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private CrmProductCategoryService productCategoryService;

    @Override
    public List<CrmStatisticsProductSalesRespVO> getProductSalesList(CrmStatisticsProductReqVO reqVO) {
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }
        setCategoryIds(reqVO);
        return productMapper.selectProductSalesList(reqVO);
    }

    @Override
    public List<CrmStatisticsProductCategoryRespVO> getProductCategorySummary(CrmStatisticsProductReqVO reqVO) {
        reqVO.setUserIds(getUserIds(reqVO));
        if (CollUtil.isEmpty(reqVO.getUserIds())) {
            return Collections.emptyList();
        }
        setCategoryIds(reqVO);
        return productMapper.selectProductCategorySummary(reqVO);
    }

    /**
     * 设置产品分类编号数组，选择父分类时，包含子分类
     *
     * @param reqVO 请求参数
     */
    private void setCategoryIds(CrmStatisticsProductReqVO reqVO) {
        if (ObjUtil.isNull(reqVO.getCategoryId())) {
            return;
        }
        List<Long> categoryIds = ListUtil.of(reqVO.getCategoryId());
        List<CrmProductCategoryDO> categoryList = productCategoryService.getProductCategoryList(
                new CrmProductCategoryListReqVO());
        categoryIds.addAll(convertList(categoryList, CrmProductCategoryDO::getId,
                category -> Objects.equals(category.getParentId(), reqVO.getCategoryId())));
        reqVO.setCategoryIds(categoryIds);
    }

    /**
     * 获取用户编号数组
     *
     * 如果用户编号为空，则获得部门下的用户编号数组，包括子部门的所有用户编号
     *
     * @param reqVO 请求参数
     * @return 用户编号数组
     */
    private List<Long> getUserIds(CrmStatisticsProductReqVO reqVO) {
        // 情况一：选中某个用户
        if (ObjUtil.isNotNull(reqVO.getUserId())) {
            return ListUtil.of(reqVO.getUserId());
        }
        // 情况二：选中某个部门
        // 2.1 获得部门列表
        List<Long> deptIds = convertList(deptApi.getChildDeptList(reqVO.getDeptId()), DeptRespDTO::getId);
        deptIds.add(reqVO.getDeptId());
        // 2.2 获得用户编号
        return convertList(adminUserApi.getUserListByDeptIds(deptIds), AdminUserRespDTO::getId);
    }

}
