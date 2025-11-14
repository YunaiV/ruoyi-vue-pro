---
outline: deep
---

# Vben Modal 模态框

框架提供的模态框组件，支持`拖拽`、`全屏`、`自动高度`、`loading`等功能。

> 如果文档内没有参数说明，可以尝试在在线示例内寻找

::: info 写在前面

如果你觉得现有组件的封装不够理想，或者不完全符合你的需求，大可以直接使用原生组件，亦或亲手封装一个适合的组件。框架提供的组件并非束缚，使用与否，完全取决于你的需求与自由。

:::

::: tip README

下方示例代码中的，存在一些国际化、主题色未适配问题，这些问题只在文档内会出现，实际使用并不会有这些问题，可忽略，不必纠结。

:::

## 基础用法

使用 `useVbenModal` 创建最基础的模态框。

<DemoPreview dir="demos/vben-modal/basic" />

## 组件抽离

Modal 内的内容一般业务中，会比较复杂，所以我们可以将 modal 内的内容抽离出来，也方便复用。通过 `connectedComponent` 参数，可以将内外组件进行连接，而不用其他任何操作。

<DemoPreview dir="demos/vben-modal/extra" />

## 开启拖拽

通过 `draggable` 参数，可开启拖拽功能。

<DemoPreview dir="demos/vben-modal/draggable" />

## 自动计算高度

弹窗会自动计算内容高度，超过一定高度会出现滚动条，同时结合 `loading` 效果以及使用 `prepend-footer` 插槽。

<DemoPreview dir="demos/vben-modal/auto-height" />

## 使用 Api

通过 `modalApi` 可以调用 modal 的方法以及使用 `setState` 更新 modal 的状态。

<DemoPreview dir="demos/vben-modal/dynamic" />

## 数据共享

如果你使用了 `connectedComponent` 参数，那么内外组件会共享数据，比如一些表单回填等操作。可以用 `modalApi` 来获取数据和设置数据，配合 `onOpenChange`，可以满足大部分的需求。

<DemoPreview dir="demos/vben-modal/shared-data" />

## 动画类型

通过 `animationType` 属性可以控制弹窗的动画效果：

- `slide`（默认）：从顶部向下滑动进入/退出
- `scale`：缩放淡入/淡出效果

<DemoPreview dir="demos/vben-modal/animation-type" />

::: info 注意

- `VbenModal` 组件对与参数的处理优先级是 `slot` > `props` > `state`(通过api更新的状态以及useVbenModal参数)。如果你已经传入了 `slot` 或者 `props`，那么 `setState` 将不会生效，这种情况下你可以通过 `slot` 或者 `props` 来更新状态。
- 如果你使用到了 `connectedComponent` 参数，那么会存在 2 个`useVbenModal`, 此时，如果同时设置了相同的参数，那么以内部为准（也就是没有设置 connectedComponent 的代码）。比如 同时设置了 `onConfirm`，那么以内部的 `onConfirm` 为准。`onOpenChange`事件除外，内外都会触发。另外，如果设置了`destroyOnClose`，内部Modal及其子组件会在被关闭后<b>完全销毁</b>。
- 如果弹窗的默认行为不符合你的预期，可以在`src\bootstrap.ts`中修改`setDefaultModalProps`的参数来设置默认的属性，如默认隐藏全屏按钮，修改默认ZIndex等。

:::

## API

```ts
// Modal 为弹窗组件
// modalApi 为弹窗的方法
const [Modal, modalApi] = useVbenModal({
  // 属性
  // 事件
});
```

### Props

所有属性都可以传入 `useVbenModal` 的第一个参数中。

