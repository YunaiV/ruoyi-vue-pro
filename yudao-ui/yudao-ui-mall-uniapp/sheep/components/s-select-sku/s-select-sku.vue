<template>
  <!-- 规格弹窗 -->
  <su-popup :show="show" round="10" @close="emits('close')">
    <!-- SKU 信息 -->
    <view class="ss-modal-box bg-white ss-flex-col">
      <view class="modal-header ss-flex ss-col-center">
        <view class="header-left ss-m-r-30">
          <image
            class="sku-image"
            :src="state.selectedSku.picUrl || goodsInfo.picUrl"
            mode="aspectFill"
          />
        </view>
        <view class="header-right ss-flex-col ss-row-between ss-flex-1">
          <view class="goods-title ss-line-2">{{ goodsInfo.name }}</view>
          <view class="header-right-bottom ss-flex ss-col-center ss-row-between">
            <view class="ss-flex">
              <view class="price-text">
                {{
                  fen2yuan(
                    state.selectedSku.promotionPrice || state.selectedSku.price || goodsInfo.price,
                  )
                }}
                <text v-if="state.selectedSku.promotionType > 0">
                  <text class="iconBox" v-if="state.selectedSku.promotionType === 4">
                    限时优惠
                  </text>
                  <text class="iconBox" v-else-if="state.selectedSku.promotionType === 6">
                    会员价
                  </text>
                  <text class="origin-price-text">
                    {{ fen2yuan(state.selectedSku.price) }}
                  </text>
                </text>
              </view>
            </view>
            <view class="stock-text ss-m-l-20">
              {{ formatStock('exact', state.selectedSku.stock || goodsInfo.stock) }}
            </view>
          </view>
        </view>
      </view>

      <!-- 属性选择 -->
      <view class="modal-content ss-flex-1">
        <scroll-view scroll-y="true" class="modal-content-scroll" @touchmove.stop>
          <view class="sku-item ss-m-b-20" v-for="property in propertyList" :key="property.id">
            <view class="label-text ss-m-b-20">{{ property.name }}</view>
            <view class="ss-flex ss-col-center ss-flex-wrap">
              <button
                class="ss-reset-button spec-btn"
                v-for="value in property.values"
                :class="[
                  {
                    'ui-BG-Main-Gradient': state.currentPropertyArray[property.id] === value.id,
                  },
                  {
                    'disabled-btn': value.disabled === true,
                  },
                ]"
                :key="value.id"
                :disabled="value.disabled === true"
                @tap="onSelectSku(property.id, value.id)"
              >
                {{ value.name }}
              </button>
            </view>
          </view>
          <view class="buy-num-box ss-flex ss-col-center ss-row-between ss-m-b-40">
            <view class="label-text">购买数量</view>
            <su-number-box
              :min="1"
              :max="state.selectedSku.stock"
              :step="1"
              v-model="state.selectedSku.goods_num"
              @change="onNumberChange($event)"
            />
          </view>
        </scroll-view>
      </view>

      <!-- 操作区 -->
      <view class="modal-footer border-top">
        <view class="buy-box ss-flex ss-col-center ss-flex ss-col-center ss-row-center">
          <button class="ss-reset-button add-btn ui-Shadow-Main" @tap="onAddCart"
            >加入购物车</button
          >
          <button class="ss-reset-button buy-btn ui-Shadow-Main" @tap="onBuy">立即购买</button>
        </view>
      </view>
    </view>
  </su-popup>
</template>

