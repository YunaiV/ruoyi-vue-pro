<template>
  <view class="ui-popover" :class="popover ? 'show' : 'hide'">
    <view
      class="ui-popover-button"
      :class="[ui]"
      :id="'popover-button-' + elId"
      :style="{ zIndex: index + zIndexConfig.popover }"
      @tap="popoverClick"
      @mouseleave="mouseleave"
      @mouseover="mouseover"
    >
      <slot></slot>
    </view>
    <view class="ui-popover-box" :style="BoxStyle">
      <view class="ui-popover-content-box" :id="'popover-content-' + elId" :style="contentStyle">
        <view
          class="ui-popover-content radius text-a"
          :class="bg"
          :style="{ zIndex: index + zIndexConfig.popover + 2 }"
        >
          <view class="p-3 text-sm" v-if="tips">{{ tips }}</view>
          <block v-else><slot name="content" /></block>
        </view>
        <view class="ui-popover-arrow" :class="bg" :style="arrowStyle"></view>
      </view>
    </view>
    <view
      class="ui-popover-mask"
      :class="mask ? 'bg-mask-50' : ''"
      :style="{ zIndex: index + zIndexConfig.popover - 1 }"
      @tap="popover = false"
      v-if="(popover && tips == '' && time == 0) || mask"
    ></view>
  </view>
</template>

<script>
  import { guid } from '@/sheep/helper';
  import zIndexConfig from '@/sheep/config/zIndex.js';
  import sheep from '@/sheep';

  export default {
    name: 'suPopover',
    data() {
      return {
        elId: guid(),
        zIndexConfig,
        popover: false,
        BoxStyle: '',
        contentStyle: '',
        arrowStyle: '',
        button: {},
        content: {},
      };
    },
    props: {
      ui: {
        type: String,
        default: '',
      },
      tips: {
        type: String,
        default: '',
      },
      bg: {
        type: String,
        default: 'ui-BG',
      },
      mask: {
        type: Boolean,
        default: false,
      },
      show: {
        type: [Boolean, String],
        default: 'change',
      },
      hover: {
        type: Boolean,
        default: false,
      },
      index: {
        type: Number,
        default: 0,
      },
      time: {
        type: Number,
        default: 0,
      },
      bottom: {
        type: Boolean,
        default: false,
      },
      isChange: {
        type: Boolean,
        default: false,
      },
    },
    watch: {
      popover(val) {
        this._computedQuery(
          sheep.$platform.device.windowWidth,
          sheep.$platform.device.windowHeight,
        );
        if (val) {
          if (this.tips != '' || this.time > 0) {
            setTimeout(
              () => {
                this.popover = false;
              },
              this.time == 0 ? 3000 : this.time,
            );
          }
          this.sys_layer = this.sys_layer + 100;
        } else {
          this.sys_layer = this.sys_layer - 100;
        }
        this.$emit('update:show', val);
      },
      show(val) {
        this.popover = val;
      },
    },
    mounted() {
      this.$nextTick(() => {
        this._computedQuery(
          sheep.$platform.device.windowWidth,
          sheep.$platform.device.windowHeight,
        );
        // #ifdef H5
        uni.onWindowResize((res) => {
          this._computedQuery(res.size.windowWidth, res.size.windowHeight);
        });
        // #endif
      });
    },
    methods: {
      _onHide() {
        this.popover = false;
      },
      _computedQuery(w, h) {
        uni
          .createSelectorQuery()
          .in(this)
          .select('#popover-button-' + this.elId)
          .boundingClientRect((button) => {
            if (button != null) {
              this.button = button;
            } else {
              console.log('popover-button-' + this.elId + ' data error');
            }
          })
          .select('#popover-content-' + this.elId)
          .boundingClientRect((content) => {
            if (content != null) {
              this.content = content;
              let button = this.button;
              //contentStyle
              let contentStyle = '';
              let arrowStyle = '';
              this.BoxStyle = `width:${w}px; left:-${button.left}px;z-index: ${
                this.index + this.sys_layer + 102
              }`;
              // 判断气泡在上面还是下面
              if (button.bottom < h / 2 || this.bottom) {
                // '下';
                contentStyle = contentStyle + `top:10px;`;
                arrowStyle = arrowStyle + `top:${-5}px;`;
              } else {
                // '上';
                contentStyle = contentStyle + `bottom:${button.height + 10}px;`;
                arrowStyle = arrowStyle + `bottom:${-5}px;`;
              }

              // 判断气泡箭头在左中右
              let btnCenter = button.right - button.width / 2;
              let contentCenter = content.right - content.width / 2;
              if (
                (btnCenter < w / 3 && content.width > btnCenter) ||
                (content.width > w / 2 && btnCenter < w / 2)
              ) {
                // '左';
                contentStyle = contentStyle + `left:10px;`;
                arrowStyle = arrowStyle + `left:${btnCenter - 17}px;`;
              } else if (
                (btnCenter > (w / 6) * 4 && content.width > w - btnCenter) ||
                (content.width > w / 2 && btnCenter > w / 2)
              ) {
                // '右';
                contentStyle = contentStyle + `right:10px;`;
                arrowStyle = arrowStyle + `right:${w - btnCenter - 17}px;`;
              } else {
                // '中';
                contentStyle =
                  contentStyle + `left:${button.left - content.width / 2 + button.width / 2}px;`;
                arrowStyle = arrowStyle + `left:0px;right:0px;margin:auto;`;
              }

              this.arrowStyle = arrowStyle + `z-index:${this.index + this.sys_layer + 1};`;
              this.contentStyle = contentStyle + `z-index:${this.index + this.sys_layer + 2};`;
            } else {
              console.log('popover-content-' + this.elId + ' data error');
            }
          })
          .exec();
      },
      popoverClick() {
        if (this.isChange) {
          return false;
        }
        if (this.tips == '') {
          this.popover = !this.popover;
        } else {
          this.popover = true;
        }
      },
      mouseover() {
        if (this.hover && (this.tips != '' || this.content.height != 0)) {
          this.popover = true;
        }
      },
      mouseleave() {
        if (this.hover) {
          this.popover = false;
        }
      },
    },
  };
</script>

<style lang="scss">
  .ui-popover {
    position: relative;

    .ui-popover-button {
      position: relative;
    }

    .ui-popover-box {
      position: absolute;

      .ui-popover-content-box {
        position: absolute;

        .ui-popover-content {
          position: relative;
        }

        .ui-popover-arrow {
          position: absolute;
          height: 15px;
          width: 15px;
          border-radius: 2px;
          transform: rotate(45deg);
        }

        &::after {
          content: '';
          width: 100%;
          height: 110%;
          position: absolute;
          background-color: #000000;
          top: 5%;
          left: 0;
          filter: blur(15px);
          opacity: 0.15;
        }
      }
    }

    .ui-popover-mask {
      position: fixed;
      width: 100%;
      height: 100%;
      top: 0;
      left: 0;
    }

    &.show {
      .ui-popover-button {
      }

      .ui-popover-content-box {
        opacity: 1;
        pointer-events: auto;
      }

      .ui-popover-arrow {
        display: block;
      }

      .ui-popover-mask {
        display: block;
      }
    }

    &.hide {
      .ui-popover-button {
      }

      .ui-popover-content-box {
        opacity: 0;
        pointer-events: none;
      }

      .ui-popover-arrow {
        display: none;
      }

      .ui-popover-mask {
        display: none;
      }
    }
  }
</style>
