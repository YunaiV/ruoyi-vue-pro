<template>
  <view class="uni-file-picker__container">
    <view class="file-picker__box" v-for="(url, index) in list" :key="index" :style="boxStyle">
      <view class="file-picker__box-content" :style="borderStyle">
        <image
          class="file-image"
          :src="getImageUrl(url)"
          mode="aspectFill"
          @click.stop="previewImage(url, index)"
        ></image>
        <view v-if="delIcon && !readonly" class="icon-del-box" @click.stop="delFile(index)">
          <view class="icon-del"></view>
          <view class="icon-del rotate"></view>
        </view>
        <!-- <view v-if="item.errMsg" class="file-picker__mask" @click.stop="uploadFiles(item, index)">
          点击重试
        </view> -->
      </view>
    </view>
    <view v-if="list.length < limit && !readonly" class="file-picker__box" :style="boxStyle">
      <view class="file-picker__box-content is-add" :style="borderStyle" @click="choose">
        <slot>
          <view class="icon-add"></view>
          <view class="icon-add rotate"></view>
        </slot>
      </view>
    </view>
  </view>
</template>

<script>
  import sheep from '@/sheep';
  export default {
    name: 'uploadImage',
    emits: ['uploadFiles', 'choose', 'delFile'],
    props: {
      filesList: {
        type: [Array, String],
        default() {
          return [];
        },
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      disablePreview: {
        type: Boolean,
        default: false,
      },
      limit: {
        type: [Number, String],
        default: 9,
      },
      imageStyles: {
        type: Object,
        default() {
          return {
            width: 'auto',
            height: 'auto',
            border: {},
          };
        },
      },
      delIcon: {
        type: Boolean,
        default: true,
      },
      readonly: {
        type: Boolean,
        default: false,
      },
    },
    computed: {
      list() {
        if (typeof this.filesList === 'string') {
          if (this.filesList) {
            return [this.filesList];
          } else {
            return [];
          }
        }
        return this.filesList;
      },
      styles() {
        let styles = {
          width: 'auto',
          height: 'auto',
          border: {},
        };
        return Object.assign(styles, this.imageStyles);
      },
      boxStyle() {
        const { width = 'auto', height = 'auto' } = this.styles;
        let obj = {};
        if (height === 'auto') {
          if (width !== 'auto') {
            obj.height = this.value2px(width);
            obj['padding-top'] = 0;
          } else {
            obj.height = 0;
          }
        } else {
          obj.height = this.value2px(height);
          obj['padding-top'] = 0;
        }

        if (width === 'auto') {
          if (height !== 'auto') {
            obj.width = this.value2px(height);
          } else {
            obj.width = '33.3%';
          }
        } else {
          obj.width = this.value2px(width);
        }

        let classles = '';
        for (let i in obj) {
          classles += `${i}:${obj[i]};`;
        }
        return classles;
      },
      borderStyle() {
        let { border } = this.styles;
        let obj = {};
        const widthDefaultValue = 1;
        const radiusDefaultValue = 3;
        if (typeof border === 'boolean') {
          obj.border = border ? '1px #eee solid' : 'none';
        } else {
          let width = (border && border.width) || widthDefaultValue;
          width = this.value2px(width);
          let radius = (border && border.radius) || radiusDefaultValue;
          radius = this.value2px(radius);
          obj = {
            'border-width': width,
            'border-style': (border && border.style) || 'solid',
            'border-color': (border && border.color) || '#eee',
            'border-radius': radius,
          };
        }
        let classles = '';
        for (let i in obj) {
          classles += `${i}:${obj[i]};`;
        }
        return classles;
      },
    },
    methods: {
      getImageUrl(url) {
        if ('blob:http:' === url.substr(0, 10)) {
          return url;
        } else {
          return sheep.$url.cdn(url);
        }
      },
      uploadFiles(item, index) {
        this.$emit('uploadFiles', item);
      },
      choose() {
        this.$emit('choose');
      },
      delFile(index) {
        this.$emit('delFile', index);
      },
      previewImage(img, index) {
        let urls = [];
        if (Number(this.limit) === 1 && this.disablePreview && !this.disabled) {
          this.$emit('choose');
        }
        if (this.disablePreview) return;
        this.list.forEach((i) => {
          urls.push(this.getImageUrl(i));
        });

        uni.previewImage({
          urls: urls,
          current: index,
        });
      },
      value2px(value) {
        if (typeof value === 'number') {
          value += 'px';
        } else {
          if (value.indexOf('%') === -1) {
            value = value.indexOf('px') !== -1 ? value : value + 'px';
          }
        }
        return value;
      },
    },
  };
</script>

<style lang="scss">
  .uni-file-picker__container {
    /* #ifndef APP-NVUE */
    display: flex;
    box-sizing: border-box;
    /* #endif */
    flex-wrap: wrap;
    margin: -5px;
  }

  .file-picker__box {
    position: relative;
    // flex: 0 0 33.3%;
    width: 33.3%;
    height: 0;
    padding-top: 33.33%;
    /* #ifndef APP-NVUE */
    box-sizing: border-box;
    /* #endif */
  }

  .file-picker__box-content {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    margin: 5px;
    border: 1px #eee solid;
    border-radius: 5px;
    overflow: hidden;
  }

  .file-picker__progress {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    /* border: 1px red solid; */
    z-index: 2;
  }

  .file-picker__progress-item {
    width: 100%;
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
    font-size: 12px;
    background-color: rgba(0, 0, 0, 0.4);
  }

  .file-image {
    width: 100%;
    height: 100%;
  }

  .is-add {
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    align-items: center;
    justify-content: center;
  }

  .icon-add {
    width: 50px;
    height: 5px;
    background-color: #f1f1f1;
    border-radius: 2px;
  }

  .rotate {
    position: absolute;
    transform: rotate(90deg);
  }

  .icon-del-box {
    /* #ifndef APP-NVUE */
    display: flex;
    /* #endif */
    align-items: center;
    justify-content: center;
    position: absolute;
    top: 3px;
    right: 3px;
    height: 26px;
    width: 26px;
    border-radius: 50%;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 2;
    transform: rotate(-45deg);
  }

  .icon-del {
    width: 15px;
    height: 2px;
    background-color: #fff;
    border-radius: 2px;
  }
</style>
