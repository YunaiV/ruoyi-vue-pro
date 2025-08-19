# CommonResult 通用返回详解 - 小白也能懂

## 📚 参考文档

- **官方文档**：[芋道框架 - 统一响应](https://doc.iocoder.cn/exception/#_1-%E7%BB%9F%E4%B8%80%E5%93%8D%E5%BA%94)
- **异常处理**：[芋道框架 - 异常处理](https://doc.iocoder.cn/exception/)

> 本文档基于芋道框架实际代码，用小白也能懂的方式详解 CommonResult 的设计和使用。



## 🎯 什么是 CommonResult？

**CommonResult** 就像是一个 "标准信封"，用来包装所有 API 接口的返回结果。

### 🤔 为什么需要统一返回格式？

想象一下，如果没有统一格式：

```java
// ❌ 混乱的返回格式
@GetMapping("/user")
public User getUser() { return user; }           // 直接返回对象

@GetMapping("/list") 
public List<User> getList() { return list; }     // 返回列表

@GetMapping("/error")
public String error() { return "出错了"; }        // 返回字符串
```

前端开发者会很痛苦：
- 不知道请求是否成功
- 不知道错误原因
- 每个接口返回格式都不一样

### ✅ 有了 CommonResult 后

```java
// ✅ 统一的返回格式
@GetMapping("/user")
public CommonResult<User> getUser() {
    return success(user);  // 成功时返回
}

@GetMapping("/error") 
public CommonResult<String> error() {
    return error(500, "出错了");  // 失败时返回
}
```

前端收到的都是这样的格式：
```json
{
    "code": 0,           // 0=成功，其他=失败
    "msg": "操作成功",    // 提示信息
    "data": { ... }      // 实际数据
}
```

---

## 📖 CommonResult 源码详解

### 核心结构

```java
/**
 * 通用返回结果
 * 
 * @param <T> 数据泛型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)  // 空值不序列化
@Data
public class CommonResult<T> implements Serializable {

    /**
     * 错误码
     * 
     * 0-成功
     * > 0-失败，对应具体的错误码
     */
    @Schema(description = "错误码，0 表示成功", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer code;

    /**
     * 返回数据
     */
    @Schema(description = "返回数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private T data;

    /**
     * 错误提示，用户可阅读
     */
    @Schema(description = "错误提示，用户可阅读", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码，牛逼！")
    private String msg;
}
```

### 🔍 字段详解

#### 1. code（错误码）
```java
private Integer code;
```

**作用**：告诉前端请求是否成功
- `0`：成功
- `>0`：失败，具体错误码

**示例**：
```java
// 成功
{ "code": 0, "msg": "操作成功", "data": {...} }

// 失败
{ "code": 400, "msg": "参数错误", "data": null }
{ "code": 401, "msg": "未登录", "data": null }
{ "code": 403, "msg": "无权限", "data": null }
```

#### 2. data（返回数据）
```java
private T data;
```

**作用**：存放实际的业务数据
- 使用泛型 `<T>`，可以是任意类型
- 成功时包含数据，失败时通常为 `null`

**示例**：
```java
// 返回单个对象
CommonResult<User> result = success(user);
// { "code": 0, "data": {"id": 1, "name": "张三"} }

// 返回列表
CommonResult<List<User>> result = success(userList);
// { "code": 0, "data": [{"id": 1}, {"id": 2}] }

// 返回分页数据
CommonResult<PageResult<User>> result = success(pageResult);
// { "code": 0, "data": {"list": [...], "total": 100} }
```

#### 3. msg（提示信息）
```java
private String msg;
```

**作用**：给用户看的提示信息
- 成功时：如 "操作成功"、"保存成功"
- 失败时：如 "参数错误"、"用户不存在"

---

## 🛠️ 静态工厂方法详解

### 成功返回方法

#### 1. success() - 无数据成功
```java
public static <T> CommonResult<T> success() {
    CommonResult<T> result = new CommonResult<>();
    result.code = GlobalErrorCodeConstants.SUCCESS.getCode();  // 0
    result.msg = GlobalErrorCodeConstants.SUCCESS.getMsg();    // "操作成功"
    result.data = null;
    return result;
}
```

**使用场景**：删除、修改等不需要返回数据的操作
```java
@DeleteMapping("/delete")
public CommonResult<Boolean> deleteUser(@RequestParam Long id) {
    userService.deleteUser(id);
    return success();  // 只表示成功，不返回数据
}
```

#### 2. success(T data) - 带数据成功
```java
public static <T> CommonResult<T> success(T data) {
    CommonResult<T> result = new CommonResult<>();
    result.code = GlobalErrorCodeConstants.SUCCESS.getCode();  // 0
    result.msg = GlobalErrorCodeConstants.SUCCESS.getMsg();    // "操作成功"
    result.data = data;  // 设置返回数据
    return result;
}
```

**使用场景**：查询、创建等需要返回数据的操作
```java
@GetMapping("/get")
public CommonResult<User> getUser(@RequestParam Long id) {
    User user = userService.getUser(id);
    return success(user);  // 返回用户数据
}

@PostMapping("/create")
public CommonResult<Long> createUser(@RequestBody UserCreateReqVO reqVO) {
    Long userId = userService.createUser(reqVO);
    return success(userId);  // 返回新创建的用户 ID
}
```

### 失败返回方法

#### 1. error(Integer code, String message) - 自定义错误
```java
public static <T> CommonResult<T> error(Integer code, String message) {
    Assert.isTrue(!GlobalErrorCodeConstants.SUCCESS.getCode().equals(code), 
                  "code 必须是错误的！");
    CommonResult<T> result = new CommonResult<>();
    result.code = code;
    result.msg = message;
    result.data = null;
    return result;
}
```

**使用场景**：自定义错误码和错误信息
```java
public CommonResult<User> getUser(Long id) {
    if (id == null) {
        return error(400, "用户ID不能为空");
    }
    
    User user = userService.getUser(id);
    if (user == null) {
        return error(404, "用户不存在");
    }
    
    return success(user);
}
```

#### 2. error(ErrorCode errorCode) - 使用错误码对象
```java
public static <T> CommonResult<T> error(ErrorCode errorCode) {
    return error(errorCode.getCode(), errorCode.getMsg());
}
```

**使用场景**：使用预定义的错误码

ErrorCodeConstants.java文件中;
```java
// 预定义错误码
public static final ErrorCode USER_NOT_EXISTS = new ErrorCode(1002001001, "用户不存在");

// 使用
public CommonResult<User> getUser(Long id) {
    User user = userService.getUser(id);
    if (user == null) {
        return error(USER_NOT_EXISTS);  // 使用预定义错误码
    }
    return success(user);
}
```

---

## 🎯 实际使用示例

### 1. 用户管理 Controller

```java
@RestController
@RequestMapping("/system/user")
public class UserController {

    @Resource
    private AdminUserService userService;

    /**
     * 创建用户
     */
    @PostMapping("/create")
    public CommonResult<Long> createUser(@Valid @RequestBody UserSaveReqVO reqVO) {
        // 调用业务逻辑
        Long id = userService.createUser(reqVO);
        
        // 返回成功结果，包含新用户的 ID
        return success(id);
    }

    /**
     * 修改用户
     */
    @PutMapping("/update")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody UserSaveReqVO reqVO) {
        // 调用业务逻辑
        userService.updateUser(reqVO);
        
        // 返回成功结果，不需要返回数据
        return success(true);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete")
    public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) {
        // 调用业务逻辑
        userService.deleteUser(id);
        
        // 返回成功结果
        return success(true);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/get")
    public CommonResult<UserRespVO> getUser(@RequestParam("id") Long id) {
        // 调用业务逻辑
        AdminUserDO user = userService.getUser(id);
        
        // 转换为 VO 并返回
        UserRespVO userVO = UserConvert.INSTANCE.convert(user);
        return success(userVO);
    }

    /**
     * 获取用户分页列表
     */
    @GetMapping("/page")
    public CommonResult<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO pageReqVO) {
        // 调用业务逻辑获取分页数据
        PageResult<AdminUserDO> pageResult = userService.getUserPage(pageReqVO);
        
        // 转换为 VO 并返回
        PageResult<UserRespVO> voPageResult = UserConvert.INSTANCE.convertPage(pageResult);
        return success(voPageResult);
    }
}
```

### 2. 前端接收处理

#### JavaScript 处理
```javascript
// 调用 API
axios.post('/system/user/create', userData)
  .then(response => {
    const result = response.data;
    
    if (result.code === 0) {
      // 成功
      console.log('创建成功，用户ID:', result.data);
      this.$message.success(result.msg);
    } else {
      // 失败
      console.error('创建失败:', result.msg);
      this.$message.error(result.msg);
    }
  })
  .catch(error => {
    console.error('请求失败:', error);
  });
```

#### TypeScript 处理
```typescript
// 定义返回类型
interface ApiResponse<T> {
  code: number;
  msg: string;
  data: T;
}

// 调用 API
const createUser = async (userData: UserCreateReqVO): Promise<number> => {
  const response = await axios.post<ApiResponse<number>>('/system/user/create', userData);
  const result = response.data;
  
  if (result.code === 0) {
    return result.data;  // 返回用户 ID
  } else {
    throw new Error(result.msg);  // 抛出错误
  }
};

// 使用
try {
  const userId = await createUser(userData);
  console.log('创建成功，用户ID:', userId);
} catch (error) {
  console.error('创建失败:', error.message);
}
```

---

## ⚡ 全局异常处理

### 异常自动转换

芋道框架通过 `GlobalExceptionHandler` 自动将异常转换为 `CommonResult`：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(value = ServiceException.class)
    public CommonResult<?> serviceExceptionHandler(ServiceException ex) {
        log.info("[serviceExceptionHandler]", ex);
        return CommonResult.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public CommonResult<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        assert fieldError != null;
        return CommonResult.error(BAD_REQUEST.getCode(), 
                                 String.format("请求参数不正确:%s", fieldError.getDefaultMessage()));
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> defaultExceptionHandler(Exception ex) {
        log.error("[defaultExceptionHandler]", ex);
        return CommonResult.error(INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
    }
}
```

### 芋道框架中的实际异常处理

#### 1. 错误码定义（ErrorCodeConstants.java）

```java
/**
 * System 错误码枚举类
 * system 系统，使用 1-002-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 用户模块 1-002-003-000 ==========
    ErrorCode USER_USERNAME_EXISTS = new ErrorCode(1_002_003_000, "用户账号已经存在");
    ErrorCode USER_MOBILE_EXISTS = new ErrorCode(1_002_003_001, "手机号已经存在");
    ErrorCode USER_EMAIL_EXISTS = new ErrorCode(1_002_003_002, "邮箱已经存在");
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1_002_003_003, "用户不存在");
    ErrorCode USER_IS_DISABLE = new ErrorCode(1_002_003_006, "名字为【{}】的用户已被禁用");
    ErrorCode USER_COUNT_MAX = new ErrorCode(1_002_003_008, "创建用户失败，原因：超过租户最大租户配额({})！");
}
```

#### 2. Service 层异常处理（AdminUserServiceImpl.java 真实代码）

```java
@Service
public class AdminUserServiceImpl extends ServiceExceptionUtil implements AdminUserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public Long createUser(UserSaveReqVO createReqVO) {
        // 1.1 校验账户配额
        tenantService.handleTenantInfo(tenant -> {
            long count = userMapper.selectCount();
            if (count >= tenant.getAccountCount()) {
                // 使用 exception() 方法抛出业务异常，支持参数格式化
                throw exception(USER_COUNT_MAX, tenant.getAccountCount());
            }
        });

        // 1.2 校验数据正确性
        validateUserForCreateOrUpdate(null, createReqVO.getUsername(),
                createReqVO.getMobile(), createReqVO.getEmail(),
                createReqVO.getDeptId(), createReqVO.getPostIds());

        // 2. 插入用户
        AdminUserDO user = BeanUtils.toBean(createReqVO, AdminUserDO.class);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus());
        user.setPassword(encodePassword(createReqVO.getPassword()));
        userMapper.insert(user);

        return user.getId();
    }

    /**
     * 校验用户是否存在
     */
    @VisibleForTesting
    AdminUserDO validateUserExists(Long id) {
        if (id == null) {
            return null;
        }
        AdminUserDO user = userMapper.selectById(id);
        if (user == null) {
            // 直接使用 exception() 方法，继承自 ServiceExceptionUtil
            throw exception(USER_NOT_EXISTS);
        }
        return user;
    }

    /**
     * 校验用户名唯一性
     */
    @VisibleForTesting
    void validateUsernameUnique(Long id, String username) {
        if (StrUtil.isBlank(username)) {
            return;
        }
        AdminUserDO user = userMapper.selectByUsername(username);
        if (user == null) {
            return;
        }
        // 如果 id 为空，说明是新增，直接抛异常
        if (id == null) {
            throw exception(USER_USERNAME_EXISTS);
        }
        // 如果不是同一个用户，说明用户名重复
        if (!user.getId().equals(id)) {
            throw exception(USER_USERNAME_EXISTS);
        }
    }

    /**
     * 校验用户列表
     */
    @Override
    public void validateUserList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<AdminUserDO> users = userMapper.selectByIds(ids);
        Map<Long, AdminUserDO> userMap = CollectionUtils.convertMap(users, AdminUserDO::getId);

        // 逐个校验
        ids.forEach(id -> {
            AdminUserDO user = userMap.get(id);
            if (user == null) {
                throw exception(USER_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(user.getStatus())) {
                // 支持参数格式化，{} 会被替换为用户昵称
                throw exception(USER_IS_DISABLE, user.getNickname());
            }
        });
    }
}
```

#### 3. exception() 方法详解（ServiceExceptionUtil.java）

```java
public class ServiceExceptionUtil {

    /**
     * 抛出业务异常（无参数）
     */
    public static ServiceException exception(ErrorCode errorCode) {
        return exception0(errorCode.getCode(), errorCode.getMsg());
    }

    /**
     * 抛出业务异常（带参数格式化）
     */
    public static ServiceException exception(ErrorCode errorCode, Object... params) {
        return exception0(errorCode.getCode(), errorCode.getMsg(), params);
    }

    /**
     * 格式化错误信息
     * 使用 {} 作为占位符，比 String.format 更安全
     */
    public static String doFormat(int code, String messagePattern, Object... params) {
        // 将 "用户【{}】已被禁用" + "张三" 格式化为 "用户【张三】已被禁用"
        // 实现逻辑...
    }
}
```

#### 4. 异常抛出的几种方式

```java
// 方式1：无参数异常
throw exception(USER_NOT_EXISTS);
// 结果：{"code": 1002003003, "msg": "用户不存在"}

// 方式2：带参数异常（推荐）
throw exception(USER_IS_DISABLE, user.getNickname());
// 结果：{"code": 1002003006, "msg": "名字为【张三】的用户已被禁用"}

// 方式3：带多个参数
throw exception(USER_COUNT_MAX, tenant.getAccountCount());
// 结果：{"code": 1002003008, "msg": "创建用户失败，原因：超过租户最大租户配额(100)！"}

// 方式4：自定义错误码和消息
throw exception0(400, "自定义错误信息");
// 结果：{"code": 400, "msg": "自定义错误信息"}
```

当抛出异常时，全局异常处理器会自动返回：
```json
{
    "code": 1002003003,
    "msg": "用户不存在",
    "data": null
}
```

---

## 💡 最佳实践

### 1. Controller 层规范

```java
// ✅ 好的写法
@PostMapping("/create")
public CommonResult<Long> createUser(@Valid @RequestBody UserSaveReqVO reqVO) {
    Long id = userService.createUser(reqVO);
    return success(id);  // 简洁明了
}

// ❌ 不好的写法
@PostMapping("/create")
public CommonResult<Long> createUser(@Valid @RequestBody UserSaveReqVO reqVO) {
    try {
        Long id = userService.createUser(reqVO);
        return success(id);
    } catch (Exception e) {
        return error(500, e.getMessage());  // 不要在 Controller 处理异常
    }
}
```

### 2. 错误码管理（芋道框架实际做法）

```java
// ✅ 集中管理错误码（ErrorCodeConstants.java）
public interface ErrorCodeConstants {
    // 用户模块错误码：1-002-003-000 段
    ErrorCode USER_USERNAME_EXISTS = new ErrorCode(1_002_003_000, "用户账号已经存在");
    ErrorCode USER_MOBILE_EXISTS = new ErrorCode(1_002_003_001, "手机号已经存在");
    ErrorCode USER_EMAIL_EXISTS = new ErrorCode(1_002_003_002, "邮箱已经存在");
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1_002_003_003, "用户不存在");
    ErrorCode USER_IS_DISABLE = new ErrorCode(1_002_003_006, "名字为【{}】的用户已被禁用");
}

// ✅ Service 继承 ServiceExceptionUtil，直接使用 exception() 方法
@Service
public class AdminUserServiceImpl extends ServiceExceptionUtil implements AdminUserService {

    // 使用方式1：无参数
    throw exception(USER_NOT_EXISTS);

    // 使用方式2：带参数格式化
    throw exception(USER_IS_DISABLE, user.getNickname());
}
```

### 3. 前端统一处理

```javascript
// axios 响应拦截器
axios.interceptors.response.use(
  response => {
    const result = response.data;
    
    // 统一处理业务错误
    if (result.code !== 0) {
      this.$message.error(result.msg);
      return Promise.reject(new Error(result.msg));
    }
    
    return result.data;  // 只返回业务数据
  },
  error => {
    console.error('请求失败:', error);
    return Promise.reject(error);
  }
);
```

---

## 🎉 总结

### ✅ CommonResult 的核心价值

1. **统一格式**：所有 API 返回格式一致，前端处理简单
2. **错误处理**：统一的错误码和错误信息管理
3. **类型安全**：使用泛型，支持任意数据类型
4. **自动转换**：异常自动转换为标准格式

### 🚀 芋道框架使用要点

1. **Controller 层**：始终返回 `CommonResult<T>`
2. **成功时**：使用 `success()` 或 `success(data)`
3. **失败时**：在 Service 层使用 `exception()` 方法抛异常
4. **Service 继承**：继承 `ServiceExceptionUtil` 类
5. **错误码管理**：在 `ErrorCodeConstants` 中集中定义
6. **前端**：统一检查 `code` 字段判断成功失败

### 💡 芋道框架异常处理公式

```
Service继承ServiceExceptionUtil → 使用exception()抛异常 → 全局异常处理器捕获 → 自动转换为CommonResult
```

### 🎯 实际开发步骤

1. **定义错误码**：在 `ErrorCodeConstants` 中定义业务错误码
2. **Service 继承**：让 Service 实现类继承 `ServiceExceptionUtil`
3. **抛出异常**：使用 `exception(ERROR_CODE)` 或 `exception(ERROR_CODE, params)` 抛异常
4. **Controller 返回**：Controller 只需要 `return success(data)`
5. **全局处理**：异常会被全局异常处理器自动转换为 `CommonResult`

现在你已经完全理解了芋道框架中 `CommonResult` 的实际使用方式，可以在项目中正确使用了！🎯
