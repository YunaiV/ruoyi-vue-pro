# 组件库切换

`Vue Admin` 支持你自由选择组件库，目前演示站点的默认组件库是 `Ant Design Vue`，与旧版本保持一致。同时框架还内置了 `Element Plus` 版本和 `Naive UI` 版本，你可以根据自己的喜好选择。

## 新增组件库应用

如果你想用其他别的组件库，你只需要按以下步骤进行操作：

1. 在`apps`内创建一个新的文件夹，例如`apps/web-xxx`。
2. 更改`apps/web-xxx/package.json`的`name`字段为`web-xxx`。
3. 移除其他组件库依赖及代码，并用你的组件库进行替换相应逻辑，需要改动的地方不多。
4. 调整`locales`内的语言文件。
5. 调整 `app.vue` 内的组件。
6. 自行适配组件库的主题，与 `Vben Admin` 契合。
7. 调整 `.env` 内的应用名
8. 在大仓根目录增加 `dev:xxx` 脚本
9. 执行 `pnpm install` 安装依赖
