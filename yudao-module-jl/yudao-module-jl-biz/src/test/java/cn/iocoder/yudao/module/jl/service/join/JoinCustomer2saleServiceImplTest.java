package cn.iocoder.yudao.module.jl.service.join;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.springframework.context.annotation.Import;
import java.util.*;

import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link JoinCustomer2saleServiceImpl} 的单元测试类
 *
 * @author 惟象科技
 */
@Import(JoinCustomer2saleServiceImpl.class)
public class JoinCustomer2saleServiceImplTest extends BaseDbUnitTest {

    @Resource
    private JoinCustomer2saleServiceImpl joinCustomer2saleService;

    @Resource
    private JoinCustomer2saleMapper joinCustomer2saleMapper;

    @Test
    public void testCreateJoinCustomer2sale_success() {
        // 准备参数
        JoinCustomer2saleCreateReqVO reqVO = randomPojo(JoinCustomer2saleCreateReqVO.class);

        // 调用
        Long joinCustomer2saleId = joinCustomer2saleService.createJoinCustomer2sale(reqVO);
        // 断言
        assertNotNull(joinCustomer2saleId);
        // 校验记录的属性是否正确
        JoinCustomer2saleDO joinCustomer2sale = joinCustomer2saleMapper.selectById(joinCustomer2saleId);
        assertPojoEquals(reqVO, joinCustomer2sale);
    }

    @Test
    public void testUpdateJoinCustomer2sale_success() {
        // mock 数据
        JoinCustomer2saleDO dbJoinCustomer2sale = randomPojo(JoinCustomer2saleDO.class);
        joinCustomer2saleMapper.insert(dbJoinCustomer2sale);// @Sql: 先插入出一条存在的数据
        // 准备参数
        JoinCustomer2saleUpdateReqVO reqVO = randomPojo(JoinCustomer2saleUpdateReqVO.class, o -> {
            o.setId(dbJoinCustomer2sale.getId()); // 设置更新的 ID
        });

        // 调用
        joinCustomer2saleService.updateJoinCustomer2sale(reqVO);
        // 校验是否更新正确
        JoinCustomer2saleDO joinCustomer2sale = joinCustomer2saleMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, joinCustomer2sale);
    }

    @Test
    public void testUpdateJoinCustomer2sale_notExists() {
        // 准备参数
        JoinCustomer2saleUpdateReqVO reqVO = randomPojo(JoinCustomer2saleUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> joinCustomer2saleService.updateJoinCustomer2sale(reqVO), JOIN_CUSTOMER2SALE_NOT_EXISTS);
    }

    @Test
    public void testDeleteJoinCustomer2sale_success() {
        // mock 数据
        JoinCustomer2saleDO dbJoinCustomer2sale = randomPojo(JoinCustomer2saleDO.class);
        joinCustomer2saleMapper.insert(dbJoinCustomer2sale);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbJoinCustomer2sale.getId();

        // 调用
        joinCustomer2saleService.deleteJoinCustomer2sale(id);
       // 校验数据不存在了
       assertNull(joinCustomer2saleMapper.selectById(id));
    }

    @Test
    public void testDeleteJoinCustomer2sale_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> joinCustomer2saleService.deleteJoinCustomer2sale(id), JOIN_CUSTOMER2SALE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetJoinCustomer2salePage() {
       // mock 数据
       JoinCustomer2saleDO dbJoinCustomer2sale = randomPojo(JoinCustomer2saleDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setCustomerId(null);
           o.setSalesId(null);
       });
       joinCustomer2saleMapper.insert(dbJoinCustomer2sale);
       // 测试 createTime 不匹配
       joinCustomer2saleMapper.insert(cloneIgnoreId(dbJoinCustomer2sale, o -> o.setCreateTime(null)));
       // 测试 customerId 不匹配
       joinCustomer2saleMapper.insert(cloneIgnoreId(dbJoinCustomer2sale, o -> o.setCustomerId(null)));
       // 测试 salesId 不匹配
       joinCustomer2saleMapper.insert(cloneIgnoreId(dbJoinCustomer2sale, o -> o.setSalesId(null)));
       // 准备参数
       JoinCustomer2salePageReqVO reqVO = new JoinCustomer2salePageReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setCustomerId(null);
       reqVO.setSalesId(null);

       // 调用
       PageResult<JoinCustomer2saleDO> pageResult = joinCustomer2saleService.getJoinCustomer2salePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbJoinCustomer2sale, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetJoinCustomer2saleList() {
       // mock 数据
       JoinCustomer2saleDO dbJoinCustomer2sale = randomPojo(JoinCustomer2saleDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setCustomerId(null);
           o.setSalesId(null);
       });
       joinCustomer2saleMapper.insert(dbJoinCustomer2sale);
       // 测试 createTime 不匹配
       joinCustomer2saleMapper.insert(cloneIgnoreId(dbJoinCustomer2sale, o -> o.setCreateTime(null)));
       // 测试 customerId 不匹配
       joinCustomer2saleMapper.insert(cloneIgnoreId(dbJoinCustomer2sale, o -> o.setCustomerId(null)));
       // 测试 salesId 不匹配
       joinCustomer2saleMapper.insert(cloneIgnoreId(dbJoinCustomer2sale, o -> o.setSalesId(null)));
       // 准备参数
       JoinCustomer2saleExportReqVO reqVO = new JoinCustomer2saleExportReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setCustomerId(null);
       reqVO.setSalesId(null);

       // 调用
       List<JoinCustomer2saleDO> list = joinCustomer2saleService.getJoinCustomer2saleList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbJoinCustomer2sale, list.get(0));
    }

}
