import type { IconifyIconStructure } from '@vben-core/icons';

import { addIcon } from '@vben-core/icons';

let loaded = false;
if (!loaded) {
  loadSvgIcons();
  loaded = true;
}

function parseSvg(svgData: string): IconifyIconStructure {
  const parser = new DOMParser();
  const xmlDoc = parser.parseFromString(svgData, 'image/svg+xml');
  const svgElement = xmlDoc.documentElement;

  const svgContent = [...svgElement.childNodes]
    .filter((node) => node.nodeType === Node.ELEMENT_NODE)
    .map((node) => new XMLSerializer().serializeToString(node))
    .join('');

  const viewBoxValue = svgElement.getAttribute('viewBox') || '';
  const [left, top, width, height] = viewBoxValue.split(' ').map((val) => {
    const num = Number(val);
    return Number.isNaN(num) ? undefined : num;
  });

  return {
    body: svgContent,
    height,
    left,
    top,
    width,
  };
}

/**
 * 自定义的svg图片转化为组件
 * @example ./svg/avatar.svg
 * <Icon icon="svg:avatar"></Icon>
 */
async function loadSvgIcons() {
  const svgEagers = import.meta.glob('./icons/**', {
    eager: true,
    query: '?raw',
  });

  await Promise.all(
    Object.entries(svgEagers).map((svg) => {
      const [key, body] = svg as [string, string | { default: string }];

      // ./icons/xxxx.svg => xxxxxx
      const start = key.lastIndexOf('/') + 1;
      const end = key.lastIndexOf('.');
      const iconName = key.slice(start, end);

      return addIcon(`svg:${iconName}`, {
        ...parseSvg(typeof body === 'object' ? body.default : body),
      });
    }),
  );
}
