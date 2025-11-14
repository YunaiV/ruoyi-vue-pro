<template>
  <view>
    <view :id="'tab-' + props.index" class="ui-tab-item" :class="[{ cur: cur }, tpl]">
      <view class="ui-tab-icon" :class="props.data.icon" v-if="props.data.icon"></view>
      <view
        class="ui-tab-text"
        :class="[cur ? 'curColor cur' : 'default-color']"
        :style="[{ color: cur ? titleStyle.activeColor : titleStyle.color }]"
      >
        {{ props.data.title }}
      </view>

      <view class="ui-tag badge ml-2" v-if="props.data.tag != null">{{ props.data.tag }}</view>
    </view>
    <view
      v-if="tpl === 'subtitle'"
      class="item-subtitle ss-flex ss-col-bottom ss-row-center"
      :style="[{ color: cur ? subtitleStyle.activeColor : subtitleStyle.color }]"
    >
      {{ props.data.subtitle }}
    </view>
  </view>
</template>

<script>
  export default {
    name: 'UiTabItem',
  };
</script>

<script setup>
  /**
   * 基础组件 - uiTabItem
   */
  import { computed, onMounted, getCurrentInstance, inject } from 'vue';
  import sheep from '@/sheep';
  const vm = getCurrentInstance();

  const props = defineProps({
    data: {
      type: [Object, String, Number],
      default() {},
    },
    index: {
      type: Number,
      default: 0,
    },
  });

  const emits = defineEmits(['up']);

  onMounted(() => {
    computedQuery();
    uni.onWindowResize((res) => {
      computedQuery();
    });
  });

  function getParent(name) {
    let parent = vm?.parent;
    // 无父级返回null
    if (parent) {
      let parentName = parent?.type?.name;
      // 父组件name 为真返回父级，为假循环
      while (parentName !== name) {
        parent = parent?.parent;
        // 存在父级循环，不存在打断循环
        if (parent) {
          parentName = parent?.type?.name;
        } else {
          return null;
        }
      }
      return parent;
    }
    return null;
  }

  const UiTab = getParent('SuTab');

  // 获取抛出的数据和方法
  let uiTabProvide;
  if (UiTab) {
    uiTabProvide = inject('suTabProvide');
  }
  const cur = computed(() => uiTabProvide?.curValue.value === props.index);
  const tpl = computed(() => uiTabProvide?.props?.tpl);
  const subtitleStyle = computed(() => uiTabProvide?.props?.subtitleStyle);
  const titleStyle = computed(() => uiTabProvide?.props?.titleStyle);

  const computedQuery = () => {
    uni.createSelectorQuery()
      .in(vm)
      .select('#tab-' + props.index)
      .boundingClientRect((data) => {
        if (data != null) {
          // 传递到父组件进行计算
          emits('up', props.index, data);
        } else {
          console.log('tab-item data error');
        }
      })
      .exec();
  };
</script>

<style lang="scss" scoped>
  .default-color {
    color: $black;
  }
  .ui-tab-item {
    display: inline-flex;
    align-items: center;
    padding: 0 1em;
    min-height: 1.5em;
    line-height: 1.5em;
    position: relative;
    z-index: 1;
    opacity: 0.6;
    transition: opacity 0.3s;
    min-width: 60px;

    .ui-tab-text {
      width: 100%;
      text-align: center;
    }

    .ui-tab-icon {
      margin: 0 0.25em;
      font-size: 120%;
    }

    &.cur {
      opacity: 1;
      font-weight: bold;
    }

    &.btn {
      .ui-tab-text {
        transform: scale(0.9);
        transition: color 0.3s;
        font-weight: bold;
      }
    }

    &.subtitle {
      .ui-tab-text {
        transform: scale(0.9);
        transition: color 0.3s;
        font-weight: bold;
        height: calc(100% - 2.6em);
        line-height: calc(100% - 2.6em);
        margin-top: 1.2em;
        color: $white;
      }
    }
  }

  .item-subtitle {
    height: 2em;
    font-size: 22rpx;
    color: $dark-9;
    margin-bottom: 0.6em;
  }

  .cur-subtitle {
    color: var(--ui-BG-Main);
  }
</style>
