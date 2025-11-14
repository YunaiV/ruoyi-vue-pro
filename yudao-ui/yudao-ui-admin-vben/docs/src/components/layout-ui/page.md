---
outline: deep
---

# Page 常规页面组件

提供一个常规页面布局的组件，包括头部、内容区域、底部三个部分。

::: info 写在前面

本组件是一个基本布局组件。如果有更多的通用页面布局需求（比如双列布局等），可以根据实际需求自行封装。

:::

## 基础用法

将`Page`作为你的业务页面的根组件即可。

### Props

| 属性名 | 描述 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| title | 页面标题 | `string\|slot` | - | - |
| description | 页面描述（标题下的内容） | `string\|slot` | - | - |
| contentClass | 内容区域的class | `string` | - | - |
| headerClass | 头部区域的class | `string` | - | - |
| footerClass | 底部区域的class | `string` | - | - |
| autoContentHeight | 自动调整内容区域的高度 | `boolean` | `false` | - |

::: tip 注意

如果`title`、`description`、`extra`三者均未提供有效内容（通过`props`或者`slots`均可），则页面头部区域不会渲染。

:::

### Slots

| 插槽名称    | 描述         |
| ----------- | ------------ |
| default     | 页面内容     |
| title       | 页面标题     |
| description | 页面描述     |
| extra       | 页面头部右侧 |
| footer      | 页面底部     |