<script setup>
  import { computed, reactive, watch } from 'vue';
  import sheep from '@/sheep';
  import { formatStock, convertProductPropertyList, fen2yuan } from '@/sheep/hooks/useGoods';

  const emits = defineEmits(['change', 'addCart', 'buy', 'close']);
  const props = defineProps({
    goodsInfo: {
      type: Object,
      default() {},
    },
    show: {
      type: Boolean,
      default: false,
    },
  });

  const state = reactive({
    selectedSku: {}, // 选中的 SKU
    currentPropertyArray: {}, // 当前选中的属性，实际是个 Map。key 是 property 编号，value 是 value 编号
  });

  const propertyList = convertProductPropertyList(props.goodsInfo.skus);
  // SKU 列表
  const skuList = computed(() => {
    let skuPrices = props.goodsInfo.skus;
    for (let price of skuPrices) {
      price.value_id_array = price.properties.map((item) => item.valueId);
    }
    return skuPrices;
  });

  watch(
    () => state.selectedSku,
    (newVal) => {
      emits('change', newVal);
    },
    {
      immediate: true, // 立即执行
      deep: true, // 深度监听
    },
  );

  // 输入框改变数量
  function onNumberChange(e) {
    if (e === 0) return;
    if (state.selectedSku.goods_num === e) return;
    state.selectedSku.goods_num = e;
  }

  // 加入购物车
  function onAddCart() {
    if (state.selectedSku.id <= 0) {
      sheep.$helper.toast('请选择规格');
      return;
    }
    if (state.selectedSku.stock <= 0) {
      sheep.$helper.toast('库存不足');
      return;
    }

    emits('addCart', state.selectedSku);
  }

  // 立即购买
  function onBuy() {
    if (state.selectedSku.id <= 0) {
      sheep.$helper.toast('请选择规格');
      return;
    }
    if (state.selectedSku.stock <= 0) {
      sheep.$helper.toast('库存不足');
      return;
    }
    emits('buy', state.selectedSku);
  }

  // 改变禁用状态：计算每个 property 属性值的按钮，是否禁用
  function changeDisabled(isChecked = false, propertyId = 0, valueId = 0) {
    let newSkus = []; // 所有可以选择的 sku 数组
    if (isChecked) {
      // 情况一：选中 property
      // 获得当前点击选中 property 的、所有可用 SKU
      for (let price of skuList.value) {
        if (price.stock <= 0) {
          continue;
        }
        if (price.value_id_array.indexOf(valueId) >= 0) {
          newSkus.push(price);
        }
      }
    } else {
      // 情况二：取消选中 property
      // 当前所选 property 下，所有可以选择的 SKU
      newSkus = getCanUseSkuList();
    }

    // 所有存在并且有库存未选择的 SKU 的 value 属性值 id
    let noChooseValueIds = [];
    for (let price of newSkus) {
      noChooseValueIds = noChooseValueIds.concat(price.value_id_array);
    }
    noChooseValueIds = Array.from(new Set(noChooseValueIds)); // 去重

    if (isChecked) {
      // 去除当前选中的 value 属性值 id
      let index = noChooseValueIds.indexOf(valueId);
      noChooseValueIds.splice(index, 1);
    } else {
      // 循环去除当前已选择的 value 属性值 id
      Object.entries(state.currentPropertyArray).forEach(([propertyId, currentPropertyId]) => {
        if (currentPropertyId.toString() !== '') {
          return;
        }
        // currentPropertyId 为空是反选 填充的
        let index = noChooseValueIds.indexOf(currentPropertyId);
        if (index >= 0) {
          // currentPropertyId 存在于 noChooseValueIds
          noChooseValueIds.splice(index, 1);
        }
      });
    }

    // 当前已选择的 property 数组
    let choosePropertyIds = [];
    if (!isChecked) {
      // 当前已选择的 property
      Object.entries(state.currentPropertyArray).forEach(([propertyId, currentValueId]) => {
        if (currentValueId !== '') {
          // currentPropertyId 为空是反选 填充的
          choosePropertyIds.push(currentValueId);
        }
      });
    } else {
      // 当前点击选择的 property
      choosePropertyIds = [propertyId];
    }

    for (let propertyIndex in propertyList) {
      // 当前点击的 property、或者取消选择时候，已选中的 property 不进行处理
      if (choosePropertyIds.indexOf(propertyList[propertyIndex]['id']) >= 0) {
        continue;
      }
      // 如果当前 property id 不存在于有库存的 SKU 中，则禁用
      for (let valueIndex in propertyList[propertyIndex]['values']) {
        propertyList[propertyIndex]['values'][valueIndex]['disabled'] =
          noChooseValueIds.indexOf(propertyList[propertyIndex]['values'][valueIndex]['id']) < 0; // true 禁用 or false 不禁用
      }
    }
  }

  // 当前所选属性下，获取所有有库存的 SKU 们
  function getCanUseSkuList() {
    let newSkus = [];
    for (let sku of skuList.value) {
      if (sku.stock <= 0) {
        continue;
      }
      let isOk = true;
      Object.entries(state.currentPropertyArray).forEach(([propertyId, valueId]) => {
        // valueId 不为空，并且，这个 条 sku 没有被选中，则排除
        if (valueId.toString() !== '' && sku.value_id_array.indexOf(valueId) < 0) {
          isOk = false;
        }
      });
      if (isOk) {
        newSkus.push(sku);
      }
    }
    return newSkus;
  }

  // 选择规格
  function onSelectSku(propertyId, valueId) {
    // 清空已选择
    let isChecked = true; // 选中 or 取消选中
    if (
      state.currentPropertyArray[propertyId] !== undefined &&
      state.currentPropertyArray[propertyId] === valueId
    ) {
      // 点击已被选中的，删除并填充 ''
      isChecked = false;
      state.currentPropertyArray.splice(propertyId, 1, '');
    } else {
      // 选中
      state.currentPropertyArray[propertyId] = valueId;
    }

    // 选中的 property 大类
    let choosePropertyId = [];
    Object.entries(state.currentPropertyArray).forEach(([propertyId, currentPropertyId]) => {
      if (currentPropertyId !== '') {
        // currentPropertyId 为空是反选 填充的
        choosePropertyId.push(currentPropertyId);
      }
    });

    // 当前所选 property 下，所有可以选择的 SKU 们
    let newSkuList = getCanUseSkuList();

    // 判断所有 property 大类是否选择完成
    if (choosePropertyId.length === propertyList.length && newSkuList.length) {
      newSkuList[0].goods_num = state.selectedSku.goods_num || 1;
      state.selectedSku = newSkuList[0];
    } else {
      state.selectedSku = {};
    }

    // 改变 property 禁用状态
    changeDisabled(isChecked, propertyId, valueId);
  }

  changeDisabled(false);
  // TODO 芋艿：待讨论的优化点：1）单规格，要不要默认选中；2）默认要不要选中第一个规格
