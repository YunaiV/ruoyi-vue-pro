<!-- 省市区选择弹窗 -->
<template>
  <su-popup :show="show" @close="onCancel" round="20">
    <view class="ui-region-picker">
      <su-toolbar
        :cancelColor="cancelColor"
        :confirmColor="confirmColor"
        :cancelText="cancelText"
        :confirmText="confirmText"
        title="选择区域"
        @cancel="onCancel"
        @confirm="onConfirm('confirm')"
      />
      <view class="ui-picker-body">
        <picker-view
          :value="state.currentIndex"
          @change="change"
          class="ui-picker-view"
          @pickstart="pickstart"
          @pickend="pickend"
        >
          <picker-view-column>
            <view class="ui-column-item" v-for="province in provinceList" :key="province.id">
              <view :style="getSizeByNameLength(province.name)">{{ province.name }}</view>
            </view>
          </picker-view-column>
          <picker-view-column>
            <view class="ui-column-item" v-for="city in cityList" :key="city.id">
              <view :style="getSizeByNameLength(city.name)">{{ city.name }}</view>
            </view>
          </picker-view-column>
          <picker-view-column>
            <view class="ui-column-item" v-for="district in districtList" :key="district.id">
              <view :style="getSizeByNameLength(district.name)">{{ district.name }}</view>
            </view>
          </picker-view-column>
        </picker-view>
      </view>
    </view>
  </su-popup>
</template>

<script setup>
  /**
   * picker picker弹出选择器
   * @property {Object} params 需要显示的参
   * @property {Boolean} safe-area-inset-bottom 是否开启底部安全区适配（默认false）
   * @property {Boolean} show-time-tag 时间模式时，是否显示后面的年月日中文提示
   * @property {String} cancel-color 取消按钮的颜色
   * @property {String} confirm-color 确认按钮的颜色
   * @property {String} confirm-text 确认按钮的文字
   * @property {String} cancel-text 取消按钮的文字
   * @property {String} default-region 默认选中的地区，
   * @property {String} default-code 默认选中的地区
   * @property {Boolean} mask-close-able 是否允许通过点击遮罩关闭Picker（默认true）
   * @property {String Number} z-index 弹出时的z-index值（默认1075）
   * @property {Array} default-selector 数组形式，其中每一项表示选择了range对应项中的第几个
   * @property {String} range-key 当range参数的元素为对象时，指定Object中的哪个key的值作为选择器显示内容
   * @event {Function} confirm 点击确定按钮，返回当前选择的值
   * @event {Function} cancel 点击取消按钮，返回当前选择的值
   */
  import { computed, reactive } from 'vue';
  const props = defineProps({
    show: {
      type: Boolean,
      default: false,
    },
    // "取消"按钮的颜色
    cancelColor: {
      type: String,
      default: '#6666',
    },
    // "确定"按钮的颜色
    confirmColor: {
      type: String,
      default: 'var(--ui-BG-Main)',
    },
    // 取消按钮的文字
    cancelText: {
      type: String,
      default: '取消',
    },
    // 确认按钮的文字
    confirmText: {
      type: String,
      default: '确认',
    },
  });
  const areaData = uni.getStorageSync('areaData');

  const getSizeByNameLength = (name) => {
    let length = name.length;
    if (length <= 7) return '';
    if (length < 9) {
      return 'font-size:28rpx';
    } else {
      return 'font-size: 24rpx';
    }
  };
  const state = reactive({
    currentIndex: [0, 0, 0],
    moving: false, // 列是否还在滑动中，微信小程序如果在滑动中就点确定，结果可能不准确
  });
  const emits = defineEmits(['confirm', 'cancel', 'change']);

  const provinceList = areaData;

  const cityList = computed(() => {
    return areaData[state.currentIndex[0]].children;
  });
  const districtList = computed(() => {
    return cityList.value[state.currentIndex[1]]?.children;
  });
  // 标识滑动开始，只有微信小程序才有这样的事件
  const pickstart = () => {
    // #ifdef MP-WEIXIN
    state.moving = true;
    // #endif
  };

  // 标识滑动结束
  const pickend = () => {
    // #ifdef MP-WEIXIN
    state.moving = false;
    // #endif
  };
  const init = () => {};

  const onCancel = () => {
    emits('cancel');
  };

  // 用户更改picker的列选项
  const change = (e) => {
    if (
      state.currentIndex[0] === e.detail.value[0] &&
      state.currentIndex[1] === e.detail.value[1]
    ) {
      // 不更改省市区列表
      state.currentIndex[2] = e.detail.value[2];
      return;
    } else {
      // 更改省市区列表
      if (state.currentIndex[0] !== e.detail.value[0]) {
        e.detail.value[1] = 0;
      }
      e.detail.value[2] = 0;
      state.currentIndex = e.detail.value;
    }
    emits('change', state.currentIndex);
  };

  // 用户点击确定按钮
  const onConfirm = (event = null) => {
    // #ifdef MP-WEIXIN
    if (state.moving) return;
    // #endif
    let index = state.currentIndex;
    let province = provinceList[index[0]];
    let city = cityList.value[index[1]];
    let district = districtList.value[index[2]];
    let result = {
      province_name: province.name,
      province_id: province.id,
      city_name: city.name,
      city_id: city.id,
      district_name: district.name,
      district_id: district.id,
    };

    if (event) emits(event, result);
  };
</script>

<style lang="scss" scoped>
  .ui-region-picker {
    position: relative;
    z-index: 999;
  }

  .ui-picker-view {
    height: 100%;
    box-sizing: border-box;
  }
  .ui-picker-header {
    width: 100%;
    height: 90rpx;
    padding: 0 40rpx;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-sizing: border-box;
    font-size: 30rpx;
    background: #fff;
    position: relative;
  }

  .ui-picker-header::after {
    content: '';
    position: absolute;
    border-bottom: 1rpx solid #eaeef1;
    -webkit-transform: scaleY(0.5);
    transform: scaleY(0.5);
    bottom: 0;
    right: 0;
    left: 0;
  }

  .ui-picker__title {
    color: #333;
  }

  .ui-picker-body {
    width: 100%;
    height: 500rpx;
    overflow: hidden;
    background-color: #fff;
  }

  .ui-column-item {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    color: #333;
    padding: 0 8rpx;
  }

  .ui-btn-picker {
    padding: 16rpx;
    box-sizing: border-box;
    text-align: center;
    text-decoration: none;
  }

  .ui-opacity {
    opacity: 0.5;
  }

  .ui-btn-picker--primary {
    color: blue;
  }

  .ui-btn-picker--tips {
    color: red;
  }
</style>
