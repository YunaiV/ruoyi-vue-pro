package cn.iocoder.yudao.module.demo.service.infra.net;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.demo.controller.admin.infra.net.vo.*;
import cn.iocoder.yudao.module.demo.dal.dataobject.infra.net.NetDO;
import cn.iocoder.yudao.module.demo.dal.mysql.infra.net.NetMapper;
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
* {@link NetServiceImpl} 的单元测试类
*
* @author ruanzh.eth
*/
@Import(NetServiceImpl.class)
public class NetServiceImplTest extends BaseDbUnitTest {

    @Resource
    private NetServiceImpl netService;

    @Resource
    private NetMapper netMapper;

    @Test
    public void testCreateNet_success() {
        // 准备参数
        NetCreateReqVO reqVO = randomPojo(NetCreateReqVO.class);

        // 调用
        Long netId = netService.createNet(reqVO);
        // 断言
        assertNotNull(netId);
        // 校验记录的属性是否正确
        NetDO net = netMapper.selectById(netId);
        assertPojoEquals(reqVO, net);
    }

    @Test
    public void testUpdateNet_success() {
        // mock 数据
        NetDO dbNet = randomPojo(NetDO.class);
        netMapper.insert(dbNet);// @Sql: 先插入出一条存在的数据
        // 准备参数
        NetUpdateReqVO reqVO = randomPojo(NetUpdateReqVO.class, o -> {
            o.setId(dbNet.getId()); // 设置更新的 ID
        });

        // 调用
        netService.updateNet(reqVO);
        // 校验是否更新正确
        NetDO net = netMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, net);
    }

    @Test
    public void testUpdateNet_notExists() {
        // 准备参数
        NetUpdateReqVO reqVO = randomPojo(NetUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> netService.updateNet(reqVO), NET_NOT_EXISTS);
    }

    @Test
    public void testDeleteNet_success() {
        // mock 数据
        NetDO dbNet = randomPojo(NetDO.class);
        netMapper.insert(dbNet);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbNet.getId();

        // 调用
        netService.deleteNet(id);
       // 校验数据不存在了
       assertNull(netMapper.selectById(id));
    }

    @Test
    public void testDeleteNet_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> netService.deleteNet(id), NET_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetNetPage() {
       // mock 数据
       NetDO dbNet = randomPojo(NetDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setExplorer(null);
           o.setSymbol(null);
           o.setEndpoint(null);
           o.setType(null);
           o.setInfo(null);
           o.setCreateTime(null);
       });
       netMapper.insert(dbNet);
       // 测试 name 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setName(null)));
       // 测试 explorer 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setExplorer(null)));
       // 测试 symbol 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setSymbol(null)));
       // 测试 endpoint 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setEndpoint(null)));
       // 测试 type 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setType(null)));
       // 测试 info 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setInfo(null)));
       // 测试 createTime 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setCreateTime(null)));
       // 准备参数
       NetPageReqVO reqVO = new NetPageReqVO();
       reqVO.setName(null);
       reqVO.setExplorer(null);
       reqVO.setSymbol(null);
       reqVO.setEndpoint(null);
       reqVO.setType(null);
       reqVO.setInfo(null);
       reqVO.setCreateTime((new Date[]{}));

       // 调用
       PageResult<NetDO> pageResult = netService.getNetPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbNet, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetNetList() {
       // mock 数据
       NetDO dbNet = randomPojo(NetDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setExplorer(null);
           o.setSymbol(null);
           o.setEndpoint(null);
           o.setType(null);
           o.setInfo(null);
           o.setCreateTime(null);
       });
       netMapper.insert(dbNet);
       // 测试 name 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setName(null)));
       // 测试 explorer 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setExplorer(null)));
       // 测试 symbol 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setSymbol(null)));
       // 测试 endpoint 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setEndpoint(null)));
       // 测试 type 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setType(null)));
       // 测试 info 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setInfo(null)));
       // 测试 createTime 不匹配
       netMapper.insert(cloneIgnoreId(dbNet, o -> o.setCreateTime(null)));
       // 准备参数
       NetExportReqVO reqVO = new NetExportReqVO();
       reqVO.setName(null);
       reqVO.setExplorer(null);
       reqVO.setSymbol(null);
       reqVO.setEndpoint(null);
       reqVO.setType(null);
       reqVO.setInfo(null);
       reqVO.setCreateTime((new Date[]{}));

       // 调用
       List<NetDO> list = netService.getNetList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbNet, list.get(0));
    }

}
