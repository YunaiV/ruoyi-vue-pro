import { AllowedComponentProps, VNodeProps } from './_common'

type _Arrayable<T> = T | T[];

/**
 * i18n配置信息
 */
type _I18nText = Partial<Record<'en' | 'zh-Hans' | 'zh-Hant', string>>;

declare global {
  namespace ZPagingEnums {
    /**
     * query的触发来源：user-pull-down:用户主动下拉刷新 reload:通过reload触发 refresh:通过refresh触发 load-more:通过滚动到底部加载更多或点击底部加载更多触发
     */
    type QueryFrom = 'user-pull-down' | 'reload' | 'refresh' | 'load-more';

    /**
     * 下拉刷新状态：default:默认状态 release-to-refresh:松手立即刷新 loading:刷新中 complete:刷新结束 go-f2:松手进入二楼
     */
    type RefresherStatus = 'default' | 'release-to-refresh' | 'loading' | 'complete' | 'go-f2';

    /**
     * 下拉进入二楼状态：	go:二楼开启 close:二楼关闭
     */
    type GoF2Status = 'go' | 'close';

    /**
     * 底部加载更多状态：default:默认状态 loading:加载中 no-more:没有更多数据 fail:加载失败
     */
    type LoadMoreStatus = 'default' | 'loading' | 'no-more' | 'fail';

    /**
     * 列表触摸的方向，top代表用户将列表向上移动(scrollTop不断减小)，bottom代表用户将列表向下移动(scrollTop不断增大)
     */
    type TouchDirection = 'top' | 'bottom';
  }

  namespace ZPagingParams {
    /**
     * z-paging返回数据
     *
     * @since 2.5.3
     */
    interface ReturnData<T> {
      /**
       * 总列表
       */
      totalList: T[];
      /**
       * 是否没有更多数据
       */
      noMore: boolean;
    }

    /**
     * 嵌套父容器信息 [list组件](https://uniapp.dcloud.net.cn/component/list.html)
     *
     * @since 2.0.4
     */
    interface SetSpecialEffectsArgs {
      /**
       * 和list同时滚动的组件id，应为外层的scroller
       */
      id?: string;
      /**
       * 要吸顶的header顶部距离scroller顶部的距离
       * - Android暂不支持
       *
       * @default 0
       */
      headerHeight?: number;
    }

    /**
     * touchmove信息
     */
    interface RefresherTouchmoveInfo {
      /** 下拉的距离 */
      pullingDistance: number;
      /** 前后两次回调滑动距离的差值 */
      dy: number;
      /** refresh组件高度 */
      viewHeight: number;
      /** pullingDistance/viewHeight的比值 */
      rate: number;
    }

    /**
     * 默认事件处理
     */
    type DefaultEventHandler = (value: boolean) => void;

    /**
     * 使用虚拟列表或内置列表时点击了cell的信息
     */
    interface InnerCellClickInfo {
      /** 当前点击的item */
      item: any;
      /** 当前点击的index */
      index: number;
    }

    /**
     * 键盘的高度信息
     */
    interface KeyboardHeightInfo {
      /** 键盘的高度 */
      height: number;
    }

    /**
     * 列表滚动信息(vue)
     */
    interface ScrollInfo {
      detail: {
          scrollLeft: number;
          scrollTop: number;
          scrollHeight: number;
          scrollWidth: number;
          deltaX: number;
          deltaY: number;
      }
    }
    /**
     * 列表滚动信息(nvue)
     */
    interface ScrollInfoN {
        contentSize: {
            width: number;
            height: number;
        };
        contentOffset: {
            x: number;
            y: number;
        };
        isDragging: boolean;
    }

    /**
     * 滚动结束时触发事件信息
     */
    interface ScrollendEvent {
      contentSize: {
          width: number;
          height: number;
      };
      contentOffset: {
          x: number;
          y: number;
      };
      isDragging: boolean;
    }

    /**
     * 下拉刷新Slot的props
     */
    interface RefresherSlotProps {
      /** 下拉刷新状态：default:默认状态 release-to-refresh:松手立即刷新 loading:刷新中 complete:刷新结束 go-f2:松手进入二楼 */
      refresherStatus: ZPagingEnums.RefresherStatus; 
    }

    /**
     * 空数据图Slot的props
     */
    interface EmptySlotProps {
      /** 是否加载失败: 加载失败，false: 加载成功) */
      isLoadFailed: boolean; 
    }

    /**
     * 虚拟列表&内置列表中cell Slot的props
     */
    interface InnerListCellSlotProps {
      /** 当前item */
      item: any; 
      /** 当前index */
      index: number; 
    }

    /**
     * 聊天记录加载中Slot的props
     */
    interface ChatLoadingSlotProps {
      /** 底部加载更多状态：default:默认状态 loading:加载中 no-more:没有更多数据 fail:加载失败 */
      loadingMoreStatus: ZPagingEnums.LoadMoreStatus; 
    }
  }

  /**
     * 虚拟列表数据项
     *
     * @since 2.7.7
     */
  type ZPagingVirtualItem<T> = T & {
    /**
     * 元素真实索引
     */
    zp_index: number;
  };
}

// ****************************** Props ******************************
declare interface ZPagingProps {
  // ******************** 数据&布局配置 ********************
  /**
   * 父组件v-model所绑定的list的值
   * @default []
   */
  value?: any[]

  /**
   * 自定义初始的pageNo(从第几页开始)
   * @default 1
   */
  defaultPageNo?: number

  /**
   * 自定义pageSize(每页显示多少条)
   * - 必须和后端的pageSize一致，例如后端分页的pageSize为15，此属性必须改为15
   * @default 10
   */
  defaultPageSize?: number

  /**
   * z-paging是否使用fixed布局
   * - 若使用fixed布局，则z-paging的父view无需固定高度，z-paging高度默认铺满屏幕，页面中的view请放在z-paging标签内，需要固定在顶部的view使用slot="top"包住，需要固定在底部的view使用slot="bottom"包住
   * @default true
   * @since 1.5.6
   */
  fixed?: boolean

  /**
   * 是否开启底部安全区域适配
   * @default false
   * @since 1.6.1
   */
  safeAreaInsetBottom?: boolean

  /**
   * 开启底部安全区域适配后，是否使用placeholder形式实现。
   * - 为否时滚动区域会自动避开底部安全区域，也就是所有滚动内容都不会挡住底部安全区域，若设置为是，则滚动时滚动内容会挡住底部安全区域，但是当滚动到底部时才会避开底部安全区域
   * @default false
   * @since 2.2.7
   */
  useSafeAreaPlaceholder?: boolean

  /**
   * 使用页面滚动，默认为否。当设置为是时，则使用页面的滚动而非此组件内部的scroll-view的滚动。
   * @default false
   */
  usePageScroll?: boolean

  /**
   * 使用页面滚动时，是否在不满屏时自动填充满屏幕
   * @default true
   * @since 2.0.6
   */
  autoFullHeight?: boolean

  /**
   * loading(下拉刷新、上拉加载更多)的主题样式，支持black，white
   * @default 'black'
   */
  defaultThemeStyle?: 'black' | 'white'

  /**
   * 设置z-paging的style，部分平台(如微信小程序)无法直接修改组件的style，可使用此属性代替。
   */
  pagingStyle?: Record<string, any>

  /**
   * z-paging的高度，优先级低于paging-style中设置的height
   * - 传字符串，如100px、100rpx、100%
   */
  height?: string

  /**
   * z-paging的宽度，优先级低于paging-style中设置的width
   * - 传字符串，如100px、100rpx、100%
   */
  width?: string

  /**
   * z-paging的最大宽度，优先级低于paging-style中设置的max-width
   * - 默认为空，也就是铺满窗口宽度，若设置了特定值则会自动添加margin: 0 auto
   */
  maxWidth?: string

  /**
   * z-paging的背景色(为css中的background，因此也可以设置渐变，背景图片等)，优先级低于paging-style中设置的background-color。
   * - 传字符串，如"#ffffff"
   */
  bgColor?: string

  /**
   * 是否监听列表触摸方向改变
   * @default false
   * @since 2.3.0
   */
  watchTouchDirectionChange?: boolean

  /**
   * 调用complete后延迟处理的时间，单位为毫秒，优先级高于min-delay
   * @default 0
   * @since 1.9.6
   */
  delay?: number | string

