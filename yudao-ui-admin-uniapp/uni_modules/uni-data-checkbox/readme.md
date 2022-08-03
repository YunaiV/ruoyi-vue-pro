

## DataCheckbox 数据驱动的单选复选框
> **组件名：uni-data-checkbox**
> 代码块： `uDataCheckbox`


本组件是基于uni-app基础组件checkbox的封装。本组件要解决问题包括：

1. 数据绑定型组件：给本组件绑定一个data，会自动渲染一组候选内容。再以往，开发者需要编写不少代码实现类似功能
2. 自动的表单校验：组件绑定了data，且符合[uni-forms](https://ext.dcloud.net.cn/plugin?id=2773)组件的表单校验规范，搭配使用会自动实现表单校验
3. 本组件合并了单选多选
4. 本组件有若干风格选择，如普通的单选多选框、并列button风格、tag风格。开发者可以快速选择需要的风格。但作为一个封装组件，样式代码虽然不用自己写了，却会牺牲一定的样式自定义性

在uniCloud开发中，`DB Schema`中配置了enum枚举等类型后，在web控制台的[自动生成表单](https://uniapp.dcloud.io/uniCloud/schema?id=autocode)功能中，会自动生成``uni-data-checkbox``组件并绑定好data

### [查看文档](https://uniapp.dcloud.io/component/uniui/uni-data-checkbox)
#### 如使用过程中有任何问题，或者您对uni-ui有一些好的建议，欢迎加入 uni-ui 交流群：871950839 