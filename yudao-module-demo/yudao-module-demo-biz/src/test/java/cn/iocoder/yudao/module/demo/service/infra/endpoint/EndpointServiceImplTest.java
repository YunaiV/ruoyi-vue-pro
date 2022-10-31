package cn.iocoder.yudao.module.demo.service.infra.endpoint;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.demo.controller.admin.infra.endpoint.vo.*;
import cn.iocoder.yudao.module.demo.dal.dataobject.infra.endpoint.EndpointDO;
import cn.iocoder.yudao.module.demo.dal.mysql.infra.endpoint.EndpointMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.demo.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
* {@link EndpointServiceImpl} 的单元测试类
*
* @author ruanzh.eth
*/
@Import(EndpointServiceImpl.class)
public class EndpointServiceImplTest extends BaseDbUnitTest {

    @Resource
    private EndpointServiceImpl endpointService;

    @Resource
    private EndpointMapper endpointMapper;

    @Test
    public void testCreateEndpoint_success() {
        // 准备参数
        EndpointCreateReqVO reqVO = randomPojo(EndpointCreateReqVO.class);

        // 调用
        Long endpointId = endpointService.createEndpoint(reqVO);
        // 断言
        assertNotNull(endpointId);
        // 校验记录的属性是否正确
        EndpointDO endpoint = endpointMapper.selectById(endpointId);
        assertPojoEquals(reqVO, endpoint);
    }

    @Test
    public void testUpdateEndpoint_success() {
        // mock 数据
        EndpointDO dbEndpoint = randomPojo(EndpointDO.class);
        endpointMapper.insert(dbEndpoint);// @Sql: 先插入出一条存在的数据
        // 准备参数
        EndpointUpdateReqVO reqVO = randomPojo(EndpointUpdateReqVO.class, o -> {
            o.setId(dbEndpoint.getId()); // 设置更新的 ID
        });

        // 调用
        endpointService.updateEndpoint(reqVO);
        // 校验是否更新正确
        EndpointDO endpoint = endpointMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, endpoint);
    }

    @Test
    public void testUpdateEndpoint_notExists() {
        // 准备参数
        EndpointUpdateReqVO reqVO = randomPojo(EndpointUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> endpointService.updateEndpoint(reqVO), ENDPOINT_NOT_EXISTS);
    }

    @Test
    public void testDeleteEndpoint_success() {
        // mock 数据
        EndpointDO dbEndpoint = randomPojo(EndpointDO.class);
        endpointMapper.insert(dbEndpoint);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbEndpoint.getId();

        // 调用
        endpointService.deleteEndpoint(id);
       // 校验数据不存在了
       assertNull(endpointMapper.selectById(id));
    }

    @Test
    public void testDeleteEndpoint_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> endpointService.deleteEndpoint(id), ENDPOINT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetEndpointPage() {
       // mock 数据
       EndpointDO dbEndpoint = randomPojo(EndpointDO.class, o -> { // 等会查询到
           o.setNetId(null);
           o.setName(null);
           o.setUrl(null);
           o.setBlocked(null);
           o.setInfo(null);
           o.setCreateTime(null);
       });
       endpointMapper.insert(dbEndpoint);
       // 测试 netId 不匹配
       endpointMapper.insert(cloneIgnoreId(dbEndpoint, o -> o.setNetId(null)));
       // 测试 name 不匹配
       endpointMapper.insert(cloneIgnoreId(dbEndpoint, o -> o.setName(null)));
       // 测试 url 不匹配
       endpointMapper.insert(cloneIgnoreId(dbEndpoint, o -> o.setUrl(null)));
       // 测试 blocked 不匹配
       endpointMapper.insert(cloneIgnoreId(dbEndpoint, o -> o.setBlocked(null)));
       // 测试 info 不匹配
       endpointMapper.insert(cloneIgnoreId(dbEndpoint, o -> o.setInfo(null)));
       // 测试 createTime 不匹配
       endpointMapper.insert(cloneIgnoreId(dbEndpoint, o -> o.setCreateTime(null)));
       // 准备参数
       EndpointPageReqVO reqVO = new EndpointPageReqVO();
       reqVO.setNetId(null);
       reqVO.setName(null);
       reqVO.setUrl(null);
       reqVO.setBlocked(null);
       reqVO.setInfo(null);
       reqVO.setCreateTime((new Date[]{}));

       // 调用
       PageResult<EndpointDO> pageResult = endpointService.getEndpointPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbEndpoint, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetEndpointList() {
       // mock 数据
       EndpointDO dbEndpoint = randomPojo(EndpointDO.class, o -> { // 等会查询到
           o.setNetId(null);
           o.setName(null);
           o.setUrl(null);
           o.setBlocked(null);
           o.setInfo(null);
           o.setCreateTime(null);
       });
       endpointMapper.insert(dbEndpoint);
       // 测试 netId 不匹配
       endpointMapper.insert(cloneIgnoreId(dbEndpoint, o -> o.setNetId(null)));
       // 测试 name 不匹配
       endpointMapper.insert(cloneIgnoreId(dbEndpoint, o -> o.setName(null)));
       // 测试 url 不匹配
       endpointMapper.insert(cloneIgnoreId(dbEndpoint, o -> o.setUrl(null)));
       // 测试 blocked 不匹配
       endpointMapper.insert(cloneIgnoreId(dbEndpoint, o -> o.setBlocked(null)));
       // 测试 info 不匹配
       endpointMapper.insert(cloneIgnoreId(dbEndpoint, o -> o.setInfo(null)));
       // 测试 createTime 不匹配
       endpointMapper.insert(cloneIgnoreId(dbEndpoint, o -> o.setCreateTime(null)));
       // 准备参数
       EndpointExportReqVO reqVO = new EndpointExportReqVO();
       reqVO.setNetId(null);
       reqVO.setName(null);
       reqVO.setUrl(null);
       reqVO.setBlocked(null);
       reqVO.setInfo(null);
       reqVO.setCreateTime((new Date[]{}));

       // 调用
       List<EndpointDO> list = endpointService.getEndpointList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbEndpoint, list.get(0));
    }

}