  /**
   * 触发@query后最小延迟处理的时间，单位为毫秒，优先级低于delay
   * @default 0
   * @since 2.0.9
   */
  minDelay?: number | string

  /**
   * 请求失败是否触发reject
   * @default true
   * @since 2.6.1
   */
  callNetworkReject?: boolean

  /**
   * z-paging中默认布局的单位
   * @default rpx
   * @since 2.6.7
   */
  unit?: 'rpx' | 'px'

  /**
   * 自动拼接complete中传过来的数组
   * - 若设置为false，则complete中传过来的数组会直接覆盖list
   * @default true
   * @since 1.8.8
   */
  concat?: boolean

  /**
   * 为保证数据一致，设置当前tab切换时的标识key，并在complete中传递相同key，若二者不一致，则complete将不会生效
   * @since 1.6.4
   */
  dataKey?: number | string | Record<string, any>

  /**
   * 【极简写法】自动注入的list名，可自动修改父view(包含ref="paging")中对应name的list值
   * - z-paging标签必须设置ref="paging"
   * @since 1.8.5
   */
  autowireListName?: string

  /**
   * 【极简写法】自动注入的query名，可自动调用父view(包含ref="paging")中的query方法
   * - z-paging标签必须设置ref="paging"
   * @since 1.8.5
   */
  autowireQueryName?: string

  /**
   * 获取分页数据Function，功能与@query类似。若设置了fetch则@query将不再触发。
   * @since 2.7.8
   */
  fetch?: (...args: any[]) => any;

  /**
   * fetch的附加参数，fetch配置后有效
   * @since 2.7.8
   */
  fetchParams?: Record<string, any>

  // ******************** reload相关配置 ********************
  /**
   * z-paging mounted 后自动调用 reload 方法 (mounted 后自动调用接口)
   * @default true
   * @since 2.4.3
   */
  auto?: boolean

  /**
   * reload 时自动滚动到顶部
   * - 如果reload时list被清空导致占位消失，也可能会自动返回到顶部，因此如果是这种情况还需要将autoCleanListWhenReload设置为false
   * @default true
   */
  autoScrollToTopWhenReload?: boolean

  /**
   * reload时立即自动清空原list若立即自动清空，则在reload之后、请求回调之前页面是空白的
   * @default true
   */
  autoCleanListWhenReload?: boolean

  /**
   * 列表刷新时自动显示下拉刷新 view
   * @default false
   * @since 1.7.2
   */
  showRefresherWhenReload?: boolean

  /**
   * 列表刷新时自动显示加载更多view，且为加载中状态 (仅初始设置有效，不可动态修改)
   * @default false
   * @since 1.7.2
   */
  showLoadingMoreWhenReload?: boolean

  /**
   * 组件 created 时立即触发 reload (可解决一些情况下先看到页面再看到 loading 的问题)
   * - auto 为 true 时有效。为否时将在 mounted+nextTick 后触发 reload
   * @default false
   * @since 2.2.3
   */
  createdReload?: boolean

  // ******************** 下拉刷新配置 ********************
  /**
   * 是否开启下拉刷新
   * @default true
   */
  refresherEnabled?: boolean

  /**
   * 设置自定义下拉刷新阈值，默认单位为px。支持传100、"100px"或"100rpx"
   * - nvue无效
   * @default '80rpx'
   */
  refresherThreshold?: number | string

  /**
   * 是否开启下拉刷新状态栏占位，适用于隐藏导航栏时，下拉刷新需要避开状态栏高度的情况
   * @default false
   * @since 2.6.1
   */
  useRefresherStatusBarPlaceholder?: boolean

  /**
   * 是否只使用下拉刷新
   * - 设置为true后将关闭mounted自动请求数据、关闭滚动到底部加载更多，强制隐藏空数据图
   * @default false
   */
  refresherOnly?: boolean

  /**
   * 是否使用自定义的下拉刷新，默认为是，即使用z-paging的下拉刷新
   * - 设置为false即代表使用uni scroll-view自带的下拉刷新，h5、App、微信小程序以外的平台不支持uni scroll-view自带的下拉刷新
   * @default true
   */
  useCustomRefresher?: boolean

  /**
   * 用户下拉刷新时是否触发reload方法
   * @default true
   */
  reloadWhenRefresh?: boolean

  /**
   * 下拉刷新的主题样式，支持black，white
   * @default 'black'
   */
  refresherThemeStyle?: string

  /**
   * 自定义下拉刷新中左侧图标的样式
   */
  refresherImgStyle?: Record<string, any>

  /**
   * 自定义下拉刷新中右侧状态描述文字的样式
   */
  refresherTitleStyle?: Record<string, any>

  /**
   * 自定义下拉刷新中右侧最后更新时间文字的样式
   * - show-refresher-update-time为true时有效
   */
  refresherUpdateTimeStyle?: Record<string, any>

  /**
   * 是否实时监听下拉刷新中进度，并通过@refresherTouchmove传递给父组件
   * @default false
   * @since 2.1.0
   */
  watchRefresherTouchmove?: boolean

  /**
   * 是否显示最后更新时间
   * @default false
   * @since 1.6.7
   */
  showRefresherUpdateTime?: boolean

  /**
   * 自定义下拉刷新默认状态下的文字
   * - 支持直接传字符串或形如：{'en':'英文','zh-Hans':'简体中文','zh-Hant':'繁体中文'}的i18n配置
   * @default '继续下拉刷新'
   */
  refresherDefaultText?: string | _I18nText

  /**
   * 自定义下拉刷新松手立即刷新状态下的文字
   * - 支持直接传字符串或形如：{'en':'英文','zh-Hans':'简体中文','zh-Hant':'繁体中文'}的i18n配置
   * @default '松开立即刷新'
   */
  refresherPullingText?: string | _I18nText

  /**
   * 自定义下拉刷新刷新中状态下的文字
   * - 支持直接传字符串或形如：{'en':'英文','zh-Hans':'简体中文','zh-Hant':'繁体中文'}的i18n配置
   * @default '正在刷新...'
   */
  refresherRefreshingText?: string | _I18nText

  /**
   * 自定义下拉刷新刷新结束状态下的文字
   * - 支持直接传字符串或形如：{'en':'英文','zh-Hans':'简体中文','zh-Hant':'繁体中文'}的i18n配置
   * @default '刷新成功'
   * @since 2.0.6
   */
  refresherCompleteText?: string | _I18nText

  /**
   * 自定义下拉刷新默认状态下的图片
   */
  refresherDefaultImg?: string

  /**
   * 自定义下拉刷新松手立即刷新状态下的图片
   */
  refresherPullingImg?: string

  /**
   * 自定义下拉刷新刷新中状态下的图片
   */
  refresherRefreshingImg?: string

  /**
   * 自定义下拉刷新刷新结束状态下的图片
   */
  refresherCompleteImg?: string

  /**
   * 自定义下拉刷新刷新中状态下是否展示旋转动画
   * @default true
   * @since 2.5.8
   */
  refresherRefreshingAnimated?: boolean

  /**
   * 是否开启自定义下拉刷新刷新结束回弹动画效果
   * @default true
   */
  refresherEndBounceEnabled?: boolean

  /**
   * 设置系统下拉刷新默认样式，支持设置 black，white，none
   * @default 'black'
   */
  refresherDefaultStyle?: string

  /**
   * 设置自定义下拉刷新区域背景颜色
   * @default '#FFFFFF00'
   */
  refresherBackground?: string

  /**
   * 设置固定的自定义下拉刷新区域背景颜色
   * @default '#FFFFFF00'
   */
  refresherFixedBackground?: string

  /**
   * 设置固定的自定义下拉刷新区域高度
   * @default 0
   */
  refresherFixedBacHeight?: number | string

  /**
   * 设置自定义下拉刷新默认状态下回弹动画时间，单位为毫秒
   * @default 100
   * @since 2.3.1
   */
  refresherDefaultDuration?: number | string

  /**
   * 自定义下拉刷新结束以后延迟收回的时间，单位为毫秒
   * @default 0
   * @since 2.0.6
   */
  refresherCompleteDelay?: number | string

  /**
   * 自定义下拉刷新结束收回动画时间，单位为毫秒
   * @default 300
   * @since 2.0.6
   */
  refresherCompleteDuration?: number | string

  /**
   * 下拉刷新时下拉到“松手立即刷新”状态时是否使手机短振动
   * @default false
   * @since 2.4.7
   */
  refresherVibrate?: boolean

