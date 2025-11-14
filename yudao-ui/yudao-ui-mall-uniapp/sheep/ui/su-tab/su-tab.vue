<template>
  <view
    class="ui-tab"
    ref="tabRef"
    :id="'tab-' + vm.uid"
    :class="[
      props.ui,
      props.tpl,
      props.bg,
      props.align,
      { 'ui-tab-inline': props.inline },
      { 'ui-tab-scrolls': props.scroll },
    ]"
  >
    <block v-if="scroll">
      <view class="ui-tab-scroll-warp">
        <scroll-view
          scroll-x="true"
          class="ui-tab-scroll"
          :scroll-left="state.curValue > 1 ? state.tabNodeList[state.curValue - 1].left : 0"
          scroll-with-animation
          :style="{ width: `${state.content.width}px` }"
        >
          <view class="ss-flex ss-col-center">
            <su-tab-item
              v-for="(item, index) in props.tab"
              :data="item"
              :index="index"
              :key="index"
              @up="upitem"
              @tap.native="click(index, item)"
            ></su-tab-item>
            <view
              class="ui-tab-mark-warp"
              :class="[{ over: state.over }]"
              :style="[{ left: state.markLeft + 'px' }, { width: state.markWidth + 'px' }]"
            >
              <view
                class="ui-tab-mark"
                :class="[props.mark, { 'ui-btn': props.tpl == 'btn' || props.tpl == 'subtitle' }]"
                :style="[
                  {
                    background:
                      props.tpl == 'btn' || props.tpl == 'subtitle' ? titleStyle.activeBg : 'none',
                  },
                ]"
              ></view>
            </view>
          </view>
        </scroll-view>
      </view>
    </block>
    <block v-else>
      <su-tab-item
        v-for="(item, index) in props.tab"
        :data="item"
        :index="index"
        :key="index"
        @up="upitem"
        @tap.native="click(index, item)"
      ></su-tab-item>
      <view
        class="ui-tab-mark-warp"
        :class="[{ over: state.over }]"
        :style="[{ left: state.markLeft + 'px' }, { width: state.markWidth + 'px' }]"
      >
        <view
          class="ui-tab-mark"
          :class="[props.mark, { 'ui-btn': props.tpl == 'btn' || props.tpl == 'subtitle' }]"
        ></view>
      </view>
    </block>
  </view>
</template>

<script>
  export default {
    name: 'SuTab',
  };
</script>

<script setup>
  /**
   * 基础组件 - suTab
   */

  import {
    toRef,
    ref,
    reactive,
    unref,
    onMounted,
    nextTick,
    getCurrentInstance,
    provide,
  } from 'vue';
  const vm = getCurrentInstance();

  // 数据
  const state = reactive({
    curValue: 0,
    tabNodeList: [],
    scrollLeft: 0,
    markLeft: 0,
    markWidth: 0,
    content: {
      width: 100,
    },
    over: false,
  });

  const tabRef = ref(null);
  // 参数
  const props = defineProps({
    modelValue: {
      type: Number,
      default: 0,
    },
    ui: {
      type: String,
      default: '',
    },
    bg: {
      type: String,
      default: '',
    },
    tab: {
      type: Array,
      default() {
        return [];
      },
    },
    // line dot long,subtitle,trapezoid
    tpl: {
      type: String,
      default: 'line',
    },
    mark: {
      type: String,
      default: '',
    },
    align: {
      type: String,
      default: '',
    },
    curColor: {
      type: String,
      default: 'ui-TC',
    },
    defaultColor: {
      type: String,
      default: 'ui-TC',
    },
    scroll: {
      type: Boolean,
      default: false,
    },
    inline: {
      type: Boolean,
      default: false,
    },
    titleStyle: {
      type: Object,
      default: () => ({
        activeBg: '#DA2B10',
        activeColor: '#FEFEFE',
        color: '#D70000',
      }),
    },
    subtitleStyle: {
      type: Object,
      default: () => ({
        activeColor: '#333',
        color: '#C42222',
      }),
    },
  });

  const emits = defineEmits(['update:modelValue', 'change']);

  onMounted(() => {
    state.curValue = props.modelValue;
    setCurValue(props.modelValue);
    nextTick(() => {
      computedQuery();
    });
    uni.onWindowResize((res) => {
      computedQuery();
    });
  });

  const computedQuery = () => {
    uni.createSelectorQuery()
      .in(vm)
      .select('#tab-' + vm.uid)
      .boundingClientRect((data) => {
        if (data != null) {
          if (data.left == 0 && data.right == 0) {
            // setTimeout(() => {
            computedQuery();
            // }, 300);
          } else {
            state.content = data;
            setTimeout(() => {
              state.over = true;
            }, 300);
          }
        } else {
          console.log('tab-' + vm.uid + ' data error');
        }
      })
      .exec();
  };

  const setCurValue = (value) => {
    if (value == state.curValue) return;
    state.curValue = value;
    computedMark();
  };

  const click = (index, item) => {
    setCurValue(index);
    emits('update:modelValue', index);
    emits('change', {
      index: index,
      data: item,
    });
  };

  const upitem = (index, e) => {
    state.tabNodeList[index] = e;
    if (index == state.curValue) {
      computedMark();
    }
  };

  const computedMark = () => {
    if (state.tabNodeList.length == 0) return;
    let left = 0;
    let list = unref(state.tabNodeList);
    let cur = state.curValue;
    state.markLeft = list[cur].left - state.content.left;
    state.markWidth = list[cur].width;
  };

  const computedScroll = () => {
    if (state.curValue == 0 || state.curValue == state.tabNodeList.length - 1) {
      return false;
    }
    let i = 0;
    let left = 0;
    let list = state.tabNodeList;
    for (i in list) {
      if (i == state.curValue && i != 0) {
        left = left - list[i - 1].width;
        break;
      }
      left = left + list[i].width;
    }
    state.scrollLeft = left;
  };

  provide('suTabProvide', {
    props,
    curValue: toRef(state, 'curValue'),
  });
