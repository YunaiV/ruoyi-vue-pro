/**
 * 参考 https://github.com/jspm/vite-plugin-jspm，调整为需要的功能
 */
import type { GeneratorOptions } from '@jspm/generator';
import type { Plugin } from 'vite';

import { Generator } from '@jspm/generator';
import { load } from 'cheerio';
import { minify } from 'html-minifier-terser';

const DEFAULT_PROVIDER = 'jspm.io';

type pluginOptions = GeneratorOptions & {
  debug?: boolean;
  defaultProvider?: 'esm.sh' | 'jsdelivr' | 'jspm.io';
  importmap?: Array<{ name: string; range?: string }>;
};

// async function getLatestVersionOfShims() {
//   const result = await fetch('https://ga.jspm.io/npm:es-module-shims');
//   const version = result.text();
//   return version;
// }

async function getShimsUrl(provide: string) {
  // const version = await getLatestVersionOfShims();
  const version = '1.10.0';

  const shimsSubpath = `dist/es-module-shims.js`;
  const providerShimsMap: Record<string, string> = {
    'esm.sh': `https://esm.sh/es-module-shims@${version}/${shimsSubpath}`,
    // unpkg: `https://unpkg.com/es-module-shims@${version}/${shimsSubpath}`,
    jsdelivr: `https://cdn.jsdelivr.net/npm/es-module-shims@${version}/${shimsSubpath}`,

    // 下面两个CDN不稳定，暂时不用
    'jspm.io': `https://ga.jspm.io/npm:es-module-shims@${version}/${shimsSubpath}`,
  };

  return providerShimsMap[provide] || providerShimsMap[DEFAULT_PROVIDER];
}

let generator: Generator;

async function viteImportMapPlugin(
  pluginOptions?: pluginOptions,
): Promise<Plugin[]> {
  const { importmap } = pluginOptions || {};

  let isSSR = false;
  let isBuild = false;
  let installed = false;
  let installError: Error | null = null;

  const options: pluginOptions = Object.assign(
    {},
    {
      debug: false,
      defaultProvider: 'jspm.io',
      env: ['production', 'browser', 'module'],
      importmap: [],
    },
    pluginOptions,
  );

  generator = new Generator({
    ...options,
    baseUrl: process.cwd(),
  });

  if (options?.debug) {
    (async () => {
      for await (const { message, type } of generator.logStream()) {
        console.log(`${type}: ${message}`);
      }
    })();
  }

  const imports = options.inputMap?.imports ?? {};
  const scopes = options.inputMap?.scopes ?? {};
  const firstLayerKeys = Object.keys(scopes);
  const inputMapScopes: string[] = [];
  firstLayerKeys.forEach((key) => {
    inputMapScopes.push(...Object.keys(scopes[key] || {}));
  });
  const inputMapImports = Object.keys(imports);

  const allDepNames: string[] = [
    ...(importmap?.map((item) => item.name) || []),
    ...inputMapImports,
    ...inputMapScopes,
  ];
  const depNames = new Set<string>(allDepNames);

  const installDeps = importmap?.map((item) => ({
    range: item.range,
    target: item.name,
  }));

  return [
    {
      async config(_, { command, isSsrBuild }) {
        isBuild = command === 'build';
        isSSR = !!isSsrBuild;
      },
      enforce: 'pre',
      name: 'importmap:external',
      resolveId(id) {
        if (isSSR || !isBuild) {
          return null;
        }

        if (!depNames.has(id)) {
          return null;
        }
        return { external: true, id };
      },
    },
    {
      enforce: 'post',
      name: 'importmap:install',
      async resolveId() {
        if (isSSR || !isBuild || installed) {
          return null;
        }
        try {
          installed = true;
          await Promise.allSettled(
            (installDeps || []).map((dep) => generator.install(dep)),
          );
        } catch (error: any) {
          installError = error;
          installed = false;
        }
        return null;
      },
    },
    {
      buildEnd() {
        // 未生成importmap时，抛出错误，防止被turbo缓存
        if (!installed && !isSSR) {
          installError && console.error(installError);
          throw new Error('Importmap installation failed.');
        }
      },
      enforce: 'post',
      name: 'importmap:html',
      transformIndexHtml: {
        async handler(html) {
          if (isSSR || !isBuild) {
            return html;
          }

          const importmapJson = generator.getMap();

          if (!importmapJson) {
            return html;
          }

          const esModuleShimsSrc = await getShimsUrl(
            options.defaultProvider || DEFAULT_PROVIDER,
          );

          const resultHtml = await injectShimsToHtml(
            html,
            esModuleShimsSrc || '',
          );
          html = await minify(resultHtml || html, {
            collapseWhitespace: true,
            minifyCSS: true,
            minifyJS: true,
            removeComments: false,
          });

          return {
            html,
            tags: [
              {
                attrs: {
                  type: 'importmap',
                },
                injectTo: 'head-prepend',
                tag: 'script',
                children: `${JSON.stringify(importmapJson)}`,
              },
            ],
          };
        },
        order: 'post',
      },
    },
  ];
}

async function injectShimsToHtml(html: string, esModuleShimUrl: string) {
  const $ = load(html);

  const $script = $(`script[type='module']`);

  if (!$script) {
    return;
  }

  const entry = $script.attr('src');

  $script.removeAttr('type');
  $script.removeAttr('crossorigin');
  $script.removeAttr('src');
  $script.html(`
if (!HTMLScriptElement.supports || !HTMLScriptElement.supports('importmap')) {
  self.importShim = function () {
      const promise = new Promise((resolve, reject) => {
          document.head.appendChild(
              Object.assign(document.createElement('script'), {
                  src: '${esModuleShimUrl}',
                  crossorigin: 'anonymous',
                  async: true,
                  onload() {
                      if (!importShim.$proxy) {
                          resolve(importShim);
                      } else {
                          reject(new Error('No globalThis.importShim found:' + esModuleShimUrl));
                      }
                  },
                  onerror(error) {
                      reject(error);
                  },
              }),
          );
      });
      importShim.$proxy = true;
      return promise.then((importShim) => importShim(...arguments));
  };
}

var modules = ['${entry}'];
typeof importShim === 'function'
  ? modules.forEach((moduleName) => importShim(moduleName))
  : modules.forEach((moduleName) => import(moduleName));
 `);
  $('body').after($script);
  $('head').remove(`script[type='module']`);
  return $.html();
}

export { viteImportMapPlugin };