  /**
   * 自定义下拉刷新刷新中状态是否允许列表滚动
   * @default true
   */
  refresherRefreshingScrollable?: boolean

  /**
   * 自定义下拉刷新结束状态下是否允许列表滚动
   * @default false
   * @since 2.1.1
   */
  refresherCompleteScrollable?: boolean

  /**
   * 设置自定义下拉刷新下拉超出阈值后继续下拉位移衰减的比例
   * @default 0.65
   */
  refresherOutRate?: number

  /**
   * 是否开启下拉进入二楼功能
   * @default false
   * @since 2.7.7
   */
  refresherF2Enabled?: boolean

  /**
   * 下拉进入二楼阈值
   * @default '200rpx'
   * @since 2.7.7
   */
  refresherF2Threshold?: number | string

  /**
   * 下拉进入二楼动画时间，单位为毫秒
   * @default 200
   * @since 2.7.7
   */
  refresherF2Duration?: number | string

  /**
   * 下拉进入二楼状态松手后是否弹出二楼
   * @default true
   * @since 2.7.7
   */
  showRefresherF2?: boolean

  /**
   * 设置自定义下拉刷新下拉时实际下拉位移与用户下拉距离的比值
   * @default 0.75
   * @since 2.3.7
   */
  refresherPullRate?: number

  /**
   * 自定义下拉刷新下拉帧率，默认为40，过高可能会出现抖动问题
   * @default 40
   */
  refresherFps?: number | string

  /**
   * 自定义下拉刷新允许触发的最大下拉角度，默认为40度
   * - 值小于0或大于90时，代表不受角度限
   * @default 40
   * @since 2.5.8
   */
  refresherMaxAngle?: number | string

  /**
   * 自定义下拉刷新的角度由未达到最大角度变到达到最大角度时，是否继续下拉刷新手势
   * @default false
   */
  refresherAngleEnableChangeContinued?: boolean

  /**
   * 下拉刷新时是否禁止下拉刷新view跟随用户触摸竖直移动
   * @default false
   * @since 2.5.8
   */
  refresherNoTransform?: boolean

  // ******************** 底部加载更多配置 ********************
  /**
   * 是否启用加载更多数据(含滑动到底部加载更多数据和点击加载更多数据)
   * @default true
   */
  loadingMoreEnabled?: boolean

  /**
   * 距底部/右边多远时，触发 scrolltolower 事件，默认单位为px
   * - 支持传100、"100px"或"100rpx"
   * @default '100rpx'
   */
  lowerThreshold?: number | string

  /**
   * 是否启用滑动到底部加载更多数据
   * @default true
   */
  toBottomLoadingMoreEnabled?: boolean

  /**
   * 底部加载更多的主题样式，支持black，white
   * @default 'black'
   */
  loadingMoreThemeStyle?: string

  /**
   * 自定义底部加载更多样式；如：{'background':'red'} 
   * - 此属性无法修改文字样式，修改文字样式请使用loading-more-title-custom-style
   */
  loadingMoreCustomStyle?: Record<string, any>

  /**
   * 自定义底部加载更多文字样式；如：{'color':'red'}
   * @since 2.1.7
   */
  loadingMoreTitleCustomStyle?: Record<string, any>

  /**
   * 自定义底部加载更多加载中动画样式
   */
  loadingMoreLoadingIconCustomStyle?: Record<string, any>

  /**
   * 自定义底部加载更多加载中动画图标类型
   * - 可选flower或circle，默认为flower (nvue不支持)
   * @default 'flower'
   */
  loadingMoreLoadingIconType?: 'flower' | 'circle'

  /**
   * 自定义底部加载更多加载中动画图标图片
   * - 若设置则使用自定义的动画图标，loading-more-loading-icon-type将无效 (nvue无效)
   */
  loadingMoreLoadingIconCustomImage?: string

  /**
   * 底部加载更多加载中view是否展示旋转动画
   * - loading-more-loading-icon-custom-image有值时有效，nvue无效
   * @default true
   * @since 1.9.4
   */
  loadingMoreLoadingAnimated?: boolean

  /**
   * 滑动到底部"默认"文字
   * - 支持直接传字符串或形如：{'en':'英文','zh-Hans':'简体中文','zh-Hant':'繁体中文'}的i18n配置
   * @default '点击加载更多'
   */
  loadingMoreDefaultText?: string | _I18nText

  /**
   * 滑动到底部"加载中"文字 
   * - 支持直接传字符串或形如：{'en':'英文','zh-Hans':'简体中文','zh-Hant':'繁体中文'}的i18n配置
   * @default '正在加载...'
   */
  loadingMoreLoadingText?: string | _I18nText

  /**
   * 滑动到底部"没有更多"文字 
   * - 支持直接传字符串或形如：{'en':'英文','zh-Hans':'简体中文','zh-Hant':'繁体中文'}的i18n配置
   * @default '没有更多了'
   */
  loadingMoreNoMoreText?: string | _I18nText

  /**
   * 滑动到底部"加载失败"文字 
   * - 支持直接传字符串或形如：{'en':'英文','zh-Hans':'简体中文','zh-Hant':'繁体中文'}的i18n配置
   * @default '加载失败，点击重新加载'
   */
  loadingMoreFailText?: string | _I18nText

  /**
   * 当没有更多数据且分页内容未超出z-paging时是否隐藏没有更多数据的view 
   * - nvue不支持，nvue中请使用hide-no-more-by-limit控制
   * @default false
   * @since 2.4.3
   */
  hideNoMoreInside?: boolean

  /**
   * 当没有更多数据且分页数组长度少于这个值时，隐藏没有更多数据的view
   * - 默认为0，代表不限制。此属性优先级高于`hide-no-more-inside`
   * @default 0
   * @since 2.4.3
   */
  hideNoMoreByLimit?: number

  /**
   * 当分页未满一屏时，是否自动加载更多 (nvue无效)
   * @default false
   * @since 2.0.0
   */
  insideMore?: boolean

  /**
   * 滑动到底部状态为默认状态时，以加载中的状态展示
   * - 若设置为是，可避免滚动到底部看到默认状态然后立刻变为加载中状态的问题，但分页数量未超过一屏时，不会显示【点击加载更多】
   * @default false
   * @since 2.2.0
   */
  loadingMoreDefaultAsLoading?: boolean

  /**
   * 是否显示没有更多数据的view
   * @default true
   */
  showLoadingMoreNoMoreView?: boolean

  /**
   * 是否显示默认的加载更多text
   * @default true
   */
  showDefaultLoadingMoreText?: boolean

  /**
   * 是否显示没有更多数据的分割线，默认为是
   * @default true
   */
  showLoadingMoreNoMoreLine?: boolean

  /**
   * 自定义底部没有更多数据的分割线样式
   */
  loadingMoreNoMoreLineCustomStyle?: Record<string, any>

  // ******************** 空数据与加载失败配置 ********************
  /**
   * 是否强制隐藏空数据图
   * @default false
   */
  hideEmptyView?: boolean

  /**
   * 空数据图片是否铺满z-paging
   * - 默认为否，即填充满z-paging内列表(滚动区域)部分。若设置为否，则为填铺满整个z-paging
   * @default false
   * @since 2.0.3
   */
  emptyViewFixed?: boolean

  /**
   * 空数据图片是否垂直居中
   * - 默认为是，若设置为否即为从空数据容器顶部开始显示 (empty-view-fixed为false时有效)
   * @default true
   * @since 2.0.6
   */
  emptyViewCenter?: boolean

  /**
   * 空数据图描述文字 
   * - 支持直接传字符串或形如：{'en':'英文','zh-Hans':'简体中文','zh-Hant':'繁体中文'}的i18n配置
   * @default '没有数据哦~'
   */
  emptyViewText?: string | _I18nText

  /**
   * 空数据图图片，默认使用z-paging内置的图片
   * - 建议使用绝对路径，开头不要添加"@"，请以"/"开头
   */
  emptyViewImg?: string

  /**
   * 空数据图“加载失败”图片，默认使用z-paging内置的图片
   * - 建议使用绝对路径，开头不要添加"@"，请以"/"开头
   * @since 1.6.7
   */
  emptyViewErrorImg?: string

  /**
   * 空数据图点击重新加载文字 
   * - 支持直接传字符串或形如：{'en':'英文','zh-Hans':'简体中文','zh-Hant':'繁体中文'}的i18n配置
   * @default '重新加载'
   * @since 1.6.7
   */
  emptyViewReloadText?: string | _I18nText

