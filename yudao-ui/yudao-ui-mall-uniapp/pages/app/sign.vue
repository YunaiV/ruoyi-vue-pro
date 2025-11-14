<!-- 签到界面  -->
<template>
  <s-layout title="签到有礼">
    <s-empty v-if="state.loading" icon="/static/data-empty.png" text="签到活动还未开始" />
    <view v-if="state.loading" />
    <view class="sign-wrap" v-else-if="!state.loading">
      <!-- 签到日历 -->
      <view class="content-box calendar">
        <view class="sign-everyday ss-flex ss-col-center ss-row-between ss-p-x-30">
          <text class="sign-everyday-title">签到日历</text>
          <view class="sign-num-box">
            已连续签到 <text class="sign-num">{{ state.signInfo.continuousDay }}</text> 天
          </view>
        </view>
        <view
          class="list acea-row row-between-wrapper"
          style="
            padding: 0 30rpx;
            height: 240rpx;
            display: flex;
            justify-content: space-between;
            align-items: center;
          "
        >
          <view class="item" v-for="(item, index) in state.signConfigList" :key="index">
            <view
              :class="
                (index === state.signConfigList.length ? 'reward' : '') +
                ' ' +
                (state.signInfo.continuousDay >= item.day ? 'rewardTxt' : '')
              "
            >
              第{{ item.day }}天
            </view>
            <view
              class="venus"
              :class="
                (index + 1 === state.signConfigList.length ? 'reward' : '') +
                ' ' +
                (state.signInfo.continuousDay >= item.day ? 'venusSelect' : '')
              "
            >
            </view>
            <view class="num" :class="state.signInfo.continuousDay >= item.day ? 'on' : ''">
              + {{ item.point }}
            </view>
          </view>
        </view>

        <!-- 签到按钮 -->
        <view class="myDateTable">
          <view class="ss-flex ss-col-center ss-row-center sign-box ss-m-y-40">
            <button
              class="ss-reset-button sign-btn"
              v-if="!state.signInfo.todaySignIn"
              @tap="onSign"
            >
              签到
            </button>
            <button class="ss-reset-button already-btn" v-else disabled> 已签到 </button>
          </view>
        </view>
      </view>

      <!-- 签到说明 -->
      <view class="bg-white ss-m-t-16 ss-p-t-30 ss-p-b-60 ss-p-x-40">
        <view class="activity-title ss-m-b-30">签到说明</view>
        <view class="activity-des">1.已累计签到{{ state.signInfo.totalDay }}天</view>
        <view class="activity-des">
          2.据说连续签到第 {{ state.maxDay }} 天可获得超额积分，要坚持签到哦~~
        </view>
        <view class="activity-des"> 3.积分可以在购物时抵现金结算的哦 ~~</view>
      </view>
    </view>

    <!-- 签到结果弹窗 -->
    <su-popup :show="state.showModel" type="center" round="10" :isMaskClick="false">
      <view class="model-box ss-flex-col">
        <view class="ss-m-t-56 ss-flex-col ss-col-center">
          <text class="cicon-check-round"></text>
          <view class="score-title">
            <text v-if="state.signResult.point">{{ state.signResult.point }} 积分 </text>
            <text v-if="state.signResult.experience"> {{ state.signResult.experience }} 经验</text>
          </view>
          <view class="model-title ss-flex ss-col-center ss-m-t-22 ss-m-b-30">
            已连续打卡 {{ state.signResult.day }} 天
          </view>
        </view>
        <view class="model-bg ss-flex-col ss-col-center ss-row-right">
          <view class="title ss-m-b-64">签到成功</view>
          <view class="ss-m-b-40">
            <button class="ss-reset-button confirm-btn" @tap="onConfirm">确认</button>
          </view>
        </view>
      </view>
    </su-popup>
  </s-layout>
</template>

<script setup>
  import sheep from '@/sheep';
  import { onReady } from '@dcloudio/uni-app';
  import { reactive } from 'vue';
  import SignInApi from '@/sheep/api/member/signin';

  const headerBg = sheep.$url.css('/static/img/shop/app/sign.png');

  const state = reactive({
    loading: true,

    signInfo: {}, // 签到信息

    signConfigList: [], // 签到配置列表
    maxDay: 0, // 最大的签到天数

    showModel: false, // 签到弹框
    signResult: {}, // 签到结果
  });

  // 发起签到
  async function onSign() {
    const { code, data } = await SignInApi.createSignInRecord();
    if (code !== 0) {
      return;
    }
    state.showModel = true;
    state.signResult = data;
    // 重新获得签到信息
    await getSignInfo();
  }

  // 签到确认刷新页面
  function onConfirm() {
    state.showModel = false;
  }

  // 获得个人签到统计
  async function getSignInfo() {
    const { code, data } = await SignInApi.getSignInRecordSummary();
    if (code !== 0) {
      return;
    }
    state.signInfo = data;
    state.loading = false;
  }

  // 获取签到配置
  async function getSignConfigList() {
    const { code, data } = await SignInApi.getSignInConfigList();
    if (code !== 0) {
      return;
    }
    state.signConfigList = data;
    if (data.length > 0) {
      state.maxDay = data[data.length - 1].day;
    }
  }

  onReady(() => {
    getSignInfo();
    getSignConfigList();
  });
