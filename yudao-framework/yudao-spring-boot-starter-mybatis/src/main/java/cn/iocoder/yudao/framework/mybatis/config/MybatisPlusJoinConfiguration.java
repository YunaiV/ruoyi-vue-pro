package cn.iocoder.yudao.framework.mybatis.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import icu.mhb.mybatisplus.plugln.injector.JoinDefaultSqlInjector;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// TODO @升平：是不是作为 YudaoMybatisAutoConfiguration 的 bean 即可呀？然后 getMethodList 不用重写
@Configuration
public class MybatisPlusJoinConfiguration extends JoinDefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        return super.getMethodList(mapperClass, tableInfo);
    }

}