  /**
   * 空数据图“加载失败”描述文字 
   * - 支持直接传字符串或形如：{'en':'英文','zh-Hans':'简体中文','zh-Hant':'繁体中文'}的i18n配置
   * @default '很抱歉，加载失败'
   * @since 1.6.7
   */
  emptyViewErrorText?: string | _I18nText
  /**
   * 空数据图父view样式
   */
  emptyViewSuperStyle?: Record<string, any>

  /**
   * 空数据图样式，可设置空数据view的top等，如：:empty-view-style="{'top':'100rpx'}"
   */
  emptyViewStyle?: Record<string, any>

  /**
   * 空数据图img样式
   */
  emptyViewImgStyle?: Record<string, any>

  /**
   * 空数据图描述文字样式
   */
  emptyViewTitleStyle?: Record<string, any>

  /**
   * 空数据图重新加载按钮样式
   * @since 1.6.7
   */
  emptyViewReloadStyle?: Record<string, any>

  /**
   * 是否显示空数据图重新加载按钮(无数据时)
   * @default false
   * @since 1.6.7
   */
  showEmptyViewReload?: boolean

  /**
   * 加载失败时是否显示空数据图重新加载按钮
   * @default true
   * @since 1.6.7
   */
  showEmptyViewReloadWhenError?: boolean

  /**
   * 加载中时是否自动隐藏空数据图
   * @default true
   */
  autoHideEmptyViewWhenLoading?: boolean

  /**
   * 用户下拉列表触发下拉刷新加载中时是否自动隐藏空数据图
   * @default true
   * @since 2.0.9
   */
  autoHideEmptyViewWhenPull?: boolean

  // ******************** 全屏Loading配置 ********************
  /**
   * 第一次加载后自动隐藏loading slot
   * @default true
   */
  autoHideLoadingAfterFirstLoaded?: boolean

  /**
   * loading slot的父view是否铺满屏幕并固定
   * - 设置为true后，插入的loading的父view会铺满全屏。注意：插入的loading需要设置height:100%（nvue为flex:1）才可铺满全屏。loading内的view从导航栏顶部开始，会被导航栏盖住，请妥善处理。
   * @default false
   * @since 2.0.9
   */
  loadingFullFixed?: boolean

  /**
   * 是否自动显示系统Loading：即uni.showLoading
   * - 若开启则将在刷新列表时（调用reload、refresh时）显示。下拉刷新和滚动到底部加载更多不会显示。
   * @default false
   * @since 2.3.7
   */
  autoShowSystemLoading?: boolean

  /**
   * 显示系统Loading时显示的文字
   * - 支持直接传字符串或形如：{'en':'英文','zh-Hans':'简体中文','zh-Hant':'繁体中文'}的i18n配置
   * @default '加载中...'
   * @since 2.3.7
   */
  systemLoadingText?: string | _I18nText

  /**
   * 显示系统Loading时是否显示透明蒙层，防止触摸穿透。H5、App、微信小程序、百度小程序有效。
   * @default true
   * @since 2.3.9
   */
  systemLoadingMask?: boolean

  // ******************** 返回顶部按钮配 ********************
  /**
   * 自动显示点击返回顶部按钮
   * @default false
   */
  autoShowBackToTop?: boolean

  /**
   * 点击返回顶部按钮显示/隐藏的阈值(滚动距离)，默认单位为px
   * - 支持传100、"100px"或"100rpx"
   * @default 400rpx
   */
  backToTopThreshold?: number | string

  /**
   * 点击返回顶部按钮的自定义图片地址
   * - 建议使用绝对路径，开头不要添加"@"，请以"/"开头
   * @default 'z-paging内置的图片'
   */
  backToTopImg?: string

  /**
   * 点击返回顶部按钮返回到顶部时是否展示过渡动画
   * @default true
   */
  backToTopWithAnimate?: boolean

  /**
   * 点击返回顶部按钮与底部的距离，默认单位为px
   * - 支持传100、"100px"或"100rpx"
   * @default 160rpx
   */
  backToTopBottom?: number | string

  /**
   * 点击返回顶部按钮的自定义样式
   */
  backToTopStyle?: Record<string, any>

  // ******************** 虚拟列表&内置列表配置 ********************
  /**
   * 是否使用虚拟列表
   * - 使用页面滚动或nvue时，不支持虚拟列表。在nvue中z-paging内置了list组件，效果与虚拟列表类似，并且可以提供更好的性能
   * @default false
   */
  useVirtualList?: boolean

  /**
   * 在使用虚拟列表时，是否使用兼容模式。兼容模式写法较繁琐，但可提供良好的兼容性。
   * @default false
   * @since 2.4.0
   */
  useCompatibilityMode?: boolean

  /**
   * 使用兼容模式时传递的附加数据，可选、非必须
   * @since 2.4.0
   */
  extraData?: Record<string, any>

  /**
   * 虚拟列表cell高度模式，默认为fixed，也就是每个cell高度完全相同，将以第一个cell高度为准进行计算。
   * @default 'fixed'
   */
  cellHeightMode?: 'fixed' | 'dynamic'

  /**
   * 预加载的列表可视范围(列表高度)页数。此数值越大，则虚拟列表中加载的dom越多，内存消耗越大(会维持在一个稳定值)，但增加预加载页面数量可缓解快速滚动短暂白屏问题。
   * @default 12
   */
  preloadPage?: number | string

  /**
   * 固定的cell高度，`cell-height-mode=fixed`才有效，若设置了值，则不计算第一个cell高度而使用设置的cell高度。默认单位为px
   * - 支持传100、"100px"或"100rpx"
   * @since 2.7.8
   */
  fixedCellHeight?: number | string

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
   * 虚拟列表cell id的前缀，适用于一个页面有多个虚拟列表的情况，用以区分不同虚拟列表cell的id
   * - 注意：请勿传数字或以数字开头的字符串。如设置为list1，则cell的id应为：list1-zp-id-${item.zp_index}
   * @since 2.8.1
   */
  virtualCellIdPrefix?: string

  /**
   * 是否在z-paging内部循环渲染列表(使用内置列表)。
   * @default false
   */
  useInnerList?: boolean

  /**
   * 强制关闭inner-list。适用于开启了虚拟列表后需要强制关闭inner-list的情况。
   * @default false
   * @since 2.2.7
   */
  forceCloseInnerList?: boolean

  /**
   * 虚拟列表是否使用swiper-item包裹，默认为否，此属性为了解决vue3+(微信小程序或QQ小程序)中，使用非内置列表写法时，若z-paging在swiper-item内存在无法获取slot插入的cell高度进而导致虚拟列表失败的问题
   * - 仅vue3+(微信小程序或QQ小程序)+非内置列表写法虚拟列表有效，其他情况此属性设置任何值都无效，所以如果您在swiper-item内使用z-paging的非内置虚拟列表写法，将此属性设置为true即可
   * @default false
   * @since 2.8.6
   */
  virtualInSwiperSlot?: boolean

  /**
   * 内置列表cell的key名称(仅nvue有效，在nvue中开启use-inner-list时必须填此项)
   * @since 2.2.7
   */
  cellKeyName?: string

  /**
   * innerList样式
   */
  innerListStyle?: Record<string, any>

  /**
   * innerCell样式
   * @since 2.2.8
   */
  innerCellStyle?: Record<string, any>

  // ******************** 本地分页配置 ********************
  /**
   * 本地分页时上拉加载更多延迟时间，单位为毫秒
   * @default 200
   */
  localPagingLoadingTime?: number | string

  // ******************** 聊天记录模式配置 ********************
  /**
   * 使用聊天记录模式，为保证良好的体验。
   * @default false
   */
  useChatRecordMode?: boolean;

  /**
   * 使用聊天记录模式时是否自动隐藏键盘(在用户触摸列表时候自动隐藏键盘)
   * @default true
   * @since 2.3.4
   */
  autoHideKeyboardWhenChat?: boolean;

  /**
   * 使用聊天记录模式中键盘弹出时是否自动调整slot="bottom"高度
   * @default true
   * @since 2.7.4
   */
  autoAdjustPositionWhenChat?: boolean;

  /**
   * 使用聊天记录模式中键盘弹出时是否自动滚动到底部
   * @default false
   * @since 2.7.4
   */
  autoToBottomWhenChat?: boolean;

