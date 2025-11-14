<template>
  <view class="ui-fixed">
    <view
      class="ui-fixed-box"
      :id="`fixed-${uuid}`"
      :class="[{ fixed: state.fixed }]"
      :style="[
        {
          left: sticky ? 'auto' : '0px',
          top: state.fixed && !bottom ? (noNav ? val : val + sys_navBar) + 'px' : 'auto',
          bottom: insetHeight,
          zIndex: index + sheep.$zIndex.navbar,
        },
        !alway ? { opacity: state.opacityVal } : '',
      ]"
    >
      <view
        class="ui-fixed-content"
        @tap="toTop"
        :style="[{ zIndex: index + sheep.$zIndex.navbar }]"
      >
        <slot></slot>
        <view
          v-if="safeAreaInsets.bottom && bottom && isInset"
          class="inset-bottom"
          :style="[{ height: safeAreaInsets.bottom + 'px' }]"
        ></view>
      </view>
      <view class="ui-fixed-bottom" :class="[bg]" v-if="bottom"></view>
      <view
        class="ui-fixed-bg"
        :class="[ui, bg]"
        :style="[
          { zIndex: index + sheep.$zIndex.navbar - 1 },
          bgStyles,
          opacity ? { opacity: state.opacityVal } : '',
        ]"
      ></view>
    </view>
    <view
      class="skeleton"
      :style="[{ height: state.content.height + 'px', width: width + 'px' }]"
      v-if="sticky ? state.fixed : placeholder && state.fixed"
    ></view>
  </view>
</template>

<script setup>
  import { onPageScroll } from '@dcloudio/uni-app';
  import { getCurrentInstance, unref, onMounted, reactive, nextTick, computed } from 'vue';
  import sheep from '@/sheep';
  const { safeAreaInsets } = sheep.$platform.device;

  const vm = getCurrentInstance();

  const uuid = sheep.$helper.guid();
  const sys_navBar = sheep.$platform.navbar;
  const state = reactive({
    content: {},
    fixed: true,
    scrollTop: 0,
    opacityVal: 0,
  });
  const insetHeight = computed(() => {
    if (state.fixed && props.bottom) {
      if (props.isInset) {
        return props.val + 'px';
      } else {
        return props.val + safeAreaInsets.bottom + 'px';
      }
    } else {
      return 'auto';
    }
  });
  const props = defineProps({
    noNav: {
      type: Boolean,
      default: false,
    },
    bottom: {
      type: Boolean,
      default: false,
    },
    bg: {
      type: String,
      default: '',
    },
    bgStyles: {
      type: Object,
      default() {},
    },
    val: {
      type: Number,
      default: 0,
    },
    width: {
      type: [String, Number],
      default: 0,
    },
    alway: {
      type: Boolean,
      default: true,
    },
    opacity: {
      type: Boolean,
      default: false,
    },
    index: {
      type: [Number, String],
      default: 0,
    },
    placeholder: {
      type: [Boolean],
      default: false,
    },
    sticky: {
      type: [Boolean],
      default: false,
    },
    noFixed: {
      type: Boolean,
      default: false,
    },
    ui: {
      type: String,
      default: '',
    },
    clickTo: {
      type: Boolean,
      default: false,
    },
    //是否需要安全区
    isInset: {
      type: Boolean,
      default: true,
    },
  });

  state.fixed = !unref(props.sticky);
  onPageScroll((e) => {
    let top = e.scrollTop;
    state.scrollTop = top;
    state.opacityVal = top > sheep.$platform.navbar ? 1 : top * 0.01;
  });

  onMounted(() => {
    nextTick(() => {
      computedQuery();
    });
  });

  const computedQuery = () => {
    uni.createSelectorQuery()
      .in(vm)
      .select(`#fixed-${uuid}`)
      .boundingClientRect((data) => {
        if (data != null) {
          state.content = data;
          if (unref(props.sticky)) {
            setFixed(state.scrollTop);
          }
        }
      })
      .exec();
  };

  const setFixed = (value) => {
    if (unref(props.bottom)) {
      state.fixed =
        value >=
        state.content.bottom -
          sheep.$platform.device.windowHeight +
          state.content.height +
          unref(props.val);
    } else {
      state.fixed =
        value >=
        state.content.top -
          (unref(props.noNav) ? unref(props.val) : unref(props.val) + sheep.$platform.navbar);
    }
  };

  const toTop = () => {
    if (props.hasToTop) {
      uni.pageScrollTo({
        scrollTop: state.content.top,
        duration: 100,
      });
    }
  };
</script>

<style lang="scss">
  .ui-fixed {
    .ui-fixed-box {
      position: relative;
      width: 100%;
      &.fixed {
        position: fixed;
      }
      .ui-fixed-content {
        position: relative;
      }
      .ui-fixed-bg {
        position: absolute;
        width: 100%;
        height: 100%;
        top: 0;
        z-index: 1;
        pointer-events: none;
      }
    }
  }
  .inset-bottom {
    background: #fff;
  }
</style>
