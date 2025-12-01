/** TinyMCE 自定义功能：
 * - processrecord 按钮：插入流程记录占位元素
 * - @ 自动补全：插入 mention 占位元素
 */

// @ts-ignore TinyMCE 全局或通过打包器提供
import type { Editor } from 'tinymce';

export interface MentionItem {
  id: string;
  name: string;
}

/** 在编辑器 setup 回调中注册流程记录按钮和 @ 自动补全 */
export function setupTinyPlugins(
  editor: Editor,
  getMentionList: () => MentionItem[],
) {
  // 按钮：流程记录
  editor.ui.registry.addButton('processrecord', {
    text: '流程记录',
    tooltip: '插入流程记录占位',
    onAction: () => {
      // 流程记录占位显示， 仅用于显示。process-print.vue 组件中会替换掉
      editor.insertContent(
        [
          '<div data-w-e-type="process-record" data-w-e-is-void contenteditable="false">',
          '<table class="process-record-table" style="width: 100%; border-collapse: collapse; border: 1px solid;">',
          '<tr><td style="width: 100%; border: 1px solid; text-align: center;" colspan="2">流程记录</td></tr>',
          '<tr>',
          '<td style="width: 25%; border: 1px solid;">节点</td>',
          '<td style="width: 75%; border: 1px solid;">操作</td>',
          '</tr>',
          '</table>',
          '</div>',
        ].join(''),
      );
    },
  });

  // @ 自动补全
  editor.ui.registry.addAutocompleter('bpmMention', {
    trigger: '@',
    minChars: 0,
    columns: 1,
    fetch: (
      pattern: string,
      _maxResults: number,
      _fetchOptions: Record<string, any>,
    ) => {
      const list = getMentionList();
      const keyword = (pattern || '').toLowerCase().trim();
      const data = list
        .filter((i) => i.name.toLowerCase().includes(keyword))
        .map((i) => ({
          value: i.id,
          text: i.name,
        }));
      return Promise.resolve(data);
    },
    onAction: (
      autocompleteApi: any,
      rng: Range,
      value: string,
      _meta: Record<string, any>,
    ) => {
      const list = getMentionList();
      const item = list.find((i) => i.id === value);
      const name = item ? item.name : value;
      const info = encodeURIComponent(JSON.stringify({ id: value }));
      editor.selection.setRng(rng);
      editor.insertContent(
        `<span data-w-e-type="mention" data-info="${info}">@${name}</span>`,
      );
      autocompleteApi.hide();
    },
  });
}