</script>

<style lang="scss">
  .ui-tab {
    position: relative;
    display: flex;
    height: 4em;
    align-items: center;

    &.ui-tab-scrolls {
      width: 100%;
      /* #ifdef MP-WEIXIN */
      padding-bottom: 10px;
      /* #endif */
      .ui-tab-scroll-warp {
        overflow: hidden;
        height: inherit;
        width: 100%;
        .ui-tab-scroll {
          position: relative;
          display: block;
          white-space: nowrap;
          overflow: auto;
          min-height: 4em;
          line-height: 4em;
          width: 100% !important;
          .ui-tab-mark-warp {
            display: flex;
            align-items: top;
            justify-content: center;
            .ui-tab-mark.ui-btn {
              /* #ifndef MP-WEIXIN */
              height: 2em;
              width: calc(100% - 0.6em);
              margin-top: 4px;
              /* #endif */
              /* #ifdef MP-WEIXIN */
              height: 2em;
              width: calc(100% - 0.6em);
              margin-top: 4px;
              /* #endif */
            }
          }
        }
      }
    }

    .ui-tab-mark-warp {
      color: inherit;
      position: absolute;
      top: 0;
      height: 100%;
      z-index: 0;

      &.over {
        transition: 0.3s;
      }

      .ui-tab-mark {
        color: var(--ui-BG-Main);
        height: 100%;
      }
    }

    &.line {
      .ui-tab-mark {
        border-bottom: 2px solid currentColor;
      }
    }

    &.topline {
      .ui-tab-mark {
        border-top: 2px solid currentColor;
      }
    }

    &.dot {
      .ui-tab-mark::after {
        content: '';
        width: 0.5em;
        height: 0.5em;
        background-color: currentColor;
        border-radius: 50%;
        display: block;
        position: absolute;
        bottom: 0.3em;
        left: 0;
        right: 0;
        margin: auto;
      }
    }

    &.long {
      .ui-tab-mark::after {
        content: '';
        width: 2em;
        height: 0.35em;
        background-color: currentColor;
        border-radius: 5em;
        display: block;
        position: absolute;
        bottom: 0.3em;
        left: 0;
        right: 0;
        margin: auto;
      }
    }

    &.trapezoid {
      .ui-tab-mark::after {
        content: '';
        width: calc(100% - 2em);
        height: 0.35em;
        background-color: currentColor;
        border-radius: 5em 5em 0 0;
        display: block;
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        margin: auto;
      }
    }

    &.btn {
      .ui-tab-mark-warp {
        display: flex;
        align-items: center;
        justify-content: center;

        .ui-tab-mark.ui-btn {
          height: calc(100% - 1.6em);
          width: calc(100% - 0.6em);
        }
      }

      &.sm .ui-tab-mark.ui-btn {
        height: calc(100% - 2px);
        width: calc(100% - 2px);
        border-radius: #{$radius - 2};
      }
    }

    &.subtitle {
      .ui-tab-mark-warp {
        display: flex;
        align-items: top;
        justify-content: center;
        padding-top: 0.6em;

        .ui-tab-mark.ui-btn {
          height: calc(100% - 2.8em);
          width: calc(100% - 0.6em);
        }
      }
    }

    &.ui-tab-inline {
      display: inline-flex;
      height: 3.5em;

      &.ui-tab-scrolls {
        .ui-tab-scroll {
          height: calc(3.5em + 17px);
          line-height: 3.5em;

          .ui-tab-mark-warp {
            height: 3.5em;
          }
        }
      }

      &.btn {
        .ui-tab-mark-warp {
          .ui-tab-mark.ui-btn {
            height: calc(100% - 10px);
            width: calc(100% - 10px);
          }
        }
      }
    }

    &.sm {
      height: 70rpx !important;

      &.ui-tab-inline {
        height: 70rpx;

        &.ui-tab-scrolls {
          .ui-tab-scroll {
            height: calc(70rpx + 17px);
            line-height: 70rpx;

            .ui-tab-mark-warp {
              height: 70rpx;
            }
          }
        }

        &.btn .ui-tab-mark.ui-btn {
          height: calc(100% - 2px);
          width: calc(100% - 2px);
          border-radius: #{$radius - 2};
        }
      }
    }
  }
</style>
