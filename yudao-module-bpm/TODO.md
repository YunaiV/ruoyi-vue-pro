1. 类名可以去掉 Bpm 前缀哈
2. 后续接口我们分成 admin 管理后天，app 用户前端；所以 controller 包下，需要有 admin 和 app 两个子包。可见 https://gitee.com/zhijiantianya/ruoyi-vue-pro/tree/feature/multi-module/yudao-module-member/yudao-module-member-impl/src/main/java/cn/iocoder/yudao/module/member/controller
3. yudao-module-xxx-api 是暴露接口给外部模块，所以可以把 yudao-module-bpm-core-service-api 改成 yudao-module-bpm-api。大概率用不了暴露 BpmUserGroupServiceApi 哈。
4. yudao-module-bpm-core-service-impl 模块，要不改成 yudao-module-bpm-base，本质上是提供给 yudao-module-bpm-flowable-impl 和 yudao-module-bpm-activiti-impl 继承的。这样的话，我们在 yudao-module-bpm-base： 
   * 定义一些可被继承的类，例如说 ModelAbstractService；
   * 定义一些无需被继承的类，例如说 UserGroupService、UserGroupController 等等；
