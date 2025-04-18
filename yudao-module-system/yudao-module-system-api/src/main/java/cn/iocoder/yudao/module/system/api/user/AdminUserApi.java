package cn.iocoder.yudao.module.system.api.user;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Admin 用户 API 接口
 *
 * @author 芋道源码
 */
public interface AdminUserApi {

    public static AdminUserApi inst() {
        return SpringUtils.getBean(AdminUserApi.class);
    }
    /**
     * 通过用户 ID 查询用户
     *
     * @param id 用户ID
     * @return 用户对象信息
     */
    AdminUserRespDTO getUser(Long id);

    /**
     * 通过用户 ID 查询用户下属
     *
     * @param id 用户编号
     * @return 用户下属用户列表
     */
    List<AdminUserRespDTO> getUserListBySubordinate(Long id);

    /**
     * 通过用户 ID 查询用户们
     *
     * @param ids 用户 ID 们
     * @return 用户对象信息
     */
    List<AdminUserRespDTO> getUserList(Collection<Long> ids);

    /**
     * 获得指定部门的用户数组
     *
     * @param deptIds 部门数组
     * @return 用户数组
     */
    List<AdminUserRespDTO> getUserListByDeptIds(Collection<Long> deptIds);

    /**
     * 获得指定岗位的用户数组
     *
     * @param postIds 岗位数组
     * @return 用户数组
     */
    List<AdminUserRespDTO> getUserListByPostIds(Collection<Long> postIds);

    /**
     * 获得用户 Map
     *
     * @param ids 用户编号数组
     * @return 用户 Map
     */
    default Map<Long, AdminUserRespDTO> getUserMap(Collection<Long> ids) {
        List<AdminUserRespDTO> users = getUserList(ids);
        return CollectionUtils.convertMap(users, AdminUserRespDTO::getId);
    }

    /**
     * 校验用户是否有效。如下情况，视为无效：
     * 1. 用户编号不存在
     * 2. 用户被禁用
     *
     * @param id 用户编号
     */
    default void validateUser(Long id) {
        validateUserList(Collections.singleton(id));
    }

    /**
     * 校验用户们是否有效。如下情况，视为无效：
     * 1. 用户编号不存在
     * 2. 用户被禁用
     *
     * @param ids 用户编号数组
     */
    void validateUserList(Collection<Long> ids);

    /**
    * @Author Wqh
    * @Description 修改用户
    * @Date 14:52 2024/11/4
    * @Param [erpUser]
     **/
    void updateUser(AdminUserReqDTO erpUser);

    /**
    * @Author Wqh
    * @Description 新增用户
    * @Date 14:52 2024/11/4
    * @Param [erpUser]
    * @return java.lang.Long
    **/
    Long createUser(AdminUserReqDTO erpUser);

    /**
    * @Author Wqh
    * @Description 获取以相同用户名开头的用户数量
    * @Date 14:52 2024/11/4
    * @Param [username]
    * @return java.lang.Integer
    **/
    Integer getUsernameIndex(String username);


    public static class OperatorBuilder<T> {

        private Collection<T> collection;
        private List<Function<T,?>> getters = new ArrayList<>();
        private List<BiConsumer<T,String>> setters = new ArrayList<>();
        private List<Function<AdminUserRespDTO,String>> propertyGetters = new ArrayList<>();
        private AdminUserApi userApi;
        private Function<AdminUserRespDTO,String> defaultPropertyGetter;

        public OperatorBuilder(AdminUserApi userApi,Collection<T> collection,Function<AdminUserRespDTO,String> defaultPropertyGetter) {
            this.collection = collection;
            this.userApi = userApi;
            this.defaultPropertyGetter = defaultPropertyGetter;
            if(this.defaultPropertyGetter==null) {
                this.defaultPropertyGetter = AdminUserRespDTO::getNickname;
            }
        }

        /**
         * 设置填充关系
         **/
        public OperatorBuilder<T> mapping(Function<T,?> getter, BiConsumer<T,String> setter) {
            return this.mapping(getter,setter,null);
        }

        /**
         * 设置填充关系
         **/
        public OperatorBuilder<T> mapping(Function<T,?> getter, BiConsumer<T,String> setter,Function<AdminUserRespDTO,String> propertyGetter) {
            this.getters.add(getter);
            this.setters.add(setter);
            propertyGetters.add(propertyGetter);
            return this;
        }

        /**
         * 执行填充
         **/
        public void fill() {

            Set<Long> userIds=new HashSet<>();
            for (T t : collection) {
                for (int i = 0; i < getters.size(); i++) {
                    Function<T, ?> getter = getters.get(i);
                    Object userId = getter.apply(t);
                    userIds.add(NumberUtils.parseLong(userId));
                }
            }

            Map<Long, AdminUserRespDTO> userMap=userApi.getUserMap(userIds);
            for (T t : collection) {
                for (int i = 0; i < getters.size(); i++) {
                    Function<T,?> getter = getters.get(i);
                    BiConsumer<T,String> setter = setters.get(i);
                    Long userId = NumberUtils.parseLong(getter.apply(t));
                    AdminUserRespDTO user = userMap.get(userId);
                    if(user!=null) {
                        Function<AdminUserRespDTO,String> propertyGetter = propertyGetters.get(i);
                        if(propertyGetter==null) {
                            propertyGetter= this.defaultPropertyGetter;
                        }
                        String propValue=propertyGetter.apply(user);
                        setter.accept(t,propValue);
                    }
                }
            }
        }
    }

    /**
     * 准备数据填充
     **/
    default <T> OperatorBuilder<T> prepareFill(Collection<T> list) {
        return new OperatorBuilder<>(this,list,null);
    }

    /**
     * 准备数据填充
     **/
    default <T> OperatorBuilder<T> prepareFill(Collection<T> list, Function<AdminUserRespDTO,String> propertyGetter) {
        return new OperatorBuilder<>(this,list,propertyGetter);
    }

}