  /**
   * 使用聊天记录模式中键盘弹出时占位高度偏移距离。默认0rpx。单位px
   * @default "0px"
   * @since 2.7.6
   */
  chatAdjustPositionOffset?: string;

  /**
   * 使用聊天记录模式中`reload`时是否显示`chatLoading`
   * @default false
   * @since 2.7.4
   */
  showChatLoadingWhenReload?: boolean;

  /**
   * `bottom`的背景色，默认透明，传字符串，如"#ffffff"
   * @since 2.7.4
   */
  bottomBgColor?: string;

  /**
   * 在聊天记录模式中滑动到顶部状态为默认状态时，是否以加载中的状态展示
   * @default true
   * @since 2.7.5
   */
  chatLoadingMoreDefaultAsLoading?: boolean;

  // ******************** scroll-view相关配置 ********************
  /**
   * 控制是否出现滚动条
   * @default true
   */
  showScrollbar?: boolean;

  /**
   * 是否可以滚动，使用内置scroll-view和nvue时有效
   * @default true
   */
  scrollable?: boolean;

  /**
   * 是否允许横向滚动
   * @default false
   * @since 2.0.6
   */
  scrollX?: boolean;

  /**
   * iOS设备上滚动到顶部时是否允许回弹效果
   * - 关闭回弹效果后可使滚动到顶部后立即下拉可立即触发下拉刷新，但是有吸顶view时滚动到顶部时可能出现抖动
   * @default false
   */
  scrollToTopBounceEnabled?: boolean;

  /**
   * iOS设备上滚动到底部时是否允许回弹效果。可能会导致使用scroll-view滚动时无法顺利滚动到底部
   * @default true
   */
  scrollToBottomBounceEnabled?: boolean;

  /**
   * 在设置滚动条位置时使用动画过渡
   * @default false
   */
  scrollWithAnimation?: boolean;

  /**
   * 值应为某子元素id（id不能以数字开头）。设置哪个方向可滚动，则在哪个方向滚动到该元素。若在一些平台中无效，可以通过调用z-paging的scrollToxxx系列的方法实现。
   */
  scrollIntoView?: string;

  /**
   * iOS点击顶部状态栏、安卓双击标题栏时，滚动条返回顶部。仅支持app-nvue，微信小程序。
   * @default true
   */
  enableBackToTop?: boolean;

  // ******************** nvue独有配置 ********************
  /**
   * nvue中修改列表类型。
   * @default "list"
   */
  nvueListIs?: 'list' | 'waterfall' | 'scroller';

  /**
   * waterfall 配置，仅在 nvue 中且 nvueListIs=waterfall 时有效。示例: {'column-gap': 20}。配置参数详情参见: https://uniapp.dcloud.io/component/waterfall
   */
  nvueWaterfallConfig?: Record<string, any>;

  /**
   * nvue 控制是否回弹效果，iOS 不支持动态修改。注意: 若禁用回弹效果，下拉刷新将失效。
   * @default true
   */
  nvueBounce?: boolean;

  /**
   * nvue 中通过代码滚动到顶部/底部时，是否加快动画效果(无滚动动画时无效)。
   * @default false
   * @since 1.9.4
   */
  nvueFastScroll?: boolean;

  /**
   * nvue 中 list 的 id。
   * @since 2.0.4
   */
  nvueListId?: string;

  /**
   * 是否隐藏 nvue 列表底部的 tagView，此 view 用于标识滚动到底部位置。
   * - 若隐藏则滚动到底部功能将失效。
   * - 在 nvue 中实现吸顶+swiper 功能时需将最外层 z-paging 的此属性设置为 true。
   * @default false
   * @since 2.0.4
   */
  hideNvueBottomTag?: boolean;

  /**
   * 设置 nvue 中是否按分页模式(类似竖向 swiper)显示 List。
   * @default false
   * @since 2.3.1
   */
  nvuePagingEnabled?: boolean;

  /**
   * nvue 中控制 onscroll 事件触发的频率。表示两次 onscroll 事件之间列表至少滚动了指定的像素值。单位: px。
   * @since 2.3.5
   */
  offsetAccuracy?: number;

  // ******************** 缓存配置 ********************
  /**
   * 是否使用缓存。若开启，将自动缓存第一页的数据。注意：默认仅会缓存组件首次加载时第一次请求到的数据，后续的下拉刷新操作不会更新缓存。
   * - 必须设置 `cacheKey`，否则缓存无效。
   * @default false
   */
  useCache?: boolean;

  /**
   * 使用缓存时缓存的 key，用于区分不同列表的缓存数据。
   * - useCache为 true 时必须设置，否则缓存无效。
   */
  cacheKey?: string;

  /**
   * 缓存模式。
   * - default: 仅缓存组件首次加载时第一次请求到的数据。
   * - always: 总是缓存，每次列表刷新(如下拉刷新、调用 reload 等)都会更新缓存。
   * @default "default"
   */
  cacheMode?: 'default' | 'always';

  // ******************** z-index配置 ********************
  /**
   * slot="top" 的 view 的 z-index。
   * - 仅使用页面滚动时有效。
   * @default 99
   */
  topZIndex?: number;

  /**
   * z-paging 内容容器父 view 的 z-index。
   * @default 1
   */
  superContentZIndex?: number;

  /**
   * z-paging 内容容器部分的 z-index。
   * @default 1
   */
  contentZIndex?: number;

  /**
   * 空数据 view 的 z-index。
   * @default 9
   */
  emptyViewZIndex?: number;

  // ******************** 其他配置 ********************
  /**
   * z-paging 是否自动高度。
   * - 自动高度时会自动铺满屏幕，不需要设置父 view 为 100% 等操作。
   * - 注意：自动高度可能并不准确。
   * @deprecated 建议使用 fixed 代替。
   * @default false
   */
  autoHeight?: boolean;

  /**
   * z-paging 自动高度时的附加高度。
   * - 添加单位 px 或 rpx，默认为 px。
   * - 若需要减少高度，请传负数，如 "-10rpx" 或 "10.5px"。
   * @deprecated 建议使用 fixed 代替。
   * @default "0px"
   */
  autoHeightAddition?: number | string;


  // ****************************** Events ******************************

  // ******************** 数据处理相关事件 ********************
  /**
   * 父组件v-model所绑定的list的值改变时触发此事件
   * @param value 列表数据
   */
  onInput?: (value: any[]) => any
  
  /**
   * 下拉刷新或滚动到底部时会自动触发此方法。z-paging加载时也会触发(若要禁止，请设置:auto="false")。pageNo和pageSize会自动计算好，直接传给服务器即可。
   * @param pageNo 当前第几页
   * @param pageSize 每页多少条
   * @param from query的触发来源：user-pull-down:用户主动下拉刷新 reload:通过reload触发 refresh:通过refresh触发 load-more:通过滚动到底部加载更多或点击底部加载更多触发
   */
  onQuery?: (pageNo: number, pageSize: number, from: ZPagingEnums.QueryFrom) => void

  /**
   * 分页渲染的数组改变时触发
   * @param list 最终的分页数据数组
   */
  onListChange?: (list: any[]) => void

  // ******************** 下拉刷新相关事件 ********************
  /**
   * 自定义下拉刷新状态改变
   * - use-custom-refresher为false时无效
   * @param status 下拉刷新状态：default:默认状态 release-to-refresh:松手立即刷新 loading:刷新中 complete:刷新结束 go-f2:松手进入二楼
   */
  onRefresherStatusChange?: (status: ZPagingEnums.RefresherStatus) => void

  /**
   * 自定义下拉刷新下拉开始
   * - use-custom-refresher为false时无效
   * @param y 当前触摸开始的屏幕点的y值(单位px)
   */
  onRefresherTouchstart?: (y: number) => void
  
  /**
   * 自定义下拉刷新下拉拖动中
   * - use-custom-refresher为false时无效
   * - 在使用wxs的平台上，为减少wxs与js通信折损，只有在z-paging添加@refresherTouchmove时，wxs才会实时将下拉拖动事件传给js，在微信小程序和QQ小程序中，因$listeners无效，所以必须设置:watch-refresher-touchmove="true"方可使此事件被触发
   * @param info touchmove信息
   */
  onRefresherTouchmove?: (info: ZPagingParams.RefresherTouchmoveInfo) => void

  /**
   * 自定义下拉刷新下拉结束
   * - use-custom-refresher为false时无效
   * @param y 当前触摸开始的屏幕点的y值(单位px)
   */
  onRefresherTouchend?: (y: number) => void

