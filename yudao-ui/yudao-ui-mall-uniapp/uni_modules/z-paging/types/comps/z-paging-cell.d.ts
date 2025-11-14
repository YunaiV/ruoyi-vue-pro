import { AllowedComponentProps, VNodeProps } from './_common'

// ****************************** Props ******************************
declare interface ZPagingCellProps {
  /**
   * z-paging-cell样式
   */
  cellStyle?: Record<string, any>
}

// ****************************** Slots ******************************
declare interface ZPagingCellSlots {
  // ******************** 主体布局Slot ********************
  /**
   * 默认插入的view
   */
  ['default']?: () => any
}

declare interface _ZPagingCell {
  new (): {
    $props: AllowedComponentProps &
      VNodeProps &
      ZPagingCellProps
    $slots: ZPagingCellSlots
  }
}

export declare const ZPagingCell: _ZPagingCell
