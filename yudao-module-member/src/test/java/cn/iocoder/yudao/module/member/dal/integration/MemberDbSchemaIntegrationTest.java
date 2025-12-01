package cn.iocoder.yudao.module.member.dal.integration;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbIntegrationTest;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelDO;
import cn.iocoder.yudao.module.member.dal.dataobject.tag.MemberTagDO;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import cn.iocoder.yudao.module.member.dal.mysql.level.MemberLevelMapper;
import cn.iocoder.yudao.module.member.dal.mysql.tag.MemberTagMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Member 模块数据库结构集成测试
 */
@DisplayName("Member模块-数据库结构集成测试")
public class MemberDbSchemaIntegrationTest extends BaseDbIntegrationTest {

    @Resource
    private MemberUserMapper userMapper;

    @Resource
    private MemberLevelMapper levelMapper;

    @Resource
    private MemberTagMapper tagMapper;

    @Test
    @DisplayName("验证 member_user 表结构")
    void testUserTable() {
        List<MemberUserDO> list = userMapper.selectList();
        assertNotNull(list, "会员用户列表不应为空");
        System.out.println("member_user 表记录数: " + list.size());
    }

    @Test
    @DisplayName("验证 member_level 表结构")
    void testLevelTable() {
        List<MemberLevelDO> list = levelMapper.selectList();
        assertNotNull(list, "会员等级列表不应为空");
        System.out.println("member_level 表记录数: " + list.size());
    }

    @Test
    @DisplayName("验证 member_tag 表结构")
    void testTagTable() {
        List<MemberTagDO> list = tagMapper.selectList();
        assertNotNull(list, "会员标签列表不应为空");
        System.out.println("member_tag 表记录数: " + list.size());
    }

    @Test
    @DisplayName("综合验证 - 所有核心表结构")
    void testAllCoreTables() {
        System.out.println("========== 开始综合验证 Member 模块表结构 ==========");
        assertDoesNotThrow(() -> userMapper.selectList(), "member_user 表查询失败");
        assertDoesNotThrow(() -> levelMapper.selectList(), "member_level 表查询失败");
        assertDoesNotThrow(() -> tagMapper.selectList(), "member_tag 表查询失败");
        System.out.println("========== Member 模块表结构验证通过 ==========");
    }
}