</script>

<style lang="scss" scoped>
  .header-box {
    border-top: 2rpx solid rgba(#dfdfdf, 0.5);
  }

  // 日历
  .calendar {
    background: #fff;

    .sign-everyday {
      height: 100rpx;
      background: rgba(255, 255, 255, 1);
      border: 2rpx solid rgba(223, 223, 223, 0.4);

      .sign-everyday-title {
        font-size: 32rpx;
        color: rgba(51, 51, 51, 1);
        font-weight: 500;
      }

      .sign-num-box {
        font-size: 26rpx;
        font-weight: 500;
        color: rgba(153, 153, 153, 1);

        .sign-num {
          font-size: 30rpx;
          font-weight: 600;
          color: #ff6000;
          padding: 0 10rpx;
          font-family: OPPOSANS;
        }
      }
    }

    // 年月日
    .bar {
      height: 100rpx;

      .date {
        font-size: 30rpx;
        font-family: OPPOSANS;
        font-weight: 500;
        color: #333333;
        line-height: normal;
      }
    }

    .cicon-back {
      margin-top: 6rpx;
      font-size: 30rpx;
      color: #c4c4c4;
      line-height: normal;
    }

    .cicon-forward {
      margin-top: 6rpx;
      font-size: 30rpx;
      color: #c4c4c4;
      line-height: normal;
    }

    // 星期
    .week {
      .week-item {
        font-size: 24rpx;
        font-weight: 500;
        color: rgba(153, 153, 153, 1);
        flex: 1;
      }
    }

    // 日历表
    .myDateTable {
      display: flex;
      flex-wrap: wrap;

      .dateCell {
        width: calc(750rpx / 7);
        height: 80rpx;
        font-size: 26rpx;
        font-weight: 400;
        color: rgba(51, 51, 51, 1);
      }
    }
  }

  .is-sign {
    width: 48rpx;
    height: 48rpx;
    position: relative;

    .is-sign-num {
      font-size: 24rpx;
      font-family: OPPOSANS;
      font-weight: 500;
      line-height: normal;
    }

    .is-sign-image {
      position: absolute;
      left: 0;
      top: 0;
      width: 48rpx;
      height: 48rpx;
    }
  }

  .cell-num {
    font-size: 24rpx;
    font-family: OPPOSANS;
    font-weight: 500;
    color: #333333;
    line-height: normal;
  }

  .cicon-title {
    position: absolute;
    right: -10rpx;
    top: -6rpx;
    font-size: 20rpx;
    color: red;
  }

  // 签到按钮
  .sign-box {
    height: 140rpx;
    width: 100%;

    .sign-btn {
      width: 710rpx;
      height: 80rpx;
      border-radius: 35rpx;
      font-size: 30rpx;
      font-weight: 500;
      box-shadow: 0 0.2em 0.5em rgba(#ff6000, 0.4);
      background: linear-gradient(90deg, #ff6000, #fe832a);
      color: #fff;
    }

    .already-btn {
      width: 710rpx;
      height: 80rpx;
      border-radius: 35rpx;
      font-size: 30rpx;
      font-weight: 500;
    }
  }

  .model-box {
    width: 520rpx;
    // height: 590rpx;
    background: linear-gradient(177deg, #ff6000 0%, #fe832a 100%);
    // background: linear-gradient(177deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    border-radius: 10rpx;

    .cicon-check-round {
      font-size: 70rpx;
      color: #fff;
    }

    .score-title {
      font-size: 34rpx;
      font-family: OPPOSANS;
      font-weight: 500;
      color: #fcff00;
    }

    .model-title {
      font-size: 28rpx;
      font-weight: 500;
      color: #ffffff;
    }

    .model-bg {
      width: 520rpx;
      height: 344rpx;
      background-size: 100% 100%;
      background-image: v-bind(headerBg);
      background-repeat: no-repeat;
      border-radius: 0 0 10rpx 10rpx;

      .title {
        font-size: 34rpx;
        font-weight: bold;
        color: var(--ui-BG-Main-TC);
      }

      .subtitle {
        font-size: 26rpx;
        font-weight: 500;
        color: #999999;
      }

      .cancel-btn {
        width: 220rpx;
        height: 70rpx;
        border: 2rpx solid #ff6000;
        border-radius: 35rpx;
        font-size: 28rpx;
        font-weight: 500;
        color: #ff6000;
        line-height: normal;
        margin-right: 10rpx;
      }

      .confirm-btn {
        width: 220rpx;
        height: 70rpx;
        background: linear-gradient(90deg, #ff6000, #fe832a);
        box-shadow: 0 0.2em 0.5em rgba(#ff6000, 0.4);
        border-radius: 35rpx;
        font-size: 28rpx;
        font-weight: 500;
        color: #ffffff;
        line-height: normal;
      }
    }
  }

  //签到说明
  .activity-title {
    font-size: 32rpx;
    font-weight: 500;
    color: #333333;
    line-height: normal;
  }

  .activity-des {
    font-size: 26rpx;
    font-weight: 500;
    color: #666666;
    line-height: 40rpx;
  }

  .reward {
    background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEsAAAA4CAYAAAC1+AWFAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA3ZpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTQyIDc5LjE2MDkyNCwgMjAxNy8wNy8xMy0wMTowNjozOSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDowYWFmYjU3Mi03MGJhLTRiNDctOTI2Yi0zOThlZDkzZDkxMDkiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6RUEyNzZEMjZEMDFCMTFFOEIzQzhEMjMxNjI1NENDQjciIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6RUEyNzZEMjVEMDFCMTFFOEIzQzhEMjMxNjI1NENDQjciIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTggKFdpbmRvd3MpIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6ZmRjNTM0MmUtNmFkOC1iMDRhLThjZTEtMjk2YWYzM2FkMmUxIiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjBhYWZiNTcyLTcwYmEtNGI0Ny05MjZiLTM5OGVkOTNkOTEwOSIv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');
    width: 75rpx;
    height: 56rpx;
  }

  .rewardTxt {
    width: 74rpx;
    height: 32rpx;
    background-color: #f4b409;
    border-radius: 16rpx;
    font-size: 20rpx;
    color: var(--ui-BG-Main-TC);
    line-height: 32rpx;
    text-align: center;
    padding: 2rpx;
  }

  .venus {
    background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADgAAAA4CAYAAACohjseAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA3ZpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTQyIDc5LjE2MDkyNCwgMjAxNy8wNy8xMy0wMTowNjozOSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDowYWFmYjU3Mi03MGJhLTRiNDctOTI2Yi0zOThlZDkzZDkxMDkiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6RDQ3N0E3RkJEMDFCMTFFODhGODVBOTc1NEM5Nzg2NzkiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6RDQ3N0E3RkFEMDFCMTFFODhGODVBOTc1NEM5Nzg2NzkiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTggKFdpbmRvd3MpIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6ZmRjNTM0MmUtNmFkOC1iMDRhLThjZTEtMjk2YWYzM2FkMmUxIiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjBhYWZiNTcyLTcwYmEtNGI0Ny05MjZiLTM5OGVkOTNkOTEwOSIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PnZClGIAAAT7SURBVHja7JtLcxtFEMdnn1rJUhIrTiS7KLsK/EXwNck3gCMcuUFx4QvACW48buHGMZxJPkiowpjC2LElcKzXSqul/6OdoOB9zezsCCimqmvXtdqd+U33dPf0rq2Tn54zg81KjrGuB75x8FbuddsQWIvkS5IxySA5b5no2DUE94TkKPk7IHmf5JDkUQJdW7MNw623o+Ra698KmAdnDNLeIJwRSHvDcLVDVnYynU771fnLl9eFcLZts+VymQf5iJ6pzfFYJeKgT/IZyTskXdWOPM9jjYbPLMticRyz2Sxk8/lc9XFDksckH1IcDKtq8FOSD6rMIuCCoPHXrBIk/qYDC0MlyO1kTBOSj6uuwXerwPn+63DrrdFo8OsV2ns6nEy3Chwg8lpFyK4OwNrgNGpSvxfFoDzP5etJR8PzsiYETmk+X5BjmpkBrHPGU109TeKqv5X3rT3QQ3ObaPDGRjIZXWZpol+b/cebUUA4iuHw938UoNbk9+zsjP16eqoV4JfjH1uqgLjxe10DiaKIDYZDNqfU7OrqSifjkzxIuwDubV2juLi8ZK7j8oT74uJSJ+BRHqQtC6cS/7A9wtrDvb7v84A9Go2UMyQZSFsWrmz6td4GgyF38a7r8lgGefHiQjnjkYG064ZDmjWktecRnEOCDa9DpjqdTrnUDSk2vJXgsHGdTKa0t5vx/V0Yhvy4WMx5hdf3VqYJDQqTxW+QdmECPM8h8flvPPRFR/QXBEEuKEw9Yz/5AyoDtBkei5zriyw4pGVFmoOHPD7+mQ/ccR1mWzZpyuIacx2Hgzl0FJkIjjBTHBeLiO6LuDZH4zFbRkt63oLt7+8XAmJcIhFP0eTn2C9CgzBTrPjUp7XbW6VSJGjx5OSEBhsz2wGkxc0R967LuukuSWKaFEwMnXJQSL/fZ51Op/QSuL5OdVjY7beFBq2sAlHZ/A8aOTg4YKenv/EBr8Nl5ZUO6jPJRGCg0dJie/d3WbPZlMpPMwpZsN0Y2sOVb7PcOzou22CGe3u7fB3J3Cd+udvvScG9soT0Kt13tAZj4UVRwHmWtZClcj+azX6vxx0GN8ECUFyHjnsEp+KlM8b3TBTKBCDqkA/SIFV20jCbO3duk8OIcgHF7G9ttbinVYFLcTBgeAAP+vc4mAkJVywLibARpZvOjWWA36rApYSI1+DSMhltkJPpRGIyJrXAZeWiWiCnk9kNU4RGl8m5MN1VLFzwWKobLm83kQtZbnMb8lgYJ2aIYH//3g73lEHQ5ECAXYUSu7QWZeCKqmoCcqSyPkQgd8lHbne3uSMRbWenSwNtU476BxtT9oJQOZ3OKKloq6SmmXBlyoZKb3nG4wnXyt1ul9261UkN9ggjvd497mDOz885KGN3pfvKg1OuixY15JCHh2/ymFjUms2AMqD9BFB/qwUQg5ZtrVY9b7H/LxtmZSCbaCr9KgGmpEeG6qpzM2tQBHudb5eKNLfKiUMzgAIyL6uRreHklB9qX4MDlQfLpHUV4AY6AB+rzl4ZyIqa+0aHiX6UlDTwMcK2CqQoEGmEE5+RfFK4N636vWjKh0Cp5ceS38k8JXko8yHQ7W7PXKBPBvYwGegNF4/q12g05mV7HXClqgs1ffEr+/LmaTIx0nCb+uI3U5M64Tadi5aBrBXORLKdB1k7nKndhID8GqUaErze/coEXJ1OJm9CY2bw3wr+FGAAoa6PIUihovYAAAAASUVORK5CYII=');
    background-repeat: no-repeat;
    background-size: 100% 100%;
    width: 56rpx;
    height: 56rpx;
    margin: 10rpx auto;
  }

  .venusSelect {
    background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADgAAAA4CAYAAACohjseAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA3ZpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTQyIDc5LjE2MDkyNCwgMjAxNy8wNy8xMy0wMTowNjozOSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDowYWFmYjU3Mi03MGJhLTRiNDctOTI2Yi0zOThlZDkzZDkxMDkiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QzkwRkI4NEFEMDFCMTFFODhDNDdBMDVGOTBBN0U2NTQiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QzkwRkI4NDlEMDFCMTFFODhDNDdBMDVGOTBBN0U2NTQiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTggKFdpbmRvd3MpIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6ZmRjNTM0MmUtNmFkOC1iMDRhLThjZTEtMjk2YWYzM2FkMmUxIiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjBhYWZiNTcyLTcwYmEtNGI0Ny05MjZiLTM5OGVkOTNkOTEwOSIv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');
  }

  .num {
    font-size: 36rpx;
    font-family: 'Guildford Pro';
  }

  .item {
    align-items: center;
    justify-content: space-between;
    flex-direction: column;
    height: 130rpx;
  }

  .reward {
    background-image: url('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEsAAAA4CAYAAAC1+AWFAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA3ZpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTQyIDc5LjE2MDkyNCwgMjAxNy8wNy8xMy0wMTowNjozOSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDowYWFmYjU3Mi03MGJhLTRiNDctOTI2Yi0zOThlZDkzZDkxMDkiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6RUEyNzZEMjZEMDFCMTFFOEIzQzhEMjMxNjI1NENDQjciIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6RUEyNzZEMjVEMDFCMTFFOEIzQzhEMjMxNjI1NENDQjciIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTggKFdpbmRvd3MpIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6ZmRjNTM0MmUtNmFkOC1iMDRhLThjZTEtMjk2YWYzM2FkMmUxIiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjBhYWZiNTcyLTcwYmEtNGI0Ny05MjZiLTM5OGVkOTNkOTEwOSIv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');
    width: 75rpx;
    height: 56rpx;
  }

  .on {
    color: #f4b409;
  }
</style>
