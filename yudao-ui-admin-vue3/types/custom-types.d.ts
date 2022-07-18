import { SlateDescendant } from '@wangeditor/editor'

declare module 'slate' {
  interface CustomTypes {
    // 扩展 text
    Text: {
      text: string
      bold?: boolean
      italic?: boolean
      code?: boolean
      through?: boolean
      underline?: boolean
      sup?: boolean
      sub?: boolean
      color?: string
      bgColor?: string
      fontSize?: string
      fontFamily?: string
    }

    // 扩展 Element 的 type 属性
    Element: {
      type: string
      children: SlateDescendant[]
    }
  }
}
