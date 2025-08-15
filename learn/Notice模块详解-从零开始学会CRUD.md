# Notice 模块详解 - 从零开始学会 CRUD

## 🎯 什么是 Notice 模块？

**Notice 模块** 就是一个"通知公告"功能，就像学校里的公告栏一样：
- 管理员可以发布通知
- 可以修改、删除通知
- 用户可以查看通知列表
- 支持实时推送通知

## 🏗️ 模块结构详解

```
notice 模块/
├── controller/          # 控制器层（接收请求）
│   ├── NoticeController.java
│   └── vo/             # 数据传输对象
├── service/            # 服务层（业务逻辑）
│   ├── NoticeService.java
│   └── NoticeServiceImpl.java
├── dal/                # 数据访问层
│   ├── dataobject/     # 数据库对象
│   │   └── NoticeDO.java
│   └── mysql/          # 数据库操作
│       └── NoticeMapper.java
└── enums/              # 枚举类
    └── NoticeTypeEnum.java
```

### 分层架构说明

想象一个餐厅：
- **Controller（服务员）**：接待客人，记录点餐
- **Service（厨师长）**：决定怎么做菜，协调整个流程
- **Mapper（厨师）**：具体执行，从冰箱取食材（数据库）

## 📊 数据库设计

### NoticeDO - 数据库对象

```java
@TableName("system_notice")  // 对应数据库表 system_notice
@KeySequence("system_notice_seq")  // 主键序列（某些数据库需要）
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeDO extends BaseDO {

    /**
     * 公告ID - 主键
     */
    private Long id;
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告类型
     * 枚举 {@link NoticeTypeEnum}
     */
    private Integer type;
    
    /**
     * 公告内容
     */
    private String content;
    
    /**
     * 公告状态
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    
    // 从 BaseDO 继承的字段：
    // createTime, updateTime, creator, updater, deleted
}
```

#### 🔍 注解详解

**@TableName("system_notice")**
- 告诉 MyBatis-Plus 这个类对应哪个数据库表
- 如果类名和表名不一样，必须用这个注解

**@KeySequence("system_notice_seq")**
- 用于 Oracle、PostgreSQL 等数据库的主键自增
- MySQL 不需要这个注解

**extends BaseDO**
- 继承基础数据对象
- 自动包含：创建时间、修改时间、创建者、修改者、删除标记

### 枚举类设计

```java
@Getter
@AllArgsConstructor
public enum NoticeTypeEnum {

    NOTICE(1),        // 通知
    ANNOUNCEMENT(2);  // 公告

    /**
     * 类型值
     */
    private final Integer type;
}
```

**为什么用枚举？**
- 限制取值范围，避免错误数据
- 代码更清晰，一看就知道有哪些类型
- 便于维护，新增类型只需要加一行

## 🎮 Controller 层详解

### 完整的 Controller 代码

```java
@Tag(name = "管理后台 - 通知公告")  // Swagger 文档标签
@RestController                    // REST 控制器
@RequestMapping("/system/notice")  // 基础路径
@Validated                        // 开启参数校验
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @Resource
    private WebSocketSenderApi webSocketSenderApi;
}
```

#### 🔍 类注解详解

**@Tag(name = "管理后台 - 通知公告")**
- Swagger 文档分组标签
- 在 API 文档中会显示这个名称

**@RestController**
- 等于 @Controller + @ResponseBody
- 所有方法返回的数据都会转成 JSON

**@RequestMapping("/system/notice")**
- 设置这个类所有方法的基础路径
- 比如 createNotice 方法的完整路径是：/system/notice/create

**@Validated**
- 开启方法参数校验
- 配合 @Valid 使用

### 1. 创建通知

```java
@PostMapping("/create")
@Operation(summary = "创建通知公告")
@PreAuthorize("@ss.hasPermission('system:notice:create')")
public CommonResult<Long> createNotice(@Valid @RequestBody NoticeSaveReqVO createReqVO) {
    Long noticeId = noticeService.createNotice(createReqVO);
    return success(noticeId);
}
```

