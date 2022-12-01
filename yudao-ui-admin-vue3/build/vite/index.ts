import { resolve } from 'path'
import Vue from '@vitejs/plugin-vue'
import VueJsx from '@vitejs/plugin-vue-jsx'
import VueI18n from '@intlify/vite-plugin-vue-i18n'
import WindiCSS from 'vite-plugin-windicss'
import progress from 'vite-plugin-progress'
import EslintPlugin from 'vite-plugin-eslint'
import PurgeIcons from 'vite-plugin-purge-icons'
import { createHtmlPlugin } from 'vite-plugin-html'
import viteCompression from 'vite-plugin-compression'
import vueSetupExtend from 'vite-plugin-vue-setup-extend'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import {
  createStyleImportPlugin,
  ElementPlusResolve,
  VxeTableResolve
} from 'vite-plugin-style-import'
export function createVitePlugins(VITE_APP_TITLE: string) {
  const root = process.cwd()
  // 路径查找
  function pathResolve(dir: string) {
    return resolve(root, '.', dir)
  }
  return [
    Vue(),
    VueJsx(),
    WindiCSS(),
    progress(),
    PurgeIcons(),
    vueSetupExtend(),
    createStyleImportPlugin({
      resolves: [ElementPlusResolve(), VxeTableResolve()],
      libs: [
        {
          libraryName: 'element-plus',
          esModule: true,
          resolveStyle: (name) => {
            return `element-plus/es/components/${name.substring(3)}/style/css`
          }
        },
        {
          libraryName: 'vxe-table',
          esModule: true,
          resolveStyle: (name) => {
            return `vxe-table/es/${name}/style.css`
          }
        }
      ]
    }),
    EslintPlugin({
      cache: false,
      include: ['src/**/*.vue', 'src/**/*.ts', 'src/**/*.tsx'] // 检查的文件
    }),
    VueI18n({
      runtimeOnly: true,
      compositionOnly: true,
      include: [resolve(__dirname, 'src/locales/**')]
    }),
    createSvgIconsPlugin({
      iconDirs: [pathResolve('src/assets/svgs')],
      symbolId: 'icon-[dir]-[name]',
      svgoOptions: true
    }),
    viteCompression({
      verbose: true, // 是否在控制台输出压缩结果
      disable: false, // 是否禁用
      threshold: 10240, // 体积大于 threshold 才会被压缩,单位 b
      algorithm: 'gzip', // 压缩算法,可选 [ 'gzip' , 'brotliCompress' ,'deflate' , 'deflateRaw']
      ext: '.gz', // 生成的压缩包后缀
      deleteOriginFile: true //压缩后是否删除源文件
    }),
    createHtmlPlugin({
      inject: {
        data: {
          title: VITE_APP_TITLE,
          injectScript: `<script src="./inject.js"></script>`
        }
      }
    })
  ]
}