  /**
   * 下拉进入二楼状态改变
   * - use-custom-refresher为false时无效
   * @param y 当前触摸开始的屏幕点的y值(单位px)
   * @since 2.7.7
   */
  onRefresherF2Change?: (status: ZPagingEnums.GoF2Status) => void

  /**
   * 自定义下拉刷新被触发
   */
  onRefresh?: () => void

  /**
   * 自定义下拉刷新被复位
   */
  onRestore?: () => void

  // ******************** 底部加载更多相关事件 ********************
  /**
   * 自定义下拉刷新状态改变
   * - use-custom-refresher为false时无效
   * @param status 底部加载更多状态：default:默认状态 loading:加载中 no-more:没有更多数据 fail:加载失败
   */
  onLoadingStatusChange?: (status: ZPagingEnums.LoadMoreStatus) => void

  // ******************** 空数据与加载失败相关事件 ********************
  /**
   * 点击了空数据图中的重新加载按钮
   * @param handler 点击空数据图中重新加载后是否进行reload操作，默认为是。如果需要禁止reload事件，则调用handler(false)
   * @since 1.8.0
   */
  onEmptyViewReload?: (handler: ZPagingParams.DefaultEventHandler) => void

  /**
   * 点击了空数据图view
   * @since 2.3.3
   */
  onEmptyViewClick?: () => void

  /**
   * z-paging请求失败状态改变
   * @param isLoadFailed 当前是否是请求失败状态，为true代表是，反之为否；默认状态为否
   * @since 2.5.0
   */
  onIsLoadFailedChange?: (isLoadFailed: boolean) => void

  // ******************** 返回顶部按钮相关事件 ********************
  /**
   * 点击了返回顶部按钮
   * @param handler 点击返回顶部按钮后是否滚动到顶部，默认为是。如果需要禁止滚动到顶部事件，则调用handler(false)
   * @since 2.6.1
   */
  onBackToTopClick?: (handler: ZPagingParams.DefaultEventHandler) => void

  // ******************** 虚拟列表&内置列表相关事件 ********************
  /**
   * 虚拟列表当前渲染的数组改变时触发，在虚拟列表中只会渲染可见区域内+预加载页面的数据
   * - nvue无效
   * @param list 虚拟列表当前渲染的数组
   * @since 2.2.7
   */
  onVirtualListChange?: (list: any[]) => void

  /**
   * 使用虚拟列表或内置列表时点击了cell
   * - nvue无效
   * @param list 虚拟列表当前渲染的数组
   * @since 2.4.0
   */
  onInnerCellClick?: (info: ZPagingParams.InnerCellClickInfo) => void

  /**
   * 虚拟列表顶部占位高度改变
   * - nvue无效
   * @param height 虚拟列表顶部占位高度(单位：px)
   * @since 2.7.12
   */
  onVirtualPlaceholderTopHeight?: (height: number) => void

  // ******************** 聊天记录模式相关事件 ********************
  /**
   * 在聊天记录模式下，触摸列表隐藏了键盘
   * - nvue无效
   * @since 2.3.6
   */
  onHidedKeyboard?: () => void
  
  /**
   * 键盘高度改变
   * @param info 键盘高度信息
   * @since 2.7.1
   */
  onKeyboardHeightChange?: (info: ZPagingParams.KeyboardHeightInfo) => void

  /**
   * z-paging列表滚动时触发
   * @param event 滚动事件信息，vue使用_ScrollInfo，nvue使用_ScrollInfoN
   */
  onScroll?: (event: ZPagingParams.ScrollInfo | ZPagingParams.ScrollInfoN) => void

  /**
   * scrollTop改变时触发，使用点击返回顶部时需要获取scrollTop时可使用此事件
   * @param scrollTop 
   */
  onScrollTopChange?: (scrollTop: number) => void

  /**
   * z-paging内置的scroll-view/list-view/waterfall滚动底部时触发
   */
  onScrolltolower?: () => void

  /**
   * z-paging内置的scroll-view/list-view/waterfall滚动顶部时触发
   */
  onScrolltoupper?: () => void

  /**
   * z-paging内置的list滚动结束时触发
   * - 仅nvue有效
   * @param event 滚动结束时触发事件信息
   * @since 2.7.3
   */
  onScrollend?: (event: ZPagingParams.ScrollendEvent) => void

  // ******************** 布局&交互相关事件 ********************
  /**
   * z-paging中内容高度改变时触发
   * @param height 改变后的高度
   * @since 2.1.3
   */
  onContentHeightChanged?: (height: number) => void

  /**
   * 监听列表触摸方向改变
   * - nvue无效
   * - 必须同时设置:watch-touch-direction-change="true"
   * @param direction 列表触摸的方向，top代表用户将列表向上移动(scrollTop不断减小)，bottom代表用户将列表向下移动(scrollTop不断增大)
   * @since 2.3.0
   */
  onTouchDirectionChange?: (direction: ZPagingEnums.TouchDirection) => void
}


// ****************************** Slots ******************************
declare interface ZPagingSlots {
  // ******************** 主体布局Slot ********************
  /**
   * 默认插入的列表view
   */
  ['default']?: () => any

  /**
   * 可以将自定义导航栏、tab-view等需要固定的(不需要跟着滚动的)元素放入slot="top"的view中。
   * 注意，当有多个需要固定的view时，请用一个view包住它们，并且在这个view上设置slot="top"。需要固定在顶部的view请勿设置position: fixed;
   * @since 1.5.5
   */
  ['top']?: () => any

  /**
   * 可以将需要固定在底部的(不需要跟着滚动的)元素放入slot="bottom"的view中。
   * 注意，当有多个需要固定的view时，请用一个view包住它们，并且在这个view上设置slot="bottom"。需要固定在底部的view请勿设置position: fixed;
   * @since 1.6.2
   */
  ['bottom']?: () => any

  /**
   * 可以将需要固定在左侧的(不需要跟着滚动的)元素放入slot="left"的view中。
   * 注意，当有多个需要固定的view时，请用一个view包住它们，并且在这个view上设置slot="left"。需要固定在左侧的view请勿设置position: fixed;
   * @since 2.2.3
   */
  ['left']?: () => any

  /**
   * 可以将需要固定在左侧的(不需要跟着滚动的)元素放入slot="right"的view中。
   * 注意，当有多个需要固定的view时，请用一个view包住它们，并且在这个view上设置slot="right"。需要固定在右侧的view请勿设置position: fixed;
   * @since 2.2.3
   */
  ['right']?: () => any

  // ******************** 下拉刷新Slot ********************
  /**
   * 自定义下拉刷新view，设置后则不使用uni自带的下拉刷新view和z-paging自定义的下拉刷新view。此view的style必须设置为height:100%
   * - 在非nvue中，自定义下拉刷新view的高度受refresher-threshold控制，因此如果它的高度不为默认的80rpx，则需要设置refresher-threshold="自定义下拉刷新view的高度"
   * @param refresherStatus 下拉刷新状态：default:默认状态 release-to-refresh:松手立即刷新 loading:刷新中 complete:刷新结束 go-f2:松手进入二楼
   */
  ['refresher']?: (props: ZPagingParams.RefresherSlotProps) => any

  /**
   * 自定义结束状态下的下拉刷新view，若设置，当下拉刷新结束时，会替换当前状态下的下拉刷新view。
   * - 注意：默认情况下您无法看到结束状态的下拉刷新view，除非您设置了refresher-complete-delay并且值足够大，例如：500
   * @since 2.1.1
   */
  ['refresherComplete']?: () => any

  /**
   * 自定义松手显示二楼状态下的view
   * @since 2.7.7
   */
  ['refresherF2']?: () => any

  /**
   * 自定义需要插入二楼的view
   * @since 2.7.7
   */
  ['f2']?: () => any

  // ******************** 底部加载更多Slot ********************
  /**
   * 自定义滑动到底部"默认"状态的view(即"点击加载更多view")
   */
  ['loadingMoreDefault']?: () => any

  /**
   * 自定义滑动到底部"加载中"状态的view
   */
  ['loadingMoreLoading']?: () => any

  /**
   * 自定义滑动到底部"没有更多数据"状态的view
   */
  ['loadingMoreNoMore']?: () => any

  /**
   * 自定义滑动到底部"加载失败"状态的view
   */
  ['loadingMoreFail']?: () => any

