package cn.iocoder.dashboard.framework.mybatis.core.handle;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.framework.security.core.LoginUser;
import cn.iocoder.dashboard.framework.security.core.util.SecurityFrameworkUtils;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * 通用参数填充实现类
 *
 * 如果没有显式的对通用参数进行赋值，这里会对通用参数进行填充、赋值
 *
 * @author hexiaowu
 */
@Component
public class DefaultDBFieldHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (Objects.nonNull(metaObject) && metaObject.getOriginalObject() instanceof BaseDO) {
            LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
            BaseDO baseDO = (BaseDO) metaObject.getOriginalObject();
            Date current = new Date();

            // 创建时间为空，则以当前时间为插入时间
            if (Objects.isNull(baseDO.getCreateTime())) {
                baseDO.setCreateTime(current);
            }
            // 更新时间为空，则以当前时间为更新时间
            if (Objects.isNull(baseDO.getUpdateTime())) {
                baseDO.setUpdateTime(current);
            }
            // 当前登录用户不为空，创建人为空，则当前登录用户为创建人
            if (Objects.nonNull(loginUser) && Objects.isNull(baseDO.getCreator())) {
                baseDO.setCreator(loginUser.getId().toString());
            }
            // 当前登录用户不为空，更新人为空，则当前登录用户为更新人
            if (Objects.nonNull(loginUser) && Objects.isNull(baseDO.getUpdater())) {
                baseDO.setUpdater(loginUser.getId().toString());
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Object modifyTime = getFieldValByName("updateTime", metaObject);
        Object modifier = getFieldValByName("updater", metaObject);
        // 获取登录用户信息
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();

        // 更新时间为空，则以当前时间为更新时间
        if (Objects.isNull(modifyTime)) {
            setFieldValByName("updateTime", new Date(), metaObject);
        }
        // 当前登录用户不为空，更新人为空，则当前登录用户为更新人
        if (Objects.nonNull(loginUser) && Objects.isNull(modifier)) {
            setFieldValByName("updater", loginUser.getId(), metaObject);
        }
    }
}
