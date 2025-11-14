<template>
  <view class="uni-file-picker__files">
    <view v-if="!readonly" class="files-button" @click="choose">
      <slot></slot>
    </view>
    <!-- :class="{'is-text-box':showType === 'list'}" -->
    <view v-if="list.length > 0" class="uni-file-picker__lists is-text-box" :style="borderStyle">
      <!-- ,'is-list-card':showType === 'list-card' -->

      <view
        class="uni-file-picker__lists-box"
        v-for="(item, index) in list"
        :key="index"
        :class="{
          'files-border': index !== 0 && styles.dividline,
        }"
        :style="index !== 0 && styles.dividline && borderLineStyle"
      >
        <view class="uni-file-picker__item">
          <!-- :class="{'is-text-image':showType === 'list'}" -->
          <!-- 	<view class="files__image is-text-image">
						<image class="header-image" :src="item.logo" mode="aspectFit"></image>
					</view> -->
          <view class="files__name">{{ item.name }}</view>
          <view v-if="delIcon && !readonly" class="icon-del-box icon-files" @click="delFile(index)">
            <view class="icon-del icon-files"></view>
            <view class="icon-del rotate"></view>
          </view>
        </view>
        <view
          v-if="(item.progress && item.progress !== 100) || item.progress === 0"
          class="file-picker__progress"
        >
          <progress
            class="file-picker__progress-item"
            :percent="item.progress === -1 ? 0 : item.progress"
            stroke-width="4"
            :backgroundColor="item.errMsg ? '#ff5a5f' : '#EBEBEB'"
          />
        </view>
        <view
          v-if="item.status === 'error'"
          class="file-picker__mask"
          @click.stop="uploadFiles(item, index)"
        >
          点击重试
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  export default {
    name: 'uploadFile',
    emits: ['uploadFiles', 'choose', 'delFile'],
    props: {
      filesList: {
        type: Array,
        default() {
          return [];
        },
      },
      delIcon: {
        type: Boolean,
        default: true,
      },
      limit: {
        type: [Number, String],
        default: 9,
      },
      showType: {
        type: String,
        default: '',
      },
      listStyles: {
        type: Object,
        default() {
          return {
            // 是否显示边框
            border: true,
            // 是否显示分隔线
            dividline: true,
            // 线条样式
            borderStyle: {},
          };
        },
      },
      readonly: {
        type: Boolean,
        default: false,
      },
    },
    computed: {
      list() {
        let files = [];
        this.filesList.forEach((v) => {
          files.push(v);
        });
        return files;
      },
      styles() {
        let styles = {
          border: true,
          dividline: true,
          'border-style': {},
        };
        return Object.assign(styles, this.listStyles);
      },
      borderStyle() {
        let { borderStyle, border } = this.styles;
        let obj = {};
        if (!border) {
          obj.border = 'none';
        } else {
          let width = (borderStyle && borderStyle.width) || 1;
          width = this.value2px(width);
          let radius = (borderStyle && borderStyle.radius) || 5;
          radius = this.value2px(radius);
          obj = {
            'border-width': width,
            'border-style': (borderStyle && borderStyle.style) || 'solid',
            'border-color': (borderStyle && borderStyle.color) || '#eee',
            'border-radius': radius,
          };
        }
        let classles = '';
        for (let i in obj) {
          classles += `${i}:${obj[i]};`;
        }
        return classles;
      },
      borderLineStyle() {
        let obj = {};
        let { borderStyle } = this.styles;
        if (borderStyle && borderStyle.color) {
          obj['border-color'] = borderStyle.color;
        }
        if (borderStyle && borderStyle.width) {
          let width = (borderStyle && borderStyle.width) || 1;
          let style = (borderStyle && borderStyle.style) || 0;
          if (typeof width === 'number') {
            width += 'px';
          } else {
            width = width.indexOf('px') ? width : width + 'px';
          }
          obj['border-width'] = width;

          if (typeof style === 'number') {
            style += 'px';
          } else {
            style = style.indexOf('px') ? style : style + 'px';
          }
          obj['border-top-style'] = style;
        }
        let classles = '';
        for (let i in obj) {
          classles += `${i}:${obj[i]};`;
        }
        return classles;
      },
    },

    methods: {
      uploadFiles(item, index) {
        this.$emit('uploadFiles', {
          item,
          index,
        });
      },
      choose() {
        this.$emit('choose');
      },
      delFile(index) {
        this.$emit('delFile', index);
      },
      value2px(value) {
        if (typeof value === 'number') {
          value += 'px';
        } else {
          value = value.indexOf('px') !== -1 ? value : value + 'px';
        }
        return value;
      },
    },
  };
</script>

<style lang="scss">
  .uni-file-picker__files {
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    flex-direction: column;
    justify-content: flex-start;
  }

  .files-button {
    // border: 1px red solid;
  }

  .uni-file-picker__lists {
    position: relative;
    margin-top: 5px;
    overflow: hidden;
  }

  .file-picker__mask {
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    justify-content: center;
    align-items: center;
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    left: 0;
    color: #fff;
    font-size: 14px;
    background-color: rgba(0, 0, 0, 0.4);
  }

  .uni-file-picker__lists-box {
    position: relative;
  }

  .uni-file-picker__item {
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    align-items: center;
    padding: 8px 10px;
    padding-right: 5px;
    padding-left: 10px;
  }

  .files-border {
    border-top: 1px #eee solid;
  }

  .files__name {
    flex: 1;
    font-size: 14px;
    color: #666;
    margin-right: 25px;
    /* #ifndef APP-NVUE */
    word-break: break-all;
    word-wrap: break-word;
    /* #endif */
  }

  .icon-files {
    /* #ifndef APP-NVUE */
    position: static;
    background-color: initial;
    /* #endif */
  }

  // .icon-files .icon-del {
  // 	background-color: #333;
  // 	width: 12px;
  // 	height: 1px;
  // }

  .is-list-card {
    border: 1px #eee solid;
    margin-bottom: 5px;
    border-radius: 5px;
    box-shadow: 0 0 2px 0px rgba(0, 0, 0, 0.1);
    padding: 5px;
  }

  .files__image {
    width: 40px;
    height: 40px;
    margin-right: 10px;
  }

  .header-image {
    width: 100%;
    height: 100%;
  }

  .is-text-box {
    border: 1px #eee solid;
    border-radius: 5px;
  }

  .is-text-image {
    width: 25px;
    height: 25px;
    margin-left: 5px;
  }

  .rotate {
    position: absolute;
    transform: rotate(90deg);
  }

  .icon-del-box {
    /* #ifndef APP-NVUE */
    display: flex;
    margin: auto 0;
    /* #endif */
    align-items: center;
    justify-content: center;
    position: absolute;
    top: 0px;
    bottom: 0;
    right: 5px;
    height: 26px;
    width: 26px;
    // border-radius: 50%;
    // background-color: rgba(0, 0, 0, 0.5);
    z-index: 2;
    transform: rotate(-45deg);
  }

  .icon-del {
    width: 15px;
    height: 1px;
    background-color: #333;
    // border-radius: 1px;
  }

  /* #ifdef H5 */
  @media all and (min-width: 768px) {
    .uni-file-picker__files {
      max-width: 375px;
    }
  }

  /* #endif */
</style>