  // ******************** 空数据图Slot ********************
  /**
   * 自定义空数据占位view
   * @param isLoadFailed 是否加载失败：true: 加载失败，false: 加载成功
   */
  ['empty']?: (props: ZPagingParams.EmptySlotProps) => any

  // ******************** 全屏Loading Slot ********************
  /**
   * 自定义页面reload时的加载view
   * - 注意：这个slot默认仅会在第一次加载时显示，若需要每次reload时都显示，需要将auto-hide-loading-after-first-loaded设置为false
   */
  ['loading']?: () => any

  // ******************** 返回顶部按钮Slot ********************
  /**
   * 自定义点击返回顶部view
   * - 注意：此view受“【返回顶部按钮】配置”控制，且其父view默认宽高为76rpx
   * @since 1.9.4
   */
  ['backToTop']?: () => any

  // ******************** 虚拟列表&内置列表Slot ********************
  /**
   * 内置列表中的cell
   * - use-virtual-list或use-inner-list为true时有效
   * @param item 当前item
   * @param index 当前index
   * @since 2.2.5
   */
  ['cell']?: (props: ZPagingParams.InnerListCellSlotProps) => any

  /**
   * 内置列表中的header(在cell顶部且跟随列表滚动)
   * - use-virtual-list或use-inner-list为true时有效
   * @since 2.2.5
   */
  ['header']?: () => any

  /**
   * 内置列表中的footer(在cell顶部且跟随列表滚动)
   * - use-virtual-list或use-inner-list为true时有效
   * @since 2.2.5
   */
  ['footer']?: () => any

  // ******************** 聊天记录模式Slot ********************
  /**
   * 使用聊天记录模式时自定义顶部加载更多view(除没有更多数据外)
   * - use-chat-record-mode为true时有效
   * @param loadingMoreStatus 底部加载更多状态：default:默认状态 loading:加载中 no-more:没有更多数据 fail:加载失败
   */
  ['chatLoading']?: (props: ZPagingParams.ChatLoadingSlotProps) => any

  /**
   * 使用聊天记录模式时自定义没有更多数据view
   * - use-chat-record-mode为true时有效
   * @since 2.7.5
   */
  ['chatNoMore']?: () => any
}

// ****************************** Methods ******************************
declare interface _ZPagingRef<T = any> {
  // ******************** 数据刷新&处理方法 ********************
  /**
   * 重新加载分页数据，pageNo恢复为默认值，相当于下拉刷新的效果
   *
   * @param [animate=false] 是否展示下拉刷新动画
   */
  reload: (animate?: boolean) => Promise< ZPagingParams.ReturnData<T>>;

  /**
   * 刷新列表数据，pageNo和pageSize不会重置，列表数据会重新从服务端获取
   *
   * @since 2.0.4
   * @returns {Promise<ZPagingParams.ReturnData<T>>} Promise，当前最新分页结果：
   * - resolve 操作成功
   * - - `totalList` (T[]): 当前总列表
   * - - `noMore` (boolean): 是否没有更多数据
   * - reject 操作失败
   */
  refresh: () => Promise<ZPagingParams.ReturnData<T>>;

  /**
   * 刷新列表数据至指定页
   *
   * @since 2.5.9
   * @param page 目标页数
   * @returns {Promise<ZPagingParams.ReturnData<T>>} Promise，当前最新分页结果：
   * - resolve 操作成功
   * - - `totalList` (T[]): 当前总列表
   * - - `noMore` (boolean): 是否没有更多数据
   * - reject 操作失败
   */
  refreshToPage: (page: number) => Promise<ZPagingParams.ReturnData<T>>;

  /**
   * 请求结束
   * - 当通过complete传进去的数组长度小于pageSize时，则判定为没有更多了
   *
   * @param [data] 请求结果数组
   * @param [success=true] 是否请求成功
   * @returns {Promise<ZPagingParams.ReturnData<T>>} Promise，当前最新分页结果：
   * - resolve 操作成功
   * - - `totalList` (T[]): 当前总列表
   * - - `noMore` (boolean): 是否没有更多数据
   * - reject 操作失败
   */
  complete: (data?: T[] | false, success?: boolean) => Promise<ZPagingParams.ReturnData<T>>;

  /**
   * 请求结束
   * - 通过total判断是否有更多数据
   *
   * @since 2.0.6
   * @param data 请求结果数组
   * @param total 列表总长度
   * @param [success=true] 是否请求成功
   * @returns {Promise<ZPagingParams.ReturnData<T>>} Promise，当前最新分页结果：
   * - resolve 操作成功
   * - - `totalList` (T[]): 当前总列表
   * - - `noMore` (boolean): 是否没有更多数据
   * - reject 操作失败
   */
  completeByTotal: (data: T[], total: number, success?: boolean) => Promise<ZPagingParams.ReturnData<T>>;

  /**
   * 请求结束
   * - 自行判断是否有更多数据
   *
   * @since 1.9.2
   * @param data 请求结果数组
   * @param noMore 是否没有更多数据
   * @param [success=true] 是否请求成功
   * @returns {Promise<ZPagingParams.ReturnData<T>>} Promise，当前最新分页结果：
   * - resolve 操作成功
   * - - `totalList` (T[]): 当前总列表
   * - - `noMore` (boolean): 是否没有更多数据
   * - reject 操作失败
   */
  completeByNoMore: (data: T[], noMore: boolean, success?: boolean) => Promise<ZPagingParams.ReturnData<T>>;

  /**
   * 请求失败
   * - 通过方法传入请求失败原因，将请求失败原因传递给z-paging展示
   *
   * @since 2.6.3
   * @param cause 请求失败原因
   * @returns {Promise<ZPagingParams.ReturnData<T>>} Promise，当前最新分页结果：
   * - resolve 操作成功
   * - - `totalList` (T[]): 当前总列表
   * - - `noMore` (boolean): 是否没有更多数据
   * - reject 操作失败
   */
  completeByError: (cause: string) => Promise<ZPagingParams.ReturnData<T>>;

  /**
   * 请求结束
   * - 保证数据一致
   *
   * @since 1.6.4
   * @param data 请求结果数组
   * @param key dataKey，需与:data-key绑定的一致
   * @param [success=true] 是否请求成功
   * @returns {Promise<ZPagingParams.ReturnData<T>>} Promise，当前最新分页结果：
   * - resolve 操作成功
   * - - `totalList` (T[]): 当前总列表
   * - - `noMore` (boolean): 是否没有更多数据
   * - reject 操作失败
   */
  completeByKey: (data: T[], key: string, success?: boolean) => Promise<ZPagingParams.ReturnData<T>>;

  /**
   * 清空分页数据，pageNo恢复为默认值
   *
   * @since 2.1.0
   */
  clear: () => void;

  /**
   * 从顶部添加数据，不会影响分页的pageNo和pageSize
   *
   * @param data 需要添加的数据，可以是一条数据或一组数据
   * @param [scrollToTop=true] 是否滚动到顶部，不填默认为true
   * @param [animate=true] 是否使用动画滚动到顶部
   */
  addDataFromTop: (data: _Arrayable<T>, scrollToTop?: boolean, animate?: boolean) => void;

  /**
   * 【不推荐】重新设置列表数据，调用此方法不会影响pageNo和pageSize，也不会触发请求
   * - 适用场景：当需要删除列表中某一项时，将删除对应项后的数组通过此方法传递给z-paging
   *
   * @param data 修改后的列表数组
   */
  resetTotalData: (data: T[]) => void;

  // ******************** 下拉刷新相关方法 ********************
  /**
   * 终止下拉刷新状态
   *
   * @since 2.1.0
   */
  endRefresh: () => void;

  /**
   * 手动更新自定义下拉刷新view高度
   * - 常用于某些情况下使用slot="refresher"插入的view高度未能正确计算导致异常时手动更新其高度
   *
   * @since 2.6.1
   */
  updateCustomRefresherHeight: () => void;

  /**
   * 手动关闭二楼
   *
   * @since 2.7.7
   */
  closeF2: () => void;

  // ******************** 底部加载更多相关方法 ********************
  /**
   * 手动触发上拉加载更多
   * - 非必须，可依据具体需求使用，例如当z-paging未确定高度时，内部的scroll-view会无限增高，此时z-paging无法得知是否滚动到底部，您可以在页面的onReachBottom中手动调用此方法触发上拉加载更多
   *
   * @param [source] 触发加载更多的来源类型
   */
  doLoadMore: (source?: "click" | "toBottom") => void;

