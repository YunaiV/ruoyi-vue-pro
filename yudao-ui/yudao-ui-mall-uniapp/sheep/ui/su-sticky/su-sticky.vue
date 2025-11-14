<template>
  <view class="u-sticky" :id="elId" :style="[style]">
    <view :style="[stickyContent]" class="u-sticky__content"><slot /></view>
  </view>
</template>

<script>
  import { deepMerge, addStyle, addUnit, guid, getPx, os } from '@/sheep/helper';
  import sheep from '@/sheep';
  /**
   * sticky 吸顶
   * @description 该组件与CSS中position: sticky属性实现的效果一致，当组件达到预设的到顶部距离时， 就会固定在指定位置，组件位置大于预设的顶部距离时，会重新按照正常的布局排列。
   * @property {String ｜ Number}	offsetTop		吸顶时与顶部的距离，单位px（默认 0 ）
   * @property {String ｜ Number}	customNavHeight	自定义导航栏的高度 （h5 默认44  其他默认 0 ）
   * @property {Boolean}			stickyToTop		是否开启吸顶功能 （默认 false ）
   * @property {String}			bgColor			组件背景颜色（默认 '#ffffff' ）
   * @property {String ｜ Number}	zIndex			吸顶时的z-index值
   * @property {String ｜ Number}	index			自定义标识，用于区分是哪一个组件
   * @property {Object}			customStyle		组件的样式，对象形式
   * @event {Function} fixed		组件吸顶时触发
   * @event {Function} unfixed	组件取消吸顶时触发
   * @example <u-sticky offsetTop="200"><view>塞下秋来风景异，衡阳雁去无留意</view></u-sticky>
   */
  export default {
    name: 'su-sticky',
    props: {
      // 吸顶容器到顶部某个距离的时候，进行吸顶，在H5平台，NavigationBar为44px
      offsetTop: {
        type: [String, Number],
        default: 0,
      },
      // 自定义导航栏的高度
      customNavHeight: {
        type: [String, Number],
        // #ifdef H5
        // H5端的导航栏属于“自定义”导航栏的范畴，因为它是非原生的，与普通元素一致
        default: 44,
        // #endif
        // #ifndef H5
        default: sheep.$platform.navbar,
        // #endif
      },
      // 是否开启吸顶功能
      stickyToTop: {
        type: Boolean,
        default: false,
      },
      // 吸顶区域的背景颜色
      bgColor: {
        type: String,
        default: 'transparent',
      },
      // z-index值
      zIndex: {
        type: [String, Number],
        default: '',
      },
      // 列表中的索引值
      index: {
        type: [String, Number],
        default: '',
      },
      customStyle: {
        type: [Object, String],
        default: () => ({}),
      },
    },
    data() {
      return {
        cssSticky: false, // 是否使用css的sticky实现
        stickyTop: 0, // 吸顶的top值，因为可能受自定义导航栏影响，最终的吸顶值非offsetTop值
        elId: guid(),
        left: 0, // js模式时，吸顶的内容因为处于postition: fixed模式，为了和原来保持一致的样式，需要记录并重新设置它的left，height，width属性
        width: 'auto',
        height: 'auto',
        fixed: false, // js模式时，是否处于吸顶模式
      };
    },
    computed: {
      style() {
        const style = {};
        if (!this.stickyToTop) {
          if (this.cssSticky) {
            style.position = 'sticky';
            style.zIndex = this.uZindex;
            style.top = addUnit(this.stickyTop);
          } else {
            style.height = this.fixed ? this.height + 'px' : 'auto';
          }
        } else {
          // 无需吸顶时，设置会默认的relative(nvue)和非nvue的static静态模式即可
          // #ifdef APP-NVUE
          style.position = 'relative';
          // #endif
          // #ifndef APP-NVUE
          style.position = 'static';
          // #endif
        }
        style.backgroundColor = this.bgColor;
        return deepMerge(addStyle(this.customStyle), style);
      },
      // 吸顶内容的样式
      stickyContent() {
        const style = {};
        if (!this.cssSticky) {
          style.position = this.fixed ? 'fixed' : 'static';
          style.top = this.stickyTop + 'px';
          style.left = this.left + 'px';
          style.width = this.width == 'auto' ? 'auto' : this.width + 'px';
          style.zIndex = this.uZindex;
        }
        return style;
      },
      uZindex() {
        return this.zIndex ? this.zIndex : 970;
      },
    },
    mounted() {
      this.init();
    },
    methods: {
      init() {
        this.getStickyTop();
        // 判断使用的模式
        this.checkSupportCssSticky();
        // 如果不支持css sticky，则使用js方案，此方案性能比不上css方案
        if (!this.cssSticky) {
          !this.stickyToTop && this.initObserveContent();
        }
      },
      $uGetRect(selector, all) {
        return new Promise((resolve) => {
          uni
            .createSelectorQuery()
            .in(this)
            [all ? 'selectAll' : 'select'](selector)
            .boundingClientRect((rect) => {
              if (all && Array.isArray(rect) && rect.length) {
                resolve(rect);
              }
              if (!all && rect) {
                resolve(rect);
              }
            })
            .exec();
        });
      },
      initObserveContent() {
        // 获取吸顶内容的高度，用于在js吸顶模式时，给父元素一个填充高度，防止"塌陷"
        this.$uGetRect('#' + this.elId).then((res) => {
          this.height = res.height;
          this.left = res.left;
          this.width = res.width;
          this.$nextTick(() => {
            this.observeContent();
          });
        });
      },
      observeContent() {
        // 先断掉之前的观察
        this.disconnectObserver('contentObserver');
        const contentObserver = uni.createIntersectionObserver({
          // 检测的区间范围
          thresholds: [0.95, 0.98, 1],
        });
        // 到屏幕顶部的高度时触发
        contentObserver.relativeToViewport({
          top: -this.stickyTop,
        });
        // 绑定观察的元素
        contentObserver.observe(`#${this.elId}`, (res) => {
          this.setFixed(res.boundingClientRect.top);
        });
        this.contentObserver = contentObserver;
      },
      setFixed(top) {
        // 判断是否出于吸顶条件范围
        const fixed = top <= this.stickyTop;
        this.fixed = fixed;
      },
      disconnectObserver(observerName) {
        // 断掉观察，释放资源
        const observer = this[observerName];
        observer && observer.disconnect();
      },
      getStickyTop() {
        this.stickyTop = getPx(this.offsetTop) + getPx(this.customNavHeight);
      },
      async checkSupportCssSticky() {
        // #ifdef H5
        // H5，一般都是现代浏览器，是支持css sticky的，这里使用创建元素嗅探的形式判断
        if (this.checkCssStickyForH5()) {
          this.cssSticky = true;
        }
        // #endif

        // 如果安卓版本高于8.0，依然认为是支持css sticky的(因为安卓7在某些机型，可能不支持sticky)
        if (os() === 'android' && Number(uni.getDeviceInfo().system) > 8) {
          this.cssSticky = true;
        }

        // APP-Vue和微信平台，通过computedStyle判断是否支持css sticky
        // #ifdef APP-VUE || MP-WEIXIN
        this.cssSticky = await this.checkComputedStyle();
        // #endif

        // ios上，从ios6开始，都是支持css sticky的
        if (os() === 'ios') {
          this.cssSticky = true;
        }

        // nvue，是支持css sticky的
        // #ifdef APP-NVUE
        this.cssSticky = true;
        // #endif
      },
      // 在APP和微信小程序上，通过uni.createSelectorQuery可以判断是否支持css sticky
      checkComputedStyle() {
        // 方法内进行判断，避免在其他平台生成无用代码
        // #ifdef APP-VUE || MP-WEIXIN
        return new Promise((resolve) => {
          uni
            .createSelectorQuery()
            .in(this)
            .select('.u-sticky')
            .fields({
              computedStyle: ['position'],
            })
            .exec((e) => {
              resolve('sticky' === e[0].position);
            });
        });
        // #endif
      },
      // H5通过创建元素的形式嗅探是否支持css sticky
      // 判断浏览器是否支持sticky属性
      checkCssStickyForH5() {
        // 方法内进行判断，避免在其他平台生成无用代码
        // #ifdef H5
        const vendorList = ['', '-webkit-', '-ms-', '-moz-', '-o-'],
          vendorListLength = vendorList.length,
          stickyElement = document.createElement('div');
        for (let i = 0; i < vendorListLength; i++) {
          stickyElement.style.position = vendorList[i] + 'sticky';
          if (stickyElement.style.position !== '') {
            return true;
          }
        }
        return false;
        // #endif
      },
    },
    beforeDestroy() {
      this.disconnectObserver('contentObserver');
    },
  };
</script>

<style lang="scss" scoped>
  .u-sticky {
    /* #ifdef APP-VUE || MP-WEIXIN */
    // 此处默认写sticky属性，是为了给微信和APP通过uni.createSelectorQuery查询是否支持css sticky使用
    position: sticky;
    /* #endif */
  }
</style>
