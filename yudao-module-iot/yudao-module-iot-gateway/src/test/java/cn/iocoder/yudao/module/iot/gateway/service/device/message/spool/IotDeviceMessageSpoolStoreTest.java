package cn.iocoder.yudao.module.iot.gateway.service.device.message.spool;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotDeviceMessageSpoolStore} 的单元测试：使用临时目录的真实 SQLite 文件
 *
 * @author 芋道源码
 */
public class IotDeviceMessageSpoolStoreTest {

    @TempDir
    File tempDir;

    @Test
    public void testInsertSelectDelete() throws SQLException {
        // 准备参数
        String file = new File(tempDir, "spool/spool.db").getPath();
        IotDeviceMessageSpoolStore store = new IotDeviceMessageSpoolStore(file);
        store.open();
        try {
            // 调用：写入 3 条
            store.insert(1000L, "m1", 10);
            store.insert(2000L, "m2", 20);
            store.insert(3000L, "m3", 30);

            // 断言：按编号顺序读取，统计消息数、字节数
            List<IotDeviceMessageSpoolStore.SpoolRecord> records = store.selectList(2);
            assertEquals(2, records.size());
            assertEquals("m1", records.get(0).getMessage());
            assertEquals("m2", records.get(1).getMessage());
            assertEquals(3, store.getCount());
            assertEquals(60, store.getBytes());

            // 调用：删除已补传的前 2 条
            store.deleteUpTo(records.get(1).getId());
            // 断言：释放配额
            assertEquals(1, store.getCount());
            assertEquals(30, store.getBytes());
            assertEquals("m3", store.selectList(10).get(0).getMessage());
        } finally {
            store.close();
        }
    }

    @Test
    public void testDeleteExpired() throws SQLException {
        // 准备参数
        IotDeviceMessageSpoolStore store = new IotDeviceMessageSpoolStore(new File(tempDir, "spool.db").getPath());
        store.open();
        try {
            store.insert(1000L, "m1", 10);
            store.insert(2000L, "m2", 20);
            store.insert(3000L, "m3", 30);

            // 调用：删除接收时刻早于 2500 的
            long deleted = store.deleteExpired(2500L);

            // 断言
            assertEquals(2, deleted);
            assertEquals(1, store.getCount());
            assertEquals(30, store.getBytes());
            assertEquals(0, store.deleteExpired(2500L));
        } finally {
            store.close();
        }
    }

    @Test
    public void testOpen_reopenKeepsRecords() throws SQLException {
        // 准备参数：写入后关闭
        String file = new File(tempDir, "spool.db").getPath();
        IotDeviceMessageSpoolStore store = new IotDeviceMessageSpoolStore(file);
        store.open();
        store.insert(1000L, "m1", 10);
        store.insert(2000L, "m2", 20);
        store.close();

        // 调用：重新打开
        IotDeviceMessageSpoolStore reopened = new IotDeviceMessageSpoolStore(file);
        reopened.open();
        try {
            // 断言：从文件统计已暂存的消息
            assertEquals(2, reopened.getCount());
            assertEquals(30, reopened.getBytes());
            assertEquals("m1", reopened.selectList(1).get(0).getMessage());
        } finally {
            reopened.close();
        }
    }

    @Test
    public void testOpen_corruptedFile() {
        // 准备参数：不是 SQLite 的文件
        File file = new File(tempDir, "spool.db");
        FileUtil.writeString("not a sqlite database file, just some garbage bytes", file, StandardCharsets.UTF_8);
        IotDeviceMessageSpoolStore store = new IotDeviceMessageSpoolStore(file.getPath());

        // 调用，并断言：打开失败，文件保留
        assertThrows(SQLException.class, store::open);
        assertTrue(file.exists());
    }

}
