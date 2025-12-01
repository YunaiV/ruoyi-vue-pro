<script setup lang="ts">
import type { Recordable } from '@vben/types';

import { provide, ref } from 'vue';

import { ElCol, ElEmpty, ElRow, ElTabPane, ElTabs } from 'element-plus';

import audioBar from './audioBar/index.vue';
import songCard from './songCard/index.vue';
import songInfo from './songInfo/index.vue';

defineOptions({ name: 'AiMusicListIndex' });

const currentType = ref('mine');
const loading = ref(false); // loading 状态
const currentSong = ref({}); // 当前音乐
const mySongList = ref<Recordable<any>[]>([]);
const squareSongList = ref<Recordable<any>[]>([]);

function generateMusic(_formData: Recordable<any>) {
  loading.value = true;
  setTimeout(() => {
    mySongList.value = Array.from({ length: 20 }, (_, index) => {
      return {
        id: index,
        audioUrl: '',
        videoUrl: '',
        title: `我走后${index}`,
        imageUrl:
          'https://www.carsmp3.com/data/attachment/forum/201909/19/091020q5kgre20fidreqyt.jpg',
        desc: 'Metal, symphony, film soundtrack, grand, majesticMetal, dtrack, grand, majestic',
        date: '2024年04月30日 14:02:57',
        lyric: `<div class="_words_17xen_66"><div>大江东去，浪淘尽，千古风流人物。
          </div><div>故垒西边，人道是，三国周郎赤壁。
          </div><div>乱石穿空，惊涛拍岸，卷起千堆雪。
          </div><div>江山如画，一时多少豪杰。
          </div><div>
          </div><div>遥想公瑾当年，小乔初嫁了，雄姿英发。
          </div><div>羽扇纶巾，谈笑间，樯橹灰飞烟灭。
          </div><div>故国神游，多情应笑我，早生华发。
          </div><div>人生如梦，一尊还酹江月。</div></div>`,
      };
    });
    loading.value = false;
  }, 3000);
}

function setCurrentSong(music: Recordable<any>) {
  currentSong.value = music;
}

defineExpose({
  generateMusic,
});

provide('currentSong', currentSong);
</script>

<template>
  <div class="flex flex-col">
    <div class="flex flex-auto overflow-hidden">
      <ElTabs
        v-model="currentType"
        class="flex-auto px-5"
        tab-position="bottom"
      >
        <!-- 我的创作 -->
        <ElTabPane name="mine" label="我的创作" v-loading="loading">
          <ElRow v-if="mySongList.length > 0" :gutter="12">
            <ElCol v-for="song in mySongList" :key="song.id" :span="24">
              <songCard :song-info="song" @play="setCurrentSong(song)" />
            </ElCol>
          </ElRow>
          <ElEmpty v-else description="暂无音乐" />
        </ElTabPane>

        <!-- 试听广场 -->
        <ElTabPane name="square" label="试听广场" v-loading="loading">
          <ElRow v-if="squareSongList.length > 0" :gutter="12">
            <ElCol v-for="song in squareSongList" :key="song.id" :span="24">
              <songCard :song-info="song" @play="setCurrentSong(song)" />
            </ElCol>
          </ElRow>
          <ElEmpty v-else description="暂无音乐" />
        </ElTabPane>
      </ElTabs>
      <!-- songInfo -->
      <songInfo class="flex-none" />
    </div>
    <audioBar class="flex-none" />
  </div>
</template>
<style lang="scss" scoped>
:deep(.el-tabs) {
  .el-tabs__content {
    padding: 0 7px;
    overflow: auto;
  }
}
</style>
