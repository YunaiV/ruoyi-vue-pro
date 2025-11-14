import { AllowedComponentProps, VNodeProps } from './_common'

// ****************************** Props ******************************
declare interface ZPagingSwiperItemProps {
  /**
   * 当前组件的index，也就是当前组件是swiper中的第几个
   * @default 0
   */
  tabIndex?: number

  /**
   * 当前swiper切换到第几个index
   * @default 0
   */
  currentIndex?: number

  /**
   * 是否使用虚拟列表。使用页面滚动或nvue时，不支持虚拟列表。在nvue中z-paging内置了list组件，效果与虚拟列表类似，并且可以提供更好的性能。
   * @default false
   */
  useVirtualList?: boolean

  /**
   * 虚拟列表cell高度模式，默认为`fixed`，也就是每个cell高度完全相同，将以第一个cell高度为准进行计算。
   * @default 'fixed'
   */
  cellHeightMode?: 'fixed' | 'dynamic'

  /**
   * 预加载的列表可视范围(列表高度)页数。此数值越大，则虚拟列表中加载的dom越多，内存消耗越大(会维持在一个稳定值)，但增加预加载页面数量可缓解快速滚动短暂白屏问题。
   * @default 12
   */
  preloadPage?: number | string

  /**
   * 虚拟列表列数，默认为1。常用于每行有多列的情况，例如每行有2列数据，需要将此值设置为2。
   * @default 1
   * @since 2.2.8
   */
  virtualListCol?: number | string

  /**
   * 虚拟列表scroll取样帧率，默认为80，过低容易出现白屏问题，过高容易出现卡顿问题
   * @default 80
   */
  virtualScrollFps?: number | string

  /**
   * 是否在z-paging内部循环渲染列表(使用内置列表)。
   * @default false
   */
  useInnerList?: boolean

  /**
   * 内置列表cell的key名称(仅nvue有效，在nvue中开启use-inner-list时必须填此项)
   * @since 2.2.7
   */
  cellKeyName?: string

  /**
   * innerList样式
   */
  innerListStyle?: Record<string, any>
}

// ****************************** Methods ******************************
declare interface _ZPagingSwiperItemRef {
  /**
   * 重新加载分页数据，pageNo恢复为默认值，相当于下拉刷新的效果
   *
   * @param [animate=false] 是否展示下拉刷新动画
   */
  reload: (animate?: boolean) => void;

  /**
   * 请求结束
   * - 当通过complete传进去的数组长度小于pageSize时，则判定为没有更多了
   *
   * @param [data] 请求结果数组
   * @param [success=true] 是否请求成功
   */
  complete: (data?: any[] | false, success?: boolean) => void;
}

declare interface _ZPagingSwiperItem {
  new (): {
    $props: AllowedComponentProps &
      VNodeProps &
      ZPagingSwiperItemProps
  }
}

export declare const ZPagingSwiperItem: _ZPagingSwiperItem

export declare const ZPagingSwiperItemRef: _ZPagingSwiperItemRef
