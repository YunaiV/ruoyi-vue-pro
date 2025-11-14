<script setup lang="ts">
import type { Recordable } from '@vben/types';

import { provide, ref } from 'vue';

import { Col, Empty, Row, TabPane, Tabs } from 'ant-design-vue';

import audioBar from './audioBar/index.vue';
import songCard from './songCard/index.vue';
import songInfo from './songInfo/index.vue';

defineOptions({ name: 'AiMusicListIndex' });

const currentType = ref('mine');
// loading 状态
const loading = ref(false);
// 当前音乐
const currentSong = ref({});

const mySongList = ref<Recordable<any>[]>([]);
const squareSongList = ref<Recordable<any>[]>([]);

/*
 *@Description: 调接口生成音乐列表
 *@MethodAuthor: xiaohong
 *@Date: 2024-06-27 17:06:44
 */
function generateMusic(formData: Recordable<any>) {
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

/*
 *@Description: 设置当前播放的音乐
 *@MethodAuthor: xiaohong
 *@Date: 2024-07-19 11:22:33
 */
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
      <Tabs
        v-model:active-key="currentType"
        class="flex-auto px-5"
        tab-position="bottom"
      >
        <!-- 我的创作 -->
        <TabPane key="mine" tab="我的创作" v-loading="loading">
          <Row v-if="mySongList.length > 0" :gutter="12">
            <Col v-for="song in mySongList" :key="song.id" :span="24">
              <songCard :song-info="song" @play="setCurrentSong(song)" />
            </Col>
          </Row>
          <Empty v-else description="暂无音乐" />
        </TabPane>

        <!-- 试听广场 -->
        <TabPane key="square" tab="试听广场" v-loading="loading">
          <Row v-if="squareSongList.length > 0" :gutter="12">
            <Col v-for="song in squareSongList" :key="song.id" :span="24">
              <songCard :song-info="song" @play="setCurrentSong(song)" />
            </Col>
          </Row>
          <Empty v-else description="暂无音乐" />
        </TabPane>
      </Tabs>
      <!-- songInfo -->
      <songInfo class="flex-none" />
    </div>
    <audioBar class="flex-none" />
  </div>
</template>
<style lang="scss" scoped>
:deep(.ant-tabs) {
  .ant-tabs__content {
    padding: 0 7px;
    overflow: auto;
  }
}
</style>
