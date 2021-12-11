package cn.iocoder.yudao.framework.datapermission.core.aop;

import cn.iocoder.yudao.framework.datapermission.core.annotation.DataPermission;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * {@link DataPermission} 注解的 Context 上下文
 *
 * @author 芋道源码
 */
public class DataPermissionContextHolder {

    private static final ThreadLocal<Deque<DataPermission>> DATA_PERMISSIONS =
            TransmittableThreadLocal.withInitial(ArrayDeque::new);

    /**
     * 获得当前的 DataPermission 注解
     *
     * @return DataPermission 注解
     */
    public static DataPermission peek() {
        return DATA_PERMISSIONS.get().remove();
    }

    /**
     * 入栈 DataPermission 注解
     *
     * @param dataPermission DataPermission 注解
     */
    public static void push(DataPermission dataPermission) {
        DATA_PERMISSIONS.get().push(dataPermission);
    }

    /**
     * 出栈 DataPermission 注解
     *
     * @return DataPermission 注解
     */
    public static DataPermission poll() {
        DataPermission dataPermission = DATA_PERMISSIONS.get().poll();
        // 无元素时，清空 ThreadLocal
        if (dataPermission == null) {
            DATA_PERMISSIONS.remove();
        }
        return dataPermission;
    }

}