#### 🔍 方法注解详解

**@PostMapping("/create")**
- 处理 POST 请求
- 完整路径：POST /system/notice/create

**@Operation(summary = "创建通知公告")**
- Swagger 文档中的方法描述

**@PreAuthorize("@ss.hasPermission('system:notice:create')")**
- 权限校验，只有有权限的用户才能访问
- `@ss` 是 Spring Security 的简写
- `system:notice:create` 是权限码

**@Valid @RequestBody NoticeSaveReqVO createReqVO**
- `@Valid`：校验请求参数
- `@RequestBody`：从 HTTP 请求体中读取 JSON 数据
- 自动转换成 NoticeSaveReqVO 对象

#### 🔄 执行流程

```
1. 前端发送 POST 请求 + JSON 数据
2. Spring 检查权限（@PreAuthorize）
3. 将 JSON 转换成 NoticeSaveReqVO 对象
4. 校验参数（@Valid）
5. 调用 noticeService.createNotice()
6. 返回新创建的通知 ID
```

### 2. 修改通知

```java
@PutMapping("/update")
@Operation(summary = "修改通知公告")
@PreAuthorize("@ss.hasPermission('system:notice:update')")
public CommonResult<Boolean> updateNotice(@Valid @RequestBody NoticeSaveReqVO updateReqVO) {
    noticeService.updateNotice(updateReqVO);
    return success(true);
}
```

**为什么创建和修改用同一个 VO？**
- 字段基本相同，只是修改时会传 ID
- 减少重复代码
- 便于维护

### 3. 删除通知

```java
@DeleteMapping("/delete")
@Operation(summary = "删除通知公告")
@Parameter(name = "id", description = "编号", required = true, example = "1024")
@PreAuthorize("@ss.hasPermission('system:notice:delete')")
public CommonResult<Boolean> deleteNotice(@RequestParam("id") Long id) {
    noticeService.deleteNotice(id);
    return success(true);
}
```

#### 🔍 新注解详解

**@DeleteMapping("/delete")**
- 处理 DELETE 请求

**@Parameter(name = "id", description = "编号", required = true, example = "1024")**
- Swagger 文档中的参数说明
- `name`：参数名
- `description`：参数描述
- `required`：是否必填
- `example`：示例值

**@RequestParam("id") Long id**
- 从 URL 参数中获取 id 值
- 比如：DELETE /system/notice/delete?id=1024

### 4. 批量删除

```java
@DeleteMapping("/delete-list")
@Operation(summary = "批量删除通知公告")
@Parameter(name = "ids", description = "编号列表", required = true)
@PreAuthorize("@ss.hasPermission('system:notice:delete')")
public CommonResult<Boolean> deleteNoticeList(@RequestParam("ids") List<Long> ids) {
    noticeService.deleteNoticeList(ids);
    return success(true);
}
```

**@RequestParam("ids") List<Long> ids**
- 接收多个 ID
- 前端传递：?ids=1,2,3,4
- Spring 自动转换成 List<Long>

### 5. 分页查询

```java
@GetMapping("/page")
@Operation(summary = "获取通知公告列表")
@PreAuthorize("@ss.hasPermission('system:notice:query')")
public CommonResult<PageResult<NoticeRespVO>> getNoticePage(@Validated NoticePageReqVO pageReqVO) {
    PageResult<NoticeDO> pageResult = noticeService.getNoticePage(pageReqVO);
    return success(BeanUtils.toBean(pageResult, NoticeRespVO.class));
}
```

#### 🔄 执行流程

```
1. 接收查询条件（NoticePageReqVO）
2. 调用 Service 查询数据库，得到 PageResult<NoticeDO>
3. 将 DO 转换成 VO：BeanUtils.toBean(pageResult, NoticeRespVO.class)
4. 返回给前端
```

### 6. 单个查询

