import { AllowedComponentProps, VNodeProps } from './_common'

// ****************************** Props ******************************
declare interface ZPagingEmptyViewProps {
  /**
   * 空数据图片是否铺满z-paging，默认为是。若设置为否，则为填充满z-paging的剩余部分
   * @default false
   * @since 2.0.3
   */
  emptyViewFixed?: boolean;

  /**
   * 空数据图描述文字
   * @default "没有数据哦~"
   */
  emptyViewText?: string;

  /**
   * 空数据图图片，默认使用z-paging内置的图片
   * - 建议使用绝对路径，开头不要添加"@"，请以"/"开头
   */
  emptyViewImg?: string;

  /**
   * 空数据图点击重新加载文字
   * @default "重新加载"
   * @since 1.6.7
   */
  emptyViewReloadText?: string;

  /**
   * 空数据图样式，可设置空数据view的top等
   * - 如果空数据图不是fixed布局，则此处是`margin-top`
   */
  emptyViewStyle?: Record<string, any>;

  /**
   * 空数据图img样式
   */
  emptyViewImgStyle?: Record<string, any>;

  /**
   * 空数据图描述文字样式
   */
  emptyViewTitleStyle?: Record<string, any>;

  /**
   * 空数据图重新加载按钮样式
   * @since 1.6.7
   */
  emptyViewReloadStyle?: Record<string, any>;

  /**
   * 是否显示空数据图重新加载按钮(无数据时)
   * @default false
   * @since 1.6.7
   */
  showEmptyViewReload?: boolean;

  /**
   * 是否是加载失败
   * @default false
   */
  isLoadFailed?: boolean;

  /**
   * 空数据图中布局的单位
   * @default 'rpx'
   * @since 2.6.7
   */
  unit?: 'rpx' | 'px';

  // ****************************** Events ******************************
  /**
   * 点击了重新加载按钮
   */
  onReload?: () => void

  /**
   * 点击了空数据view
   * @since 2.3.3
   */
  onViewClick?: () => void
}

declare interface _ZPagingEmptyView {
  new (): {
    $props: AllowedComponentProps &
      VNodeProps &
      ZPagingEmptyViewProps
  }
}

export declare const ZPagingEmptyView: _ZPagingEmptyView

