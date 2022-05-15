package cn.iocoder.yudao.module.product.service.attrkey;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.product.controller.admin.attrkey.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.attrkey.AttrKeyDO;
import cn.iocoder.yudao.module.product.dal.mysql.attrkey.AttrKeyMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.product.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
* {@link AttrKeyServiceImpl} 的单元测试类
*
* @author 芋道源码
*/
@Import(AttrKeyServiceImpl.class)
public class AttrKeyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AttrKeyServiceImpl attrKeyService;

    @Resource
    private AttrKeyMapper attrKeyMapper;

    @Test
    public void testCreateAttrKey_success() {
        // 准备参数
        AttrKeyCreateReqVO reqVO = randomPojo(AttrKeyCreateReqVO.class);

        // 调用
        Integer attrKeyId = attrKeyService.createAttrKey(reqVO);
        // 断言
        assertNotNull(attrKeyId);
        // 校验记录的属性是否正确
        AttrKeyDO attrKey = attrKeyMapper.selectById(attrKeyId);
        assertPojoEquals(reqVO, attrKey);
    }

    @Test
    public void testUpdateAttrKey_success() {
        // mock 数据
        AttrKeyDO dbAttrKey = randomPojo(AttrKeyDO.class);
        attrKeyMapper.insert(dbAttrKey);// @Sql: 先插入出一条存在的数据
        // 准备参数
        AttrKeyUpdateReqVO reqVO = randomPojo(AttrKeyUpdateReqVO.class, o -> {
            o.setId(dbAttrKey.getId()); // 设置更新的 ID
        });

        // 调用
        attrKeyService.updateAttrKey(reqVO);
        // 校验是否更新正确
        AttrKeyDO attrKey = attrKeyMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, attrKey);
    }

    @Test
    public void testUpdateAttrKey_notExists() {
        // 准备参数
        AttrKeyUpdateReqVO reqVO = randomPojo(AttrKeyUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> attrKeyService.updateAttrKey(reqVO), ATTR_KEY_NOT_EXISTS);
    }

    @Test
    public void testDeleteAttrKey_success() {
        // mock 数据
        AttrKeyDO dbAttrKey = randomPojo(AttrKeyDO.class);
        attrKeyMapper.insert(dbAttrKey);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Integer id = dbAttrKey.getId();

        // 调用
        attrKeyService.deleteAttrKey(id);
       // 校验数据不存在了
       assertNull(attrKeyMapper.selectById(id));
    }

    @Test
    public void testDeleteAttrKey_notExists() {
        // 准备参数
//        Integer id = randomIntegerId();
        Integer id = 1;

        // 调用, 并断言异常
        assertServiceException(() -> attrKeyService.deleteAttrKey(id), ATTR_KEY_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetAttrKeyPage() {
       // mock 数据
       AttrKeyDO dbAttrKey = randomPojo(AttrKeyDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setAttrName(null);
           o.setStatus(null);
       });
       attrKeyMapper.insert(dbAttrKey);
       // 测试 createTime 不匹配
       attrKeyMapper.insert(cloneIgnoreId(dbAttrKey, o -> o.setCreateTime(null)));
       // 测试 attrName 不匹配
       attrKeyMapper.insert(cloneIgnoreId(dbAttrKey, o -> o.setAttrName(null)));
       // 测试 status 不匹配
       attrKeyMapper.insert(cloneIgnoreId(dbAttrKey, o -> o.setStatus(null)));
       // 准备参数
       AttrKeyPageReqVO reqVO = new AttrKeyPageReqVO();
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);
       reqVO.setAttrName(null);
       reqVO.setStatus(null);

       // 调用
       PageResult<AttrKeyDO> pageResult = attrKeyService.getAttrKeyPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbAttrKey, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetAttrKeyList() {
       // mock 数据
       AttrKeyDO dbAttrKey = randomPojo(AttrKeyDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setAttrName(null);
           o.setStatus(null);
       });
       attrKeyMapper.insert(dbAttrKey);
       // 测试 createTime 不匹配
       attrKeyMapper.insert(cloneIgnoreId(dbAttrKey, o -> o.setCreateTime(null)));
       // 测试 attrName 不匹配
       attrKeyMapper.insert(cloneIgnoreId(dbAttrKey, o -> o.setAttrName(null)));
       // 测试 status 不匹配
       attrKeyMapper.insert(cloneIgnoreId(dbAttrKey, o -> o.setStatus(null)));
       // 准备参数
       AttrKeyExportReqVO reqVO = new AttrKeyExportReqVO();
       reqVO.setBeginCreateTime(null);
       reqVO.setEndCreateTime(null);
       reqVO.setAttrName(null);
       reqVO.setStatus(null);

       // 调用
       List<AttrKeyDO> list = attrKeyService.getAttrKeyList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbAttrKey, list.get(0));
    }

}