```java
@GetMapping("/get")
@Operation(summary = "获得通知公告")
@Parameter(name = "id", description = "编号", required = true, example = "1024")
@PreAuthorize("@ss.hasPermission('system:notice:query')")
public CommonResult<NoticeRespVO> getNotice(@RequestParam("id") Long id) {
    NoticeDO notice = noticeService.getNotice(id);
    return success(BeanUtils.toBean(notice, NoticeRespVO.class));
}
```

### 7. WebSocket 推送

```java
@PostMapping("/push")
@Operation(summary = "推送通知公告", description = "只发送给 websocket 连接在线的用户")
@Parameter(name = "id", description = "编号", required = true, example = "1024")
@PreAuthorize("@ss.hasPermission('system:notice:update')")
public CommonResult<Boolean> push(@RequestParam("id") Long id) {
    NoticeDO notice = noticeService.getNotice(id);
    Assert.notNull(notice, "公告不能为空");
    // 通过 websocket 推送给在线的用户
    webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), "notice-push", notice);
    return success(true);
}
```

#### 🔍 新知识点

**Assert.notNull(notice, "公告不能为空")**
- 断言，如果 notice 为 null，抛出异常
- 相当于：if (notice == null) throw new Exception("公告不能为空")

**webSocketSenderApi.sendObject()**
- 通过 WebSocket 实时推送消息
- `UserTypeEnum.ADMIN.getValue()`：发送给管理员用户
- `"notice-push"`：消息类型
- `notice`：消息内容

## 🔧 Service 层详解

### Service 接口

```java
public interface NoticeService {

    /**
     * 创建通知公告
     */
    Long createNotice(NoticeSaveReqVO createReqVO);

    /**
     * 更新通知公告
     */
    void updateNotice(NoticeSaveReqVO reqVO);

    /**
     * 删除通知公告
     */
    void deleteNotice(Long id);

    /**
     * 批量删除通知公告
     */
    void deleteNoticeList(List<Long> ids);

    /**
     * 获得通知公告分页列表
     */
    PageResult<NoticeDO> getNoticePage(NoticePageReqVO reqVO);

    /**
     * 获得通知公告
     */
    NoticeDO getNotice(Long id);
}
```

### Service 实现类

```java
@Service
public class NoticeServiceImpl implements NoticeService {

    @Resource
    private NoticeMapper noticeMapper;

    @Override
    public Long createNotice(NoticeSaveReqVO createReqVO) {
        // 1. 将 VO 转换成 DO
        NoticeDO notice = BeanUtils.toBean(createReqVO, NoticeDO.class);
        
        // 2. 插入数据库
        noticeMapper.insert(notice);
        
        // 3. 返回自动生成的 ID
        return notice.getId();
    }

    @Override
    public void updateNotice(NoticeSaveReqVO updateReqVO) {
        // 1. 校验通知是否存在
        validateNoticeExists(updateReqVO.getId());
        
        // 2. 将 VO 转换成 DO
        NoticeDO updateObj = BeanUtils.toBean(updateReqVO, NoticeDO.class);
        
        // 3. 更新数据库
        noticeMapper.updateById(updateObj);
    }

    @Override
    public void deleteNotice(Long id) {
        // 1. 校验通知是否存在
        validateNoticeExists(id);
        
        // 2. 删除（逻辑删除，不是真删除）
        noticeMapper.deleteById(id);
    }

    @Override
    public void deleteNoticeList(List<Long> ids) {
        // 批量删除，不校验是否存在（提高性能）
        noticeMapper.deleteByIds(ids);
    }

    @Override
    public PageResult<NoticeDO> getNoticePage(NoticePageReqVO reqVO) {
        // 直接调用 Mapper 的分页查询方法
        return noticeMapper.selectPage(reqVO);
    }

    @Override
    public NoticeDO getNotice(Long id) {
        // 根据 ID 查询单个记录
        return noticeMapper.selectById(id);
    }

    /**
     * 校验通知是否存在
     */
    @VisibleForTesting  // 标记为测试可见
    public void validateNoticeExists(Long id) {
        if (id == null) {
            return;  // ID 为空，不校验
        }
        NoticeDO notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw exception(NOTICE_NOT_FOUND);  // 抛出业务异常
        }
    }
}
```

