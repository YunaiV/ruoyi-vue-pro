//package cn.iocoder.yudao.framework.mybatis.core.physical;
//
//import com.baomidou.mybatisplus.core.enums.SqlMethod;
//import com.baomidou.mybatisplus.core.injector.AbstractMethod;
//import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
//import com.baomidou.mybatisplus.core.metadata.TableInfo;
//import org.apache.ibatis.mapping.MappedStatement;
//import org.apache.ibatis.mapping.SqlSource;
//
//import java.util.List;
//
/// **
// * @author: LeeFJ
// * @date: 2025/4/24 9:46
// * @description: 实现数据的物理删除
// */
//public class AbsoluteDeleteMpSqlInjector extends DefaultSqlInjector {
//
//    public static final String DELETE_ABSOLUTE_BY_ID = "deleteAbsoluteById";
//
//    @Override
//    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
//        List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
//        methodList.add(new DeleteAbsoluteById(DELETE_ABSOLUTE_BY_ID));
//        return methodList;
//    }
//
//    private static class DeleteAbsoluteById extends AbstractMethod {
//
//        protected DeleteAbsoluteById(String methodName) {
//            super(methodName);
//        }
//
//        @Override
//        public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
//            SqlMethod sqlMethod = SqlMethod.DELETE_BY_ID;
//            String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), tableInfo.getKeyColumn(), tableInfo.getKeyProperty());
//            SqlSource sqlSource = this.languageDriver.createSqlSource(this.configuration, sql, Object.class);
//            return this.addDeleteMappedStatement(mapperClass,this.methodName, sqlSource);
//        }
//
//    }
//}
