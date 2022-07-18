import type { CSSProperties } from 'vue'

declare global {
  declare type ComponentName =
    | 'Radio'
    | 'RadioButton'
    | 'Checkbox'
    | 'CheckboxButton'
    | 'Input'
    | 'Autocomplete'
    | 'InputNumber'
    | 'Select'
    | 'Cascader'
    | 'Switch'
    | 'Slider'
    | 'TimePicker'
    | 'DatePicker'
    | 'Rate'
    | 'ColorPicker'
    | 'Transfer'
    | 'Divider'
    | 'TimeSelect'
    | 'SelectV2'
    | 'InputPassword'
    | 'Editor'

  declare type ColProps = {
    span?: number
    xs?: number
    sm?: number
    md?: number
    lg?: number
    xl?: number
    tag?: string
  }

  declare type FormValueType = string | number | string[] | number[] | boolean | undefined | null

  declare type FormItemProps = {
    labelWidth?: string | number
    required?: boolean
    rules?: Recordable
    error?: string
    showMessage?: boolean
    inlineMessage?: boolean
    style?: CSSProperties
  }

  declare type ComponentOptions = {
    label?: string
    value?: FormValueType
    disabled?: boolean
    key?: string | number
    children?: ComponentOptions[]
    options?: ComponentOptions[]
  } & Recordable

  declare type ComponentOptionsAlias = {
    labelField?: string
    valueField?: string
  }

  declare type ComponentProps = {
    optionsAlias?: ComponentOptionsAlias
    options?: ComponentOptions[]
    optionsSlot?: boolean
  } & Recordable

  declare type FormSchema = {
    // 唯一值
    field: string
    // 标题
    label?: string
    // 提示
    labelMessage?: string
    // col组件属性
    colProps?: ColProps
    // 表单组件属性，slots对应的是表单组件的插槽，规则：${field}-xxx，具体可以查看element-plus文档
    componentProps?: { slots?: Recordable } & ComponentProps
    // formItem组件属性
    formItemProps?: FormItemProps
    // 渲染的组件
    component?: ComponentName
    // 初始值
    value?: FormValueType
    // 是否隐藏
    hidden?: boolean
    // 远程加载下拉项
    api?: <T = any>() => Promise<T>
  }

  declare type FormSetPropsType = {
    field: string
    path: string
    value: any
  }
}