#### 🔍 关键知识点

**@Service**
- 标记这是一个服务类
- Spring 会自动创建这个类的实例

**@Resource**
- 自动注入依赖
- 相当于：noticeMapper = Spring容器.getBean(NoticeMapper.class)

**BeanUtils.toBean()**
- 对象属性复制工具
- 把 VO 的属性复制到 DO 中

**validateNoticeExists()**
- 业务校验方法
- 确保要操作的数据存在

**exception(NOTICE_NOT_FOUND)**
- 抛出业务异常
- NOTICE_NOT_FOUND 是错误码常量

## 💾 Mapper 层详解

```java
@Mapper
public interface NoticeMapper extends BaseMapperX<NoticeDO> {

    default PageResult<NoticeDO> selectPage(NoticePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<NoticeDO>()
                .likeIfPresent(NoticeDO::getTitle, reqVO.getTitle())      // 标题模糊查询
                .eqIfPresent(NoticeDO::getStatus, reqVO.getStatus())      // 状态精确查询
                .orderByDesc(NoticeDO::getId));                           // 按ID降序排列
    }
}
```

#### 🔍 关键知识点

**@Mapper**
- 标记这是 MyBatis 的 Mapper 接口

**extends BaseMapperX<NoticeDO>**
- 继承基础 Mapper，自动获得 CRUD 方法
- insert()、updateById()、deleteById()、selectById() 等

**LambdaQueryWrapperX**
- 查询条件构造器
- 使用 Lambda 表达式，避免写错字段名

**likeIfPresent()**
- 如果参数不为空，则添加 LIKE 查询条件
- 相当于：WHERE title LIKE '%参数值%'

**eqIfPresent()**
- 如果参数不为空，则添加等于查询条件
- 相当于：WHERE status = 参数值

**orderByDesc()**
- 降序排列
- 相当于：ORDER BY id DESC

## 🔄 完整的数据流转

### 创建通知的完整流程

```
1. 前端发送请求
   POST /system/notice/create
   Body: {"title":"系统维护","type":1,"content":"今晚维护","status":1}

2. Controller 接收
   @PostMapping("/create")
   createNotice(@Valid @RequestBody NoticeSaveReqVO createReqVO)

3. 参数校验
   @NotBlank, @NotNull 等注解自动校验

4. 调用 Service
   noticeService.createNotice(createReqVO)

5. VO 转 DO
   NoticeDO notice = BeanUtils.toBean(createReqVO, NoticeDO.class)

6. 保存数据库
   noticeMapper.insert(notice)

7. 返回结果
   return success(notice.getId())

8. 前端收到响应
   {"code":0,"msg":"操作成功","data":1024}
```

## 🎯 总结

### Notice 模块的核心功能

1. **CRUD 操作**：创建、查询、修改、删除
2. **分页查询**：支持条件查询和分页
3. **批量操作**：批量删除
4. **实时推送**：WebSocket 推送通知
5. **权限控制**：每个操作都有权限校验

### 学到的关键技术

1. **分层架构**：Controller → Service → Mapper
2. **对象转换**：VO ↔ DO 的转换
3. **参数校验**：@Valid + 校验注解
4. **权限控制**：@PreAuthorize
5. **API 文档**：Swagger 注解
6. **数据库操作**：MyBatis-Plus

### 新手建议

1. **理解分层**：每一层都有自己的职责
2. **看懂注解**：每个注解都有具体作用
3. **跟踪数据流**：数据是怎么从前端到数据库的
4. **多看日志**：出错时看日志找问题
5. **动手实践**：照着这个模式写其他模块

## 🧪 实际测试示例

### 使用 Postman 测试

#### 1. 创建通知