</script>

<style lang="scss" scoped>
  // 购买
  .buy-box {
    padding: 10rpx 0;

    .add-btn {
      width: 356rpx;
      height: 80rpx;
      border-radius: 40rpx 0 0 40rpx;
      background-color: var(--ui-BG-Main-light);
      color: var(--ui-BG-Main);
    }

    .buy-btn {
      width: 356rpx;
      height: 80rpx;
      border-radius: 0 40rpx 40rpx 0;
      background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
      color: #fff;
    }

    .score-btn {
      width: 100%;
      margin: 0 20rpx;
      height: 80rpx;
      border-radius: 40rpx;
      background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
      color: #fff;
    }
  }

  .ss-modal-box {
    border-radius: 30rpx 30rpx 0 0;
    max-height: 1000rpx;

    .modal-header {
      position: relative;
      padding: 80rpx 20rpx 40rpx;

      .sku-image {
        width: 160rpx;
        height: 160rpx;
        border-radius: 10rpx;
      }

      .header-right {
        height: 160rpx;
      }

      .close-icon {
        position: absolute;
        top: 10rpx;
        right: 20rpx;
        font-size: 46rpx;
        opacity: 0.2;
      }

      .goods-title {
        font-size: 28rpx;
        font-weight: 500;
        line-height: 42rpx;
      }

      .score-img {
        width: 36rpx;
        height: 36rpx;
        margin: 0 4rpx;
      }

      .score-text {
        font-size: 30rpx;
        font-weight: 500;
        color: $red;
        font-family: OPPOSANS;
      }

      .price-text {
        font-size: 30rpx;
        font-weight: 500;
        color: $red;
        font-family: OPPOSANS;

        &::before {
          content: '￥';
          font-size: 30rpx;
          font-weight: 500;
          color: $red;
        }
      }

      .stock-text {
        font-size: 26rpx;
        color: #999999;
      }
    }

    .modal-content {
      padding: 0 20rpx;

      .modal-content-scroll {
        max-height: 600rpx;

        .label-text {
          font-size: 26rpx;
          font-weight: 500;
        }

        .buy-num-box {
          height: 100rpx;
        }

        .spec-btn {
          height: 60rpx;
          min-width: 100rpx;
          padding: 0 30rpx;
          background: #f4f4f4;
          border-radius: 30rpx;
          color: #434343;
          font-size: 26rpx;
          margin-right: 10rpx;
          margin-bottom: 10rpx;
        }

        .disabled-btn {
          font-weight: 400;
          color: #c6c6c6;
          background: #f8f8f8;
        }
      }
    }
  }

  .iconBox {
    width: fit-content;
    height: fit-content;
    padding: 2rpx 10rpx;
    background-color: rgb(255, 242, 241);
    color: #ff2621;
    font-size: 24rpx;
    margin-left: 5rpx;
  }

  .origin-price-text {
    font-size: 26rpx;
    font-weight: 400;
    text-decoration: line-through;
    color: $gray-c;
    font-family: OPPOSANS;

    &::before {
      content: '￥';
    }
  }
</style>
