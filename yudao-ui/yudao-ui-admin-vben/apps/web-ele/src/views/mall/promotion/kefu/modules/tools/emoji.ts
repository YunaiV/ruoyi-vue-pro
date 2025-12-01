import { onMounted, ref } from 'vue';

import { isEmpty } from '@vben/utils';

const emojiList = [
  { name: '[笑掉牙]', file: 'xiaodiaoya.png' },
  { name: '[可爱]', file: 'keai.png' },
  { name: '[冷酷]', file: 'lengku.png' },
  { name: '[闭嘴]', file: 'bizui.png' },
  { name: '[生气]', file: 'shengqi.png' },
  { name: '[惊恐]', file: 'jingkong.png' },
  { name: '[瞌睡]', file: 'keshui.png' },
  { name: '[大笑]', file: 'daxiao.png' },
  { name: '[爱心]', file: 'aixin.png' },
  { name: '[坏笑]', file: 'huaixiao.png' },
  { name: '[飞吻]', file: 'feiwen.png' },
  { name: '[疑问]', file: 'yiwen.png' },
  { name: '[开心]', file: 'kaixin.png' },
  { name: '[发呆]', file: 'fadai.png' },
  { name: '[流泪]', file: 'liulei.png' },
  { name: '[汗颜]', file: 'hanyan.png' },
  { name: '[惊悚]', file: 'jingshu.png' },
  { name: '[困~]', file: 'kun.png' },
  { name: '[心碎]', file: 'xinsui.png' },
  { name: '[天使]', file: 'tianshi.png' },
  { name: '[晕]', file: 'yun.png' },
  { name: '[啊]', file: 'a.png' },
  { name: '[愤怒]', file: 'fennu.png' },
  { name: '[睡着]', file: 'shuizhuo.png' },
  { name: '[面无表情]', file: 'mianwubiaoqing.png' },
  { name: '[难过]', file: 'nanguo.png' },
  { name: '[犯困]', file: 'fankun.png' },
  { name: '[好吃]', file: 'haochi.png' },
  { name: '[呕吐]', file: 'outu.png' },
  { name: '[龇牙]', file: 'ziya.png' },
  { name: '[懵比]', file: 'mengbi.png' },
  { name: '[白眼]', file: 'baiyan.png' },
  { name: '[饿死]', file: 'esi.png' },
  { name: '[凶]', file: 'xiong.png' },
  { name: '[感冒]', file: 'ganmao.png' },
  { name: '[流汗]', file: 'liuhan.png' },
  { name: '[笑哭]', file: 'xiaoku.png' },
  { name: '[流口水]', file: 'liukoushui.png' },
  { name: '[尴尬]', file: 'ganga.png' },
  { name: '[惊讶]', file: 'jingya.png' },
  { name: '[大惊]', file: 'dajing.png' },
  { name: '[不好意思]', file: 'buhaoyisi.png' },
  { name: '[大闹]', file: 'danao.png' },
  { name: '[不可思议]', file: 'bukesiyi.png' },
  { name: '[爱你]', file: 'aini.png' },
  { name: '[红心]', file: 'hongxin.png' },
  { name: '[点赞]', file: 'dianzan.png' },
  { name: '[恶魔]', file: 'emo.png' },
];

export interface Emoji {
  name: string;
  url: string;
}

export function useEmoji() {
  const emojiPathList = ref<any[]>([]);

  /** 加载本地图片 */
  async function initStaticEmoji() {
    const pathList = import.meta.glob('../../asserts/*.{png,jpg,jpeg,svg}');
    for (const path in pathList) {
      const imageModule: any = await pathList[path]?.();
      emojiPathList.value.push({ path, src: imageModule.default });
    }
  }

  /** 初始化 */
  onMounted(async () => {
    if (isEmpty(emojiPathList.value)) {
      await initStaticEmoji();
    }
  });

  /**
   * 将文本中的表情替换成图片
   *
   * @return 替换后的文本
   * @param content 消息内容
   */
  function replaceEmoji(content: string) {
    let newData = content;
    if (typeof newData !== 'object') {
      const reg = /\[(.+?)\]/g; // [] 中括号
      const zhEmojiName = newData.match(reg);
      if (zhEmojiName) {
        zhEmojiName.forEach((item) => {
          const emojiFile = getEmojiFileByName(item);
          newData = newData.replace(
            item,
            `<img style="width: 20px;height: 20px;margin:0 1px 3px 1px;vertical-align: middle;" src="${emojiFile}" alt=""/>`,
          );
        });
      }
    }
    return newData;
  }

  /** 获得所有表情 */
  function getEmojiList(): Emoji[] {
    return emojiList.map((item) => ({
      url: getEmojiFileByName(item.name),
      name: item.name,
    })) as Emoji[];
  }

  function getEmojiFileByName(name: string) {
    for (const emoji of emojiList) {
      if (emoji.name === name) {
        const emojiPath = emojiPathList.value.find(
          (item: { path: string; src: string }) =>
            item.path.includes(emoji.file),
        );
        return emojiPath ? emojiPath.src : undefined;
      }
    }
    return false;
  }

  return { replaceEmoji, getEmojiList };
}