**Postman 测试：**
```http
POST http://localhost:8080/admin-api/system/notice/create
Content-Type: application/json
Authorization: Bearer your-token-here

{
    "title": "系统维护通知",
    "type": 1,
    "content": "系统将于今晚22:00-24:00进行维护，请提前保存工作。",
    "status": 1
}
```

**前端 TypeScript 代码：**
```typescript
// 在 Vue 组件中创建通知
import * as NoticeApi from '@/api/system/notice'

const createNotice = async () => {
  const noticeData: NoticeApi.NoticeVO = {
    id: undefined,  // 新增时 ID 为 undefined
    title: "系统维护通知",
    type: 1,        // 1=通知，2=公告
    content: "系统将于今晚22:00-24:00进行维护，请提前保存工作。",
    status: 1,      // 1=启用，0=禁用
    remark: "重要通知",
    creator: "",    // 后端自动填充
    createTime: new Date()  // 后端自动填充
  }

  try {
    const id = await NoticeApi.createNotice(noticeData)
    console.log('创建成功，通知ID:', id)
    // 刷新列表或其他操作
  } catch (error) {
    console.error('创建失败:', error)
  }
}
```

**预期响应：**
```json
{
    "code": 0,
    "msg": "操作成功",
    "data": 1024
}
```

#### 2. 查询通知列表

**Postman 测试：**
```http
GET http://localhost:8080/admin-api/system/notice/page?pageNo=1&pageSize=10&title=维护&status=1
Authorization: Bearer your-token-here
```

**前端 TypeScript 代码：**
```typescript
// 定义查询参数类型
interface NoticePageReqVO {
  pageNo: number
  pageSize: number
  title?: string
  status?: number
}

// 查询通知列表
const getNoticeList = async () => {
  const params: NoticePageReqVO = {
    pageNo: 1,
    pageSize: 10,
    title: "维护",  // 可选的搜索条件
    status: 1       // 可选的状态筛选
  }

  try {
    const result = await NoticeApi.getNoticePage(params)
    console.log('查询结果:', result)
    // result.list 是 NoticeVO[] 类型
    // result.total 是总数
  } catch (error) {
    console.error('查询失败:', error)
  }
}
```

**预期响应：**
```json
{
    "code": 0,
    "msg": "操作成功",
    "data": {
        "list": [
            {
                "id": 1024,
                "title": "系统维护通知",
                "type": 1,
                "content": "系统将于今晚22:00-24:00进行维护...",
                "status": 1,
                "createTime": "2024-01-15T10:30:00"
            }
        ],
        "total": 1
    }
}
```

#### 3. 修改通知

```http
PUT http://localhost:8080/admin-api/system/notice/update
Content-Type: application/json
Authorization: Bearer your-token-here

{
    "id": 1024,
    "title": "系统维护通知（已修改）",
    "type": 1,
    "content": "维护时间调整为今晚23:00-01:00",
    "status": 1
}
```

#### 4. 删除通知

```http
DELETE http://localhost:8080/admin-api/system/notice/delete?id=1024
Authorization: Bearer your-token-here
```

#### 5. 推送通知

```http
POST http://localhost:8080/admin-api/system/notice/push?id=1024
Authorization: Bearer your-token-here
```

## 🐛 常见错误和解决方案

### 1. 参数校验失败

**错误信息：**
```json
{
    "code": 400,
    "msg": "公告标题不能为空"
}
```

**原因：** 前端没有传递 title 字段或者传递了空字符串

**解决：** 检查前端请求数据，确保必填字段有值

### 2. 权限不足

**错误信息：**
```json
{
    "code": 403,
    "msg": "Access Denied"
}
```

**原因：** 当前用户没有对应的权限

**解决：**
1. 检查用户是否登录
2. 检查用户是否有 `system:notice:create` 等权限
3. 联系管理员分配权限

### 3. 数据不存在

**错误信息：**
```json
{
    "code": 1002001001,
    "msg": "通知公告不存在"
}
```

**原因：** 要修改或删除的通知不存在

**解决：** 检查传递的 ID 是否正确

