package cn.iocoder.yudao.module.jl.service.crm;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.InstitutionDO;
import cn.iocoder.yudao.module.jl.dal.mysql.crm.InstitutionMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link InstitutionServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(InstitutionServiceImpl.class)
public class InstitutionServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InstitutionServiceImpl institutionService;

    @Resource
    private InstitutionMapper institutionMapper;

    @Test
    public void testCreateInstitution_success() {
        // 准备参数
        InstitutionCreateReqVO reqVO = randomPojo(InstitutionCreateReqVO.class);

        // 调用
        Long institutionId = institutionService.createInstitution(reqVO);
        // 断言
        assertNotNull(institutionId);
        // 校验记录的属性是否正确
        InstitutionDO institution = institutionMapper.selectById(institutionId);
        assertPojoEquals(reqVO, institution);
    }

    @Test
    public void testUpdateInstitution_success() {
        // mock 数据
        InstitutionDO dbInstitution = randomPojo(InstitutionDO.class);
        institutionMapper.insert(dbInstitution);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InstitutionUpdateReqVO reqVO = randomPojo(InstitutionUpdateReqVO.class, o -> {
            o.setId(dbInstitution.getId()); // 设置更新的 ID
        });

        // 调用
        institutionService.updateInstitution(reqVO);
        // 校验是否更新正确
        InstitutionDO institution = institutionMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, institution);
    }

    @Test
    public void testUpdateInstitution_notExists() {
        // 准备参数
        InstitutionUpdateReqVO reqVO = randomPojo(InstitutionUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> institutionService.updateInstitution(reqVO), INSTITUTION_NOT_EXISTS);
    }

    @Test
    public void testDeleteInstitution_success() {
        // mock 数据
        InstitutionDO dbInstitution = randomPojo(InstitutionDO.class);
        institutionMapper.insert(dbInstitution);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbInstitution.getId();

        // 调用
        institutionService.deleteInstitution(id);
       // 校验数据不存在了
       assertNull(institutionMapper.selectById(id));
    }

    @Test
    public void testDeleteInstitution_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> institutionService.deleteInstitution(id), INSTITUTION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInstitutionPage() {
       // mock 数据
       InstitutionDO dbInstitution = randomPojo(InstitutionDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setProvince(null);
           o.setCity(null);
           o.setName(null);
           o.setAddress(null);
           o.setMark(null);
           o.setType(null);
       });
       institutionMapper.insert(dbInstitution);
       // 测试 createTime 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setCreateTime(null)));
       // 测试 province 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setProvince(null)));
       // 测试 city 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setCity(null)));
       // 测试 name 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setName(null)));
       // 测试 address 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setAddress(null)));
       // 测试 mark 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setMark(null)));
       // 测试 type 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setType(null)));
       // 准备参数
       InstitutionPageReqVO reqVO = new InstitutionPageReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setProvince(null);
       reqVO.setCity(null);
       reqVO.setName(null);
       reqVO.setAddress(null);
       reqVO.setMark(null);
       reqVO.setType(null);

       // 调用
       PageResult<InstitutionDO> pageResult = institutionService.getInstitutionPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbInstitution, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInstitutionList() {
       // mock 数据
       InstitutionDO dbInstitution = randomPojo(InstitutionDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setProvince(null);
           o.setCity(null);
           o.setName(null);
           o.setAddress(null);
           o.setMark(null);
           o.setType(null);
       });
       institutionMapper.insert(dbInstitution);
       // 测试 createTime 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setCreateTime(null)));
       // 测试 province 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setProvince(null)));
       // 测试 city 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setCity(null)));
       // 测试 name 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setName(null)));
       // 测试 address 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setAddress(null)));
       // 测试 mark 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setMark(null)));
       // 测试 type 不匹配
       institutionMapper.insert(cloneIgnoreId(dbInstitution, o -> o.setType(null)));
       // 准备参数
       InstitutionExportReqVO reqVO = new InstitutionExportReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setProvince(null);
       reqVO.setCity(null);
       reqVO.setName(null);
       reqVO.setAddress(null);
       reqVO.setMark(null);
       reqVO.setType(null);

       // 调用
       List<InstitutionDO> list = institutionService.getInstitutionList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbInstitution, list.get(0));
    }

}
