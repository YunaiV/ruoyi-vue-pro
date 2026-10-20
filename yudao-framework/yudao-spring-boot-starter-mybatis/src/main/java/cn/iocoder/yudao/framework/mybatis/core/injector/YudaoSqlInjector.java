package cn.iocoder.yudao.framework.mybatis.core.injector;

import cn.iocoder.yudao.framework.mybatis.core.util.CaseInsensitiveResultMap;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.github.yulichang.injector.MPJSqlInjector;
import com.github.yulichang.method.mp.SelectMaps;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.util.List;

/**
 * 自定义 SQL 注入器
 *
 * 在 MyBatis Plus Join 的 {@link MPJSqlInjector} 基础上，将 selectMaps 的结果行替换为 {@link CaseInsensitiveResultMap}，
 * 解决 PostgreSQL、Oracle 等数据库列标签大小写折叠，导致按固定 key 取不到值的问题
 *
 * @author 芋道源码
 */
public class YudaoSqlInjector extends MPJSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration, Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methods = super.getMethodList(configuration, mapperClass, tableInfo);
        methods.replaceAll(method -> method instanceof SelectMaps ? new CaseInsensitiveSelectMaps() : method);
        return methods;
    }

    /**
     * 结果行为 {@link CaseInsensitiveResultMap} 的 selectMaps
     *
     * 仅修改结果类型，SQL 仍由 MyBatis Plus Join 的 {@link SelectMaps} 生成，兼容 MPJ 的 Wrapper
     */
    private static class CaseInsensitiveSelectMaps extends SelectMaps {

        @Override
        protected MappedStatement addSelectMappedStatementForOther(Class<?> mapperClass, String id, SqlSource sqlSource,
                                                                   Class<?> resultType) {
            return super.addSelectMappedStatementForOther(mapperClass, id, sqlSource, CaseInsensitiveResultMap.class);
        }

    }

}