### 4. 数据库连接失败

**错误信息：**
```
Could not open JDBC Connection for transaction
```

**原因：** 数据库连接配置错误或数据库服务未启动

**解决：**
1. 检查 application.yml 中的数据库配置
2. 确认数据库服务是否启动
3. 检查数据库用户名密码是否正确

## 🔧 如何扩展 Notice 模块

### 1. 添加新字段

假设要添加"发布时间"字段：

**1. 修改数据库表**
```sql
ALTER TABLE system_notice ADD COLUMN publish_time datetime COMMENT '发布时间';
```

**2. 修改 NoticeDO**
```java
/**
 * 发布时间
 */
private LocalDateTime publishTime;
```

**3. 修改 VO**
```java
// NoticeSaveReqVO 中添加
@Schema(description = "发布时间", example = "2024-01-15T10:30:00")
private LocalDateTime publishTime;

// NoticeRespVO 中添加
@Schema(description = "发布时间", example = "2024-01-15T10:30:00")
private LocalDateTime publishTime;
```

### 2. 添加新的查询条件

假设要按发布时间范围查询：

**修改 NoticePageReqVO**
```java
@Schema(description = "发布开始时间")
private LocalDateTime publishTimeStart;

@Schema(description = "发布结束时间")
private LocalDateTime publishTimeEnd;
```

**修改 NoticeMapper**
```java
default PageResult<NoticeDO> selectPage(NoticePageReqVO reqVO) {
    return selectPage(reqVO, new LambdaQueryWrapperX<NoticeDO>()
            .likeIfPresent(NoticeDO::getTitle, reqVO.getTitle())
            .eqIfPresent(NoticeDO::getStatus, reqVO.getStatus())
            .betweenIfPresent(NoticeDO::getPublishTime, reqVO.getPublishTimeStart(), reqVO.getPublishTimeEnd())
            .orderByDesc(NoticeDO::getId));
}
```

### 3. 添加新的业务方法

假设要添加"发布通知"功能：

**1. 在 NoticeService 中添加方法**
```java
/**
 * 发布通知
 */
void publishNotice(Long id);
```

**2. 在 NoticeServiceImpl 中实现**
```java
@Override
public void publishNotice(Long id) {
    // 1. 校验通知是否存在
    NoticeDO notice = validateNoticeExists(id);

    // 2. 检查状态是否允许发布
    if (CommonStatusEnum.ENABLE.getStatus().equals(notice.getStatus())) {
        throw exception(NOTICE_ALREADY_PUBLISHED);
    }

    // 3. 更新发布时间和状态
    NoticeDO updateObj = new NoticeDO();
    updateObj.setId(id);
    updateObj.setPublishTime(LocalDateTime.now());
    updateObj.setStatus(CommonStatusEnum.ENABLE.getStatus());

    noticeMapper.updateById(updateObj);

    // 4. 自动推送给在线用户
    webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), "notice-publish", notice);
}
```

**3. 在 NoticeController 中添加接口**
```java
@PostMapping("/publish")
@Operation(summary = "发布通知公告")
@Parameter(name = "id", description = "编号", required = true, example = "1024")
@PreAuthorize("@ss.hasPermission('system:notice:update')")
public CommonResult<Boolean> publishNotice(@RequestParam("id") Long id) {
    noticeService.publishNotice(id);
    return success(true);
}
```

## 📚 相关知识点扩展

### 1. MyBatis-Plus 常用方法

```java
// 基础 CRUD
noticeMapper.insert(notice);           // 插入
noticeMapper.updateById(notice);       // 根据ID更新
noticeMapper.deleteById(id);           // 根据ID删除
noticeMapper.selectById(id);           // 根据ID查询
noticeMapper.selectList(wrapper);      // 条件查询列表
noticeMapper.selectPage(page, wrapper); // 分页查询

// 批量操作
noticeMapper.insertBatch(noticeList);  // 批量插入
noticeMapper.deleteByIds(ids);         // 批量删除
```

