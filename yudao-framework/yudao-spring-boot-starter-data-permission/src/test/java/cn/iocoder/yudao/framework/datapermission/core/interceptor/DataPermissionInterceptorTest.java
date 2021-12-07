package cn.iocoder.yudao.framework.datapermission.core.interceptor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataPermissionInterceptorTest {

    private final DataPermissionInterceptor interceptor = new DataPermissionInterceptor();

    @Test
    public void selectSingle() {
        // 单表
        assertSql("select * from entity where id = ?",
                "SELECT * FROM entity WHERE id = ? AND tenant_id = 1");

        assertSql("select * from entity where id = ? or name = ?",
                "SELECT * FROM entity WHERE (id = ? OR name = ?) AND tenant_id = 1");

        assertSql("SELECT * FROM entity WHERE (id = ? OR name = ?)",
                "SELECT * FROM entity WHERE (id = ? OR name = ?) AND tenant_id = 1");

        /* not */
        assertSql("SELECT * FROM entity WHERE not (id = ? OR name = ?)",
                "SELECT * FROM entity WHERE NOT (id = ? OR name = ?) AND tenant_id = 1");
    }

    private void assertSql(String sql, String targetSql) {
        assertThat(interceptor.parserSingle(sql, null)).isEqualTo(targetSql);
    }

    public static void main(String[] args) {
        System.out.println("123");
    }

}