  // ******************** 页面滚动&布局相关方法 ********************
  /**
   * 当使用页面滚动并且自定义下拉刷新时，请在页面的onPageScroll中调用此方法，告知z-paging当前的pageScrollTop，否则会导致在任意位置都可以下拉刷新
   * - 若引入了mixins，则不需要调用此方法
   *
   * @param scrollTop 从page的onPageScroll中获取的scrollTop
   */
  updatePageScrollTop: (scrollTop: number) => void;

  /**
   * 在使用页面滚动并且设置了slot="top"时，默认初次加载会自动获取其高度，并使内部容器下移，当slot="top"的view高度动态改变时，在其高度需要更新时调用此方法
   */
  updatePageScrollTopHeight: () => void;

  /**
   * 在使用页面滚动并且设置了slot="bottom"时，默认初次加载会自动获取其高度，并使内部容器下移，当slot="bottom"的view高度动态改变时，在其高度需要更新时调用此方法
   */
  updatePageScrollBottomHeight: () => void;

  /**
   * 更新slot="left"和slot="right"宽度，当slot="left"或slot="right"宽度动态改变后调用
   *
   * @since 2.3.5
   */
  updateLeftAndRightWidth: () => void;

  /**
   * 更新fixed模式下z-paging的布局，在onShow时候调用，以修复在iOS+h5+tabbar+fixed+底部有安全区域的设备中从tabbar页面跳转到无tabbar页面后返回，底部有一段空白区域的问题
   *
   * @since 2.6.5
   */
  updateFixedLayout: () => void;

  // ******************** 虚拟列表相关方法 ********************
  /**
   * 在使用动态高度虚拟列表时，若在列表数组中需要插入某个item，需要调用此方法
   *
   * @since 2.5.9
   * @param item 插入的数据项
   * @param index 插入的cell位置，若为2，则插入的item在原list的index=1之后，从0开始
   */
  doInsertVirtualListItem: (item: T, index: number) => void;

  /**
   * 在使用动态高度虚拟列表时，手动更新指定cell的缓存高度
   * - 当cell高度在初始化之后再次改变时调用
   *
   * @since 2.4.0
   * @param index 需要更新的cell在列表中的位置，从0开始
   */
  didUpdateVirtualListCell: (index: number) => void;

  /**
   * 在使用动态高度虚拟列表时，若删除了列表数组中的某个item，需要调用此方法以更新高度缓存数组
   *
   * @since 2.4.0
   * @param index 需要更新的cell在列表中的位置，从0开始
   */
  didDeleteVirtualListCell: (index: number) => void;

  /**
   * 手动触发虚拟列表渲染更新，可用于解决例如修改了虚拟列表数组中元素，但展示未更新的情况
   *
   * @since 2.7.11
   */
  updateVirtualListRender: () => void;

  // ******************** 本地分页相关方法 ********************
  /**
   * 设置本地分页，请求结束(成功或者失败)调用此方法，将请求的结果传递给z-paging作分页处理
   * - 若调用了此方法，则上拉加载更多时内部会自动分页，不会触发@query所绑定的事件
   *
   * @param data 请求结果数组
   * @param [success=true] 是否请求成功
   * @returns {Promise<ZPagingParams.ReturnData<T>>} Promise，当前最新分页结果：
   * - resolve 操作成功
   * - - `totalList` (T[]): 当前总列表
   * - - `noMore` (boolean): 是否没有更多数据
   * - reject 操作失败
   */
  setLocalPaging: (data: T[], success?: boolean) => Promise<ZPagingParams.ReturnData<T>>;

  // ******************** 聊天记录模式相关方法 ********************
  /**
   * 手动触发滚动到顶部加载更多，聊天记录模式时有效
   */
  doChatRecordLoadMore: () => void;

  /**
   * 添加聊天记录，use-chat-record-mode为true时有效
   *
   * @param data 需要添加的聊天数据，可以是一条数据或一组数据
   * @param [scrollToBottom=true] 是否滚动到底部
   * @param [animate=true] 是否使用动画滚动到底部
   */
  addChatRecordData: (data: _Arrayable<T>, scrollToBottom?: boolean, animate?: boolean) => void;

  // ******************** 滚动到指定位置方法 ********************
  /**
   * 滚动到顶部
   *
   * @param [animate=true] 是否有动画效果
   */
  scrollToTop: (animate?: boolean) => void;

  /**
   * 滚动到底部
   *
   * @param [animate=true] 是否有动画效果
   */
  scrollToBottom: (animate?: boolean) => void;

  /**
   * 滚动到指定view
   * - vue中有效，若此方法无效，请使用scrollIntoViewByNodeTop
   *
   * @param id 需要滚动到的view的id值，不包含"#"
   * @param [offset=0] 偏移量，单位为px
   * @param [animate=false] 是否有动画效果
   */
  scrollIntoViewById: (id: string, offset?: number, animate?: boolean) => void;

  /**
   * 滚动到指定view
   * - vue中有效
   *
   * @since 1.7.4
   * @param top 需要滚动的view的top值(通过uni.createSelectorQuery()获取)
   * @param [offset=0] 偏移量，单位为px
   * @param [animate=false] 是否有动画效果
   */
  scrollIntoViewByNodeTop: (top: number, offset?: number, animate?: boolean) => void;

  /**
   * y轴滚动到指定位置
   * - vue中有效
   * - 与scrollIntoViewByNodeTop的不同之处在于，scrollToY传入的是view相对于屏幕的top值，而scrollIntoViewByNodeTop传入的top值并非是固定的，通过uni.createSelectorQuery()获取到的top会因列表滚动而改变
   *
   * @since 2.1.0
   * @param y 与顶部的距离，单位为px
   * @param [offset=0] 偏移量，单位为px
   * @param [animate=false] 是否有动画效果
   */
  scrollToY: (y: number, offset?: number, animate?: boolean) => void;

  /**
   * x轴滚动到指定位置
   * - 非页面滚动且在vue中有效
   *
   * @since 2.8.5
   * @param x 与左侧的距离，单位为px
   * @param [offset=0] 偏移量，单位为px
   * @param [animate=false] 是否有动画效果
   */
  scrollToX: (x: number, offset?: number, animate?: boolean) => void;

  /**
   * 滚动到指定view
   * - nvue或虚拟列表中有效
   * - 在nvue中的cell必须设置 :ref="`z-paging-${index}`"
   *
   * @param index 需要滚动到的view的index(第几个)
   * @param [offset=0] 偏移量，单位为px
   * @param [animate=false] 是否有动画效果
   */
  scrollIntoViewByIndex: (index: number, offset?: number, animate?: boolean) => void;

  /**
   * 滚动到指定view
   * - nvue中有效
   *
   * @param view 需要滚动到的view(通过this.$refs.xxx获取)
   * @param [offset=0] 偏移量，单位为px
   * @param [animate=false] 是否有动画效果
   */
  scrollIntoViewByView: (view: any, offset?: number, animate?: boolean) => void;

  /**
   * 设置nvue List的specialEffects
   *
   * @since 2.0.4
   * @param args 参见https://uniapp.dcloud.io/component/list?id=listsetspecialeffects
   */
  setSpecialEffects: (args: ZPagingParams.SetSpecialEffectsArgs) => void;

  // ******************** nvue独有方法 ********************
  /**
   * 与{@link setSpecialEffects}相同
   *
   * @since 2.0.4
   */
  setListSpecialEffects: (args: ZPagingParams.SetSpecialEffectsArgs) => void;

  // ******************** 缓存相关方法 ********************
  /**
   * 手动更新列表缓存数据，将自动截取v-model绑定的list中的前pageSize条覆盖缓存，请确保在list数据更新到预期结果后再调用此方法
   *
   * @since 2.3.9
   */
  updateCache: () => void;

  // ******************** 获取版本号方法 ********************
  /**
   * 获取当前版本号
   */
  getVersion: () => string;
}

declare interface _ZPaging {
  new (): {
    $props: AllowedComponentProps &
      VNodeProps &
      ZPagingProps
    $slots: ZPagingSlots
  }
}

export declare const ZPaging: _ZPaging

declare global {
  interface ZPagingRef<T = any> extends _ZPagingRef<T> {}
  // 兼容v2.8.1之前的旧版本
  interface ZPagingInstance<T = any> extends _ZPagingRef<T> {}
}