### 2. 查询条件构造器

```java
LambdaQueryWrapperX<NoticeDO> wrapper = new LambdaQueryWrapperX<NoticeDO>()
    .eq(NoticeDO::getStatus, 1)                    // 等于
    .ne(NoticeDO::getStatus, 0)                    // 不等于
    .like(NoticeDO::getTitle, "维护")               // 模糊查询
    .likeLeft(NoticeDO::getTitle, "系统")           // 左模糊
    .likeRight(NoticeDO::getTitle, "通知")          // 右模糊
    .gt(NoticeDO::getId, 100)                      // 大于
    .ge(NoticeDO::getId, 100)                      // 大于等于
    .lt(NoticeDO::getId, 1000)                     // 小于
    .le(NoticeDO::getId, 1000)                     // 小于等于
    .between(NoticeDO::getId, 100, 1000)           // 区间查询
    .in(NoticeDO::getType, Arrays.asList(1, 2))    // IN 查询
    .isNull(NoticeDO::getDeletedTime)              // 为空
    .isNotNull(NoticeDO::getCreateTime)            // 不为空
    .orderByAsc(NoticeDO::getId)                   // 升序
    .orderByDesc(NoticeDO::getCreateTime);         // 降序
```

### 3. 事务管理

```java
@Service
@Transactional  // 类级别事务
public class NoticeServiceImpl implements NoticeService {

    @Override
    @Transactional(rollbackFor = Exception.class)  // 方法级别事务
    public void batchCreateNotice(List<NoticeSaveReqVO> createReqVOList) {
        for (NoticeSaveReqVO reqVO : createReqVOList) {
            createNotice(reqVO);
        }
        // 如果任何一个创建失败，整个批次都会回滚
    }
}
```

## 🎯 学习建议

### 对于纯新手

1. **先理解概念**
   - 什么是分层架构
   - 什么是 VO、DO
   - 什么是 CRUD

2. **跟着代码走一遍**
   - 从 Controller 开始
   - 看每个注解的作用
   - 理解数据是怎么流转的

3. **动手实践**
   - 复制这个模块，改成其他功能（比如用户管理）
   - 尝试添加新字段
   - 尝试添加新功能

4. **遇到问题怎么办**
   - 看日志，理解错误信息
   - 检查参数是否正确
   - 检查权限是否足够
   - 问有经验的同事

### 进阶学习

1. **深入理解框架**
   - Spring Boot 自动配置原理
   - MyBatis-Plus 插件机制
   - Spring Security 权限控制

2. **性能优化**
   - 数据库索引优化
   - 查询条件优化
   - 缓存使用

3. **扩展功能**
   - 添加审计日志
   - 添加数据权限
   - 添加多租户支持

记住：**这就是一个标准的 CRUD 模块，掌握了这个，其他模块都是类似的！**

## 🎯 TypeScript 实战：完整的 Notice 组件

### 前端完整示例（基于芋道项目）

#### 1. API 接口定义 (api/system/notice/index.ts)

```typescript
import request from '@/config/axios'

// 通知 VO 接口定义
export interface NoticeVO {
  id: number | undefined
  title: string
  type: number
  content: string
  status: number
  remark: string
  creator: string
  createTime: Date
}

// 分页查询参数
export interface NoticePageReqVO {
  pageNo: number
  pageSize: number
  title?: string
  status?: number
}

// API 函数
export const getNoticePage = (params: NoticePageReqVO) => {
  return request.get({ url: '/system/notice/page', params })
}

export const getNotice = (id: number) => {
  return request.get({ url: '/system/notice/get?id=' + id })
}

export const createNotice = (data: NoticeVO) => {
  return request.post({ url: '/system/notice/create', data })
}

export const updateNotice = (data: NoticeVO) => {
  return request.put({ url: '/system/notice/update', data })
}

export const deleteNotice = (id: number) => {
  return request.delete({ url: '/system/notice/delete?id=' + id })
}

export const pushNotice = (id: number) => {
  return request.post({ url: '/system/notice/push?id=' + id })
}
```