| 属性名 | 描述 | 类型 | 默认值 |
| --- | --- | --- | --- |
| appendToMain | 是否挂载到内容区域（默认挂载到body） | `boolean` | `false` |
| connectedComponent | 连接另一个Modal组件 | `Component` | - |
| destroyOnClose | 关闭时销毁 | `boolean` | `false` |
| title | 标题 | `string\|slot` | - |
| titleTooltip | 标题提示信息 | `string\|slot` | - |
| description | 描述信息 | `string\|slot` | - |
| isOpen | 弹窗打开状态 | `boolean` | `false` |
| loading | 弹窗加载状态 | `boolean` | `false` |
| fullscreen | 全屏显示 | `boolean` | `false` |
| fullscreenButton | 显示全屏按钮 | `boolean` | `true` |
| draggable | 可拖拽 | `boolean` | `false` |
| closable | 显示关闭按钮 | `boolean` | `true` |
| centered | 居中显示 | `boolean` | `false` |
| modal | 显示遮罩 | `boolean` | `true` |
| header | 显示header | `boolean` | `true` |
| footer | 显示footer | `boolean\|slot` | `true` |
| confirmDisabled | 禁用确认按钮 | `boolean` | `false` |
| confirmLoading | 确认按钮loading状态 | `boolean` | `false` |
| closeOnClickModal | 点击遮罩关闭弹窗 | `boolean` | `true` |
| closeOnPressEscape | esc 关闭弹窗 | `boolean` | `true` |
| confirmText | 确认按钮文本 | `string\|slot` | `确认` |
| cancelText | 取消按钮文本 | `string\|slot` | `取消` |
| showCancelButton | 显示取消按钮 | `boolean` | `true` |
| showConfirmButton | 显示确认按钮 | `boolean` | `true` |
| class | modal的class，宽度通过这个配置 | `string` | - |
| contentClass | modal内容区域的class | `string` | - |
| footerClass | modal底部区域的class | `string` | - |
| headerClass | modal顶部区域的class | `string` | - |
| bordered | 是否显示border | `boolean` | `false` |
| zIndex | 弹窗的ZIndex层级 | `number` | `1000` |
| overlayBlur | 遮罩模糊度 | `number` | - |
| animationType | 动画类型 | `'slide' \| 'scale'` | `'slide'` |
| submitting | 标记为提交中，锁定弹窗当前状态 | `boolean` | `false` |

::: info appendToMain

`appendToMain`可以指定将弹窗挂载到内容区域，打开这种弹窗时，内容区域以外的部分（标签栏、导航菜单等等）不会被遮挡。默认情况下，弹窗会挂载到body上。但是：挂载到内容区域时，作为页面根容器的`Page`组件，需要设置`auto-content-height`属性，以便弹窗能够正确计算高度。

:::

### Event

以下事件，只有在 `useVbenModal({onCancel:()=>{}})` 中传入才会生效。

| 事件名 | 描述 | 类型 | 版本号 |
| --- | --- | --- | --- |
| onBeforeClose | 关闭前触发，返回 `false`或者被`reject`则禁止关闭 | `()=>Promise<boolean>\|boolean` | >5.5.2支持Promise |
| onCancel | 点击取消按钮触发 | `()=>void` |  |
| onClosed | 关闭动画播放完毕时触发 | `()=>void` | >5.4.3 |
| onConfirm | 点击确认按钮触发 | `()=>void` |  |
| onOpenChange | 关闭或者打开弹窗时触发 | `(isOpen:boolean)=>void` |  |
| onOpened | 打开动画播放完毕时触发 | `()=>void` | >5.4.3 |

### Slots

除了上面的属性类型包含`slot`，还可以通过插槽来自定义弹窗的内容。

| 插槽名         | 描述                                               |
| -------------- | -------------------------------------------------- |
| default        | 默认插槽 - 弹窗内容                                |
| prepend-footer | 取消按钮左侧                                       |
| center-footer  | 取消按钮和确认按钮中间（不使用 footer 插槽时有效） |
| append-footer  | 确认按钮右侧                                       |

### modalApi

| 方法 | 描述 | 类型 | 版本 |
| --- | --- | --- | --- |
| setState | 动态设置弹窗状态属性 | `(((prev: ModalState) => Partial<ModalState>)\| Partial<ModalState>)=>modalApi` | - |
| open | 打开弹窗 | `()=>void` | - |
| close | 关闭弹窗 | `()=>void` | - |
| setData | 设置共享数据 | `<T>(data:T)=>modalApi` | - |
| getData | 获取共享数据 | `<T>()=>T` | - |
| useStore | 获取可响应式状态 | - | - |
| lock | 将弹窗标记为提交中，锁定当前状态 | `(isLock:boolean)=>modalApi` | >5.5.2 |
| unlock | lock方法的反操作，解除弹窗的锁定状态，也是lock(false)的别名 | `()=>modalApi` | >5.5.3 |

::: info lock

`lock`方法用于锁定当前弹窗的状态，一般用于提交数据的过程中防止用户重复提交或者弹窗被意外关闭、表单数据被改变等等。当处于锁定状态时，弹窗的确认按钮会变为loading状态，同时禁用取消按钮和关闭按钮、禁止ESC或者点击遮罩等方式关闭弹窗、开启弹窗的spinner动画以遮挡弹窗内容。调用`close`方法关闭处于锁定状态的弹窗时，会自动解锁。要主动解除这种状态，可以调用`unlock`方法或者再次调用lock方法并传入false参数。

:::
