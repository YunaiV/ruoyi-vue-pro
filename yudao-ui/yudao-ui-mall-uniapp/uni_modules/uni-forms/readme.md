

## Forms 表单

> **组件名：uni-forms**
> 代码块： `uForms`、`uni-forms-item`
> 关联组件：`uni-forms-item`、`uni-easyinput`、`uni-data-checkbox`、`uni-group`。


uni-app的内置组件已经有了 `<form>`组件，用于提交表单内容。

然而几乎每个表单都需要做表单验证，为了方便做表单验证，减少重复开发，`uni ui` 又基于 `<form>`组件封装了 `<uni-forms>`组件，内置了表单验证功能。

`<uni-forms>` 提供了 `rules`属性来描述校验规则、`<uni-forms-item>`子组件来包裹具体的表单项，以及给原生或三方组件提供了 `binddata()` 来设置表单值。

每个要校验的表单项，不管input还是checkbox，都必须放在`<uni-forms-item>`组件中，且一个`<uni-forms-item>`组件只能放置一个表单项。

`<uni-forms-item>`组件内部预留了显示error message的区域，默认是在表单项的底部。

另外，`<uni-forms>`组件下面的各个表单项，可以通过`<uni-group>`包裹为不同的分组。同一`<uni-group>`下的不同表单项目将聚拢在一起，同其他group保持垂直间距。`<uni-group>`仅影响视觉效果。

### [查看文档](https://uniapp.dcloud.io/component/uniui/uni-forms)
#### 如使用过程中有任何问题，或者您对uni-ui有一些好的建议，欢迎加入 uni-ui 交流群：871950839 