#### 2. 列表页面 (views/system/notice/index.vue)

```vue
<template>
  <div>
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true">
      <el-form-item label="通知标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入通知标题" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="noticeList">
      <el-table-column label="序号" type="index" width="50" />
      <el-table-column label="公告标题" prop="title" />
      <el-table-column label="公告类型" prop="type">
        <template #default="{ row }">
          {{ row.type === 1 ? '通知' : '公告' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="text" @click="handleUpdate(row)">修改</el-button>
          <el-button type="text" @click="handleDelete(row)">删除</el-button>
          <el-button type="text" @click="handlePush(row)">推送</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 表单弹窗 -->
    <NoticeForm ref="formRef" @success="getList" />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import * as NoticeApi from '@/api/system/notice'
import NoticeForm from './NoticeForm.vue'

// 响应式数据
const loading = ref(true)
const total = ref(0)
const noticeList = ref<NoticeApi.NoticeVO[]>([])

// 查询参数
const queryParams = ref<NoticeApi.NoticePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  title: '',
  status: undefined
})

// 表单引用
const formRef = ref()
const queryFormRef = ref()

// 获取列表数据
const getList = async () => {
  loading.value = true
  try {
    const data = await NoticeApi.getNoticePage(queryParams.value)
    noticeList.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.value.pageNo = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryFormRef.value?.resetFields()
  handleQuery()
}

// 新增
const handleAdd = () => {
  formRef.value?.open('create')
}

// 修改
const handleUpdate = (row: NoticeApi.NoticeVO) => {
  formRef.value?.open('update', row.id)
}

// 删除
const handleDelete = async (row: NoticeApi.NoticeVO) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？')
    await NoticeApi.deleteNotice(row.id!)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 推送
const handlePush = async (row: NoticeApi.NoticeVO) => {
  try {
    await NoticeApi.pushNotice(row.id!)
    ElMessage.success('推送成功')
  } catch (error) {
    console.error('推送失败:', error)
  }
}

// 页面加载时获取数据
onMounted(() => {
  getList()
})
</script>
```

### TypeScript 的实际好处

#### 1. 编译时错误检查

```typescript
// ❌ 这些错误在编译时就会被发现
const wrongData = {
  title: 123,        // 错误：应该是 string
  type: "notice",    // 错误：应该是 number
  status: true       // 错误：应该是 number
}

// ✅ 正确的数据
const correctData: NoticeVO = {
  id: undefined,
  title: "通知标题",  // ✓ string
  type: 1,           // ✓ number
  content: "内容",
  status: 1,         // ✓ number
  remark: "",
  creator: "",
  createTime: new Date()
}
```

#### 2. 智能提示和自动补全

```typescript
// 当你输入 notice. 时，IDE 会自动提示所有可用属性
const notice: NoticeVO = getNoticeData()
notice.title     // ✓ IDE 知道这是 string 类型
notice.type      // ✓ IDE 知道这是 number 类型
notice.xyz       // ❌ IDE 会提示这个属性不存在
```

#### 3. 重构安全

```typescript
// 如果你修改了 NoticeVO 接口，比如把 title 改成 noticeTitle
// TypeScript 会在所有使用 title 的地方报错，确保你不会遗漏任何地方
```

### 新手学习路径

#### 第一步：理解基础类型
```typescript
let title: string = "通知标题"
let type: number = 1
let isActive: boolean = true
let data: NoticeVO = { /* ... */ }
```

#### 第二步：学会定义接口
```typescript
interface MyNotice {
  title: string
  content: string
  type?: number  // 可选属性
}
```

#### 第三步：在实际项目中使用
- 复制芋道项目的代码
- 尝试修改接口定义
- 看编译错误提示
- 理解错误信息并修复

**最重要的是：多看、多想、多练！代码是写出来的，不是看出来的！**

**TypeScript 让你的代码更安全，但需要时间学习。先从简单的开始，慢慢进步！**
