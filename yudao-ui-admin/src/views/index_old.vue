<template>
  <div class="app-container home">
    <el-row :gutter="20">
      <el-col :sm="24" :lg="24">
        <blockquote class="text-warning" style="font-size: 14px">
          领取阿里云通用云产品1888优惠券
          <br />
          <el-link
            href="https://www.aliyun.com/minisite/goods?userCode=brki8iof"
            type="primary"
            target="_blank"
            >https://www.aliyun.com/minisite/goods?userCode=brki8iof</el-link
          >
          <br />
          领取腾讯云通用云产品2860优惠券
          <br />
          <el-link
            href="https://cloud.tencent.com/redirect.php?redirect=1025&cps_key=198c8df2ed259157187173bc7f4f32fd&from=console"
            type="primary"
            target="_blank"
            >https://cloud.tencent.com/redirect.php?redirect=1025&cps_key=198c8df2ed259157187173bc7f4f32fd&from=console</el-link
          >
          <br />
          阿里云服务器折扣区
          <el-link href="http://aly.ruoyi.vip" type="primary" target="_blank"
            >>☛☛点我进入☚☚</el-link
          >
          &nbsp;&nbsp;&nbsp; 腾讯云服务器秒杀区
          <el-link href="http://txy.ruoyi.vip" type="primary" target="_blank"
            >>☛☛点我进入☚☚</el-link
          ><br />
          <h4 class="text-danger">
            云产品通用红包，可叠加官网常规优惠使用。(仅限新用户)
          </h4>
        </blockquote>

        <hr />
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :sm="24" :lg="12" style="padding-left: 20px">
        <h2>芋道后台管理框架</h2>
        <p>
          一直想做一款后台管理系统，看了很多优秀的开源项目但是发现没有合适自己的。于是利用空闲休息时间开始自己写一套后台系统。如此有了芋道管理系统。，她可以用于所有的Web应用程序，如网站管理后台，网站会员中心，CMS，CRM，OA等等，当然，您也可以对她进行深度定制，以做出更强系统。所有前端后台代码封装过后十分精简易上手，出错概率低。同时支持移动客户端访问。系统会陆续更新一些实用功能。
        </p>
        <p>
          <b>当前版本:</b> <span>v{{ version }}</span>
        </p>
        <p>
          <el-tag type="danger">&yen;免费开源</el-tag>
        </p>
        <p>
          <el-button
            type="primary"
            size="mini"
            icon="el-icon-cloudy"
            plain
            @click="goTarget('https://gitee.com/zhijiantianya/ruoyi-vue-pro')"
            >访问码云</el-button
          >
          <el-button
            size="mini"
            icon="el-icon-s-home"
            plain
            @click="goTarget('http://ruoyi.vip')"
            >访问主页</el-button
          >
        </p>
      </el-col>

      <el-col :sm="24" :lg="12" style="padding-left: 50px">
        <el-row>
          <el-col :span="12">
            <h2>技术选型</h2>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="6">
            <h4>后端技术</h4>
            <ul>
              <li>SpringBoot</li>
              <li>Spring Security</li>
              <li>JWT</li>
              <li>MyBatis</li>
              <li>Druid</li>
              <li>Fastjson</li>
              <li>...</li>
            </ul>
          </el-col>
          <el-col :span="6">
            <h4>前端技术</h4>
            <ul>
              <li>Vue</li>
              <li>Vuex</li>
              <li>Element-ui</li>
              <li>Axios</li>
              <li>Sass</li>
              <li>Quill</li>
              <li>...</li>
            </ul>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
    <el-divider />
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="update-log">
          <div slot="header" class="clearfix">
            <span>联系信息</span>
          </div>
          <div class="body">
            <p>
              <i class="el-icon-s-promotion"></i> 官网：<el-link
                href="http://www.ruoyi.vip"
                target="_blank"
                >http://www.ruoyi.vip</el-link
              >
            </p>
            <p>
              <i class="el-icon-user-solid"></i> QQ群：<s>满937441</s>
              <s>满887144332</s> <s>满180251782</s>
              <a href="https://jq.qq.com/?_wv=1027&k=4WWwqNxN" target="_blank"
                > 104180207</a
              >
            </p>
            <p>
              <i class="el-icon-chat-dot-round"></i> 微信：<a
                href="javascript:;"
                >/ *芋道</a
              >
            </p>
            <p>
              <i class="el-icon-money"></i> 支付宝：<a
                href="javascript:;"
                class="支付宝信息"
                >/ *芋道</a
              >
            </p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="update-log">
          <div slot="header" class="clearfix">
            <span>更新日志</span>
          </div>
          <el-collapse accordion>
            <el-collapse-item title="v3.3.0 - 2020-12-14">
              <ol>
                <li>新增缓存监控功能</li>
                <li>支持主题风格配置</li>
                <li>修复多级菜单之间切换无法缓存的问题</li>
                <li>多级菜单自动配置组件</li>
                <li>代码生成预览支持高亮显示</li>
                <li>支持Get请求映射Params参数</li>
                <li>删除用户和角色解绑关联</li>
                <li>去除用户手机邮箱部门必填验证</li>
                <li>Excel支持注解align对齐方式</li>
                <li>Excel支持导入Boolean型数据</li>
                <li>优化头像样式，鼠标移入悬停遮罩</li>
                <li>代码生成预览提供滚动机制</li>
                <li>代码生成删除多余的数字float类型</li>
                <li>修正转换字符串的目标字符集属性</li>
                <li>回显数据字典防止空值报错</li>
                <li>日志记录增加过滤多文件场景</li>
                <li>修改缓存Set方法可能导致嵌套的问题</li>
                <li>移除前端一些多余的依赖</li>
                <li>防止安全扫描YUI出现的风险提示</li>
                <li>修改node-sass为dart-sass</li>
                <li>升级SpringBoot到最新版本2.1.18</li>
                <li>升级poi到最新版本4.1.2</li>
                <li>升级oshi到最新版本v5.3.6</li>
                <li>升级bitwalker到最新版本1.21</li>
                <li>升级axios到最新版本0.21.0</li>
                <li>升级element-ui到最新版本2.14.1</li>
                <li>升级vue到最新版本2.6.12</li>
                <li>升级vuex到最新版本3.6.0</li>
                <li>升级vue-cli到版本4.5.9</li>
                <li>升级vue-router到最新版本3.4.9</li>
                <li>升级vue-cli到最新版本4.4.6</li>
                <li>升级vue-cropper到最新版本0.5.5</li>
                <li>升级clipboard到最新版本2.0.6</li>
                <li>升级core-js到最新版本3.8.1</li>
                <li>升级echarts到最新版本4.9.0</li>
                <li>升级file-saver到最新版本2.0.4</li>
                <li>升级fuse.js到最新版本6.4.3</li>
                <li>升级js-beautify到最新版本1.13.0</li>
                <li>升级js-cookie到最新版本2.2.1</li>
                <li>升级path-to-regexp到最新版本6.2.0</li>
                <li>升级quill到最新版本1.3.7</li>
                <li>升级screenfull到最新版本5.0.2</li>
                <li>升级sortablejs到最新版本1.10.2</li>
                <li>升级vuedraggable到最新版本2.24.3</li>
                <li>升级chalk到最新版本4.1.0</li>
                <li>升级eslint到最新版本7.15.0</li>
                <li>升级eslint-plugin-vue到最新版本7.2.0</li>
                <li>升级lint-staged到最新版本10.5.3</li>
                <li>升级runjs到最新版本4.4.2</li>
                <li>升级sass-loader到最新版本10.1.0</li>
                <li>升级script-ext-html-webpack-plugin到最新版本2.1.5</li>
                <li>升级svg-sprite-loader到最新版本5.1.1</li>
                <li>升级vue-template-compiler到最新版本2.6.12</li>
                <li>其他细节优化</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v3.2.1 - 2020-11-18">
              <ol>
                <li>阻止任意文件下载漏洞</li>
                <li>代码生成支持上传控件</li>
                <li>新增图片上传组件</li>
                <li>调整默认首页</li>
                <li>升级druid到最新版本v1.2.2</li>
                <li>mapperLocations配置支持分隔符</li>
                <li>权限信息调整</li>
                <li>调整sql默认时间</li>
                <li>解决代码生成没有bit类型的问题</li>
                <li>升级pagehelper到最新版1.3.0</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v3.2.0 - 2020-10-10">
              <ol>
                <li>升级springboot版本到2.1.17 提升安全性</li>
                <li>升级oshi到最新版本v5.2.5</li>
                <li>升级druid到最新版本v1.2.1</li>
                <li>升级jjwt到版本0.9.1</li>
                <li>升级fastjson到最新版1.2.74</li>
                <li>修改sass为node-sass，避免el-icon图标乱码</li>
                <li>代码生成支持同步数据库</li>
                <li>代码生成支持富文本控件</li>
                <li>代码生成页面时不忽略remark属性</li>
                <li>代码生成添加select必填选项</li>
                <li>Excel导出类型NUMERIC支持精度浮点类型</li>
                <li>Excel导出targetAttr优化获取值，防止get方法不规范</li>
                <li>Excel注解支持自动统计数据总和</li>
                <li>Excel注解支持设置BigDecimal精度&舍入规则</li>
                <li>菜单&数据权限新增（展开/折叠 全选/全不选 父子联动）</li>
                <li>允许用户分配到部门父节点</li>
                <li>菜单新增是否缓存keep-alive</li>
                <li>表格操作列间距调整</li>
                <li>限制系统内置参数不允许删除</li>
                <li>富文本组件优化，支持自定义高度&图片冲突问题</li>
                <li>富文本工具栏样式对齐</li>
                <li>导入excel整形值校验优化</li>
                <li>修复页签关闭所有时固定标签路由不刷新问题</li>
                <li>表单构建布局型组件新增按钮</li>
                <li>左侧菜单文字过长显示省略号</li>
                <li>修正根节点为子部门时，树状结构显示问题</li>
                <li>修正调用目标字符串最大长度</li>
                <li>修正菜单提示信息错误</li>
                <li>修正定时任务执行一次权限标识</li>
                <li>修正数据库字符串类型nvarchar</li>
                <li>优化递归子节点</li>
                <li>优化数据权限判断</li>
                <li>其他细节优化</li>
              </ol>
            </el-collapse-item>

            <el-collapse-item title="v3.1.0 - 2020-08-13">
              <ol>
                <li>表格工具栏右侧添加刷新&显隐查询组件</li>
                <li>后端支持CORS跨域请求</li>
                <li>代码生成支持选择上级菜单</li>
                <li>代码生成支持自定义路径</li>
                <li>代码生成支持复选框</li>
                <li>Excel导出导入支持dictType字典类型</li>
                <li>Excel支持分割字符串组内容</li>
                <li>验证码类型支持（数组计算、字符验证）</li>
                <li>升级vue-cli版本到4.4.4</li>
                <li>修改 node-sass 为 dart-sass</li>
                <li>表单类型为Integer/Long设置整形默认值</li>
                <li>代码生成器默认mapper路径与默认mapperScan路径不一致</li>
                <li>优化防重复提交拦截器</li>
                <li>优化上级菜单不能选择自己</li>
                <li>修复角色的权限分配后，未实时生效问题</li>
                <li>修复在线用户日志记录类型</li>
                <li>修复富文本空格和缩进保存后不生效问题</li>
                <li>修复在线用户判断逻辑</li>
                <li>唯一限制条件只返回单条数据</li>
                <li>添加获取当前的环境配置方法</li>
                <li>超时登录后页面跳转到首页</li>
                <li>全局异常状态汉化拦截处理</li>
                <li>HTML过滤器改为将html转义</li>
                <li>检查字符支持小数点&降级改成异常提醒</li>
                <li>其他细节优化</li>
              </ol>
            </el-collapse-item>

            <el-collapse-item title="v3.0.0 - 2020-07-20">
              <ol>
                <li>单应用调整为多模块项目</li>
                <li>升级element-ui版本到2.13.2</li>
                <li>删除babel，提高编译速度。</li>
                <li>新增菜单默认主类目</li>
                <li>编码文件名修改为uuid方式</li>
                <li>定时任务cron表达式验证</li>
                <li>角色权限修改时已有权限未自动勾选异常修复</li>
                <li>防止切换权限用户后登录出现404</li>
                <li>Excel支持sort导出排序</li>
                <li>创建用户不允许选择超级管理员角色</li>
                <li>修复代码生成导入表结构出现异常页面不提醒问题</li>
                <li>修复代码生成点击多次表修改数据不变化的问题</li>
                <li>修复头像上传成功二次打开无法改变裁剪框大小和位置问题</li>
                <li>修复布局为small者mini用户表单显示错位问题</li>
                <li>修复热部署导致的强换异常问题</li>
                <li>修改用户管理复选框宽度，防止部分浏览器出现省略号</li>
                <li>IpUtils工具，清除Xss特殊字符，防止Xff注入攻击</li>
                <li>生成domain 如果是浮点型 统一用BigDecimal</li>
                <li>定时任务调整label-width，防止部署出现错位</li>
                <li>调整表头固定列默认样式</li>
                <li>代码生成模板调整，字段为String并且必填则加空串条件</li>
                <li>代码生成字典Integer/Long使用parseInt</li>
                <li>
                  修复dict_sort不可update为0的问题&查询返回增加dict_sort升序排序
                </li>
                <li>修正岗位导出权限注解</li>
                <li>禁止加密密文返回前端</li>
                <li>修复代码生成页面中的查询条件创建时间未生效的问题</li>
                <li>修复首页搜索菜单外链无法点击跳转问题</li>
                <li>修复菜单管理选择图标，backspace删除时不过滤数据</li>
                <li>用户管理部门分支节点不可检查&显示计数</li>
                <li>数据范围过滤属性调整</li>
                <li>其他细节优化</li>
              </ol>
            </el-collapse-item>

            <el-collapse-item title="v2.3.0 - 2020-06-01">
              <ol>
                <li>升级fastjson到最新版1.2.70 修复高危安全漏洞</li>
                <li>dev启动默认打开浏览器</li>
                <li>vue-cli使用默认source-map</li>
                <li>slidebar eslint报错优化</li>
                <li>当tags-view滚动关闭右键菜单</li>
                <li>字典管理添加缓存读取</li>
                <li>参数管理支持缓存操作</li>
                <li>支持一级菜单（和主页同级）在main区域显示</li>
                <li>限制外链地址必须以http(s)开头</li>
                <li>tagview & sidebar 主题颜色与element ui(全局)同步</li>
                <li>修改数据源类型优先级，先根据方法，再根据类</li>
                <li>支持是否需要设置token属性，自定义返回码消息。</li>
                <li>swagger请求前缀加入配置。</li>
                <li>登录地点设置内容过长则隐藏显示</li>
                <li>修复定时任务执行一次按钮后不提示消息问题</li>
                <li>修改上级部门（选择项排除本身和下级）</li>
                <li>通用http发送方法增加参数 contentType 编码类型</li>
                <li>更换IP地址查询接口</li>
                <li>修复页签变量undefined</li>
                <li>添加校验部门包含未停用的子部门</li>
                <li>修改定时任务详情下次执行时间日期显示错误</li>
                <li>角色管理查询设置默认排序字段</li>
                <li>swagger添加enable参数控制是否启用</li>
                <li>只对json类型请求构建可重复读取inputStream的request</li>
                <li>修改代码生成字典字段int类型没有自动选中问题</li>
                <li>vuex用户名取值修正</li>
                <li>表格树模板去掉多余的)</li>
                <li>代码生成序号修正</li>
                <li>全屏情况下不调整上外边距</li>
                <li>代码生成Date字段添加默认格式</li>
                <li>用户管理角色选择权限控制</li>
                <li>修复路由懒加载报错问题</li>
                <li>模板sql.vm添加菜单状态</li>
                <li>设置用户名称不能修改</li>
                <li>dialog添加append-to-body属性，防止ie遮罩</li>
                <li>菜单区分状态和显示隐藏功能</li>
                <li>升级fastjson到最新版1.2.68 修复安全加固</li>
                <li>修复代码生成如果选择字典类型缺失逗号问题</li>
                <li>登录请求params更换为data，防止暴露url</li>
                <li>日志返回时间格式处理</li>
                <li>添加handle控制允许拖动的元素</li>
                <li>布局设置点击扩大范围</li>
                <li>代码生成列属性排序查询</li>
                <li>代码生成列支持拖动排序</li>
                <li>修复时间格式不支持ios问题</li>
                <li>表单构建添加父级class，防止冲突</li>
                <li>定时任务并发属性修正</li>
                <li>角色禁用&菜单隐藏不查询权限</li>
                <li>其他细节优化</li>
              </ol>
            </el-collapse-item>

            <el-collapse-item title="v2.2.0 - 2020-03-18">
              <ol>
                <li>系统监控新增定时任务功能</li>
                <li>添加一个打包Web工程bat</li>
                <li>修复页签鼠标滚轮按下的时候，可以关闭不可关闭的tag</li>
                <li>修复点击退出登录有时会无提示问题</li>
                <li>修复防重复提交注解无效问题</li>
                <li>修复通知公告批量删除异常问题</li>
                <li>添加菜单时路由地址必填限制</li>
                <li>代码生成字段描述可编辑</li>
                <li>修复用户修改个人信息导致缓存不过期问题</li>
                <li>个人信息创建时间获取正确属性值</li>
                <li>操作日志详细显示正确类型</li>
                <li>导入表单击行数据时选中对应的复选框</li>
                <li>批量替换表前缀逻辑调整</li>
                <li>固定重定向路径表达式</li>
                <li>升级element-ui版本到2.13.0</li>
                <li>操作日志排序调整</li>
                <li>修复charts切换侧边栏或者缩放窗口显示bug</li>
                <li>其他细节优化</li>
              </ol>
            </el-collapse-item>

            <el-collapse-item title="v2.1.0 - 2020-02-24">
              <ol>
                <li>新增表单构建</li>
                <li>代码生成支持树表结构</li>
                <li>新增用户导入</li>
                <li>修复动态加载路由页面刷新问题</li>
                <li>修复地址开关无效问题</li>
                <li>汉化错误提示页面</li>
                <li>代码生成已知问题修改</li>
                <li>修复多数据源下配置关闭出现异常处理</li>
                <li>添加HTML过滤器，用于去除XSS漏洞隐患</li>
                <li>修复上传头像控制台出现异常</li>
                <li>修改用户管理分页不正确的问题</li>
                <li>修复验证码记录提示错误</li>
                <li>修复request.js缺少Message引用</li>
                <li>修复表格时间为空出现的异常</li>
                <li>添加Jackson日期反序列化时区配置</li>
                <li>调整根据用户权限加载菜单数据树形结构</li>
                <li>调整成功登录不恢复按钮，防止多次点击</li>
                <li>修改用户个人资料同步缓存信息</li>
                <li>修复页面同时出现el-upload和Editor不显示处理</li>
                <li>修复在角色管理页修改菜单权限偶尔未选中问题</li>
                <li>配置文件新增redis密码属性</li>
                <li>设置mybatis全局的配置文件</li>
                <li>其他细节优化</li>
              </ol>
            </el-collapse-item>

            <el-collapse-item title="v2.0.0 - 2019-12-02">
              <ol>
                <li>新增代码生成</li>
                <li>新增@RepeatSubmit注解，防止重复提交</li>
                <li>新增菜单主目录添加/删除操作</li>
                <li>日志记录过滤特殊对象，防止转换异常</li>
                <li>修改代码生成路由脚本错误</li>
                <li>用户上传头像实时同步缓存，无需重新登录</li>
                <li>调整切换页签后不重新加载数据</li>
                <li>添加jsencrypt实现参数的前端加密</li>
                <li>系统退出删除用户缓存记录</li>
                <li>其他细节优化</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v1.1.0 - 2019-11-11">
              <ol>
                <li>新增在线用户管理</li>
                <li>新增按钮组功能实现（批量删除、导出、清空）</li>
                <li>新增查询条件重置按钮</li>
                <li>新增Swagger全局Token配置</li>
                <li>新增后端参数校验</li>
                <li>修复字典管理页面的日期查询异常</li>
                <li>修改时间函数命名防止冲突</li>
                <li>去除菜单上级校验，默认为顶级</li>
                <li>修复用户密码无法修改问题</li>
                <li>修复菜单类型为按钮时不显示权限标识</li>
                <li>其他细节优化</li>
              </ol>
            </el-collapse-item>
            <el-collapse-item title="v1.0.0 - 2019-10-08">
              <ol>
                <li>芋道前后端分离系统正式发布</li>
              </ol>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="update-log">
          <div slot="header" class="clearfix">
            <span>捐赠支持</span>
          </div>
          <div class="body">
            <img
              src="https://oscimg.oschina.net/oscnet/up-d6695f82666e5018f715c41cb7ee60d3b73.png"
              alt="donate"
              width="100%"
            />
            <span style="display: inline-block; height: 30px; line-height: 30px"
              >你可以请作者喝杯咖啡表示鼓励</span
            >
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "Index",
  data() {
    return {
      // 版本号
      version: "3.3.0",
    };
  },
  methods: {
    goTarget(href) {
      window.open(href, "_blank");
    },
  },
};
</script>

<style scoped lang="scss">
.home {
  blockquote {
    padding: 10px 20px;
    margin: 0 0 20px;
    font-size: 17.5px;
    border-left: 5px solid #eee;
  }
  hr {
    margin-top: 20px;
    margin-bottom: 20px;
    border: 0;
    border-top: 1px solid #eee;
  }
  .col-item {
    margin-bottom: 20px;
  }

  ul {
    padding: 0;
    margin: 0;
  }

  font-family: "open sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 13px;
  color: #676a6c;
  overflow-x: hidden;

  ul {
    list-style-type: none;
  }

  h4 {
    margin-top: 0px;
  }

  h2 {
    margin-top: 10px;
    font-size: 26px;
    font-weight: 100;
  }

  p {
    margin-top: 10px;

    b {
      font-weight: 700;
    }
  }

  .update-log {
    ol {
      display: block;
      list-style-type: decimal;
      margin-block-start: 1em;
      margin-block-end: 1em;
      margin-inline-start: 0;
      margin-inline-end: 0;
      padding-inline-start: 40px;
    }
  }
}
</style>

