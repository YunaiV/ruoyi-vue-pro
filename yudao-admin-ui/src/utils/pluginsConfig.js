const CDN = 'https://lib.baomitu.com/' // CDN Homepage: https://cdn.baomitu.com/
const publicPath = process.env.BASE_URL

function splicingPluginUrl(PluginName, version, fileName) {
  return `${CDN}${PluginName}/${version}/${fileName}`
}

export default {
  beautifierUrl: splicingPluginUrl('js-beautify', '1.13.5', 'beautifier.min.js'),
  // monacoEditorUrl: splicingPluginUrl('monaco-editor', '0.19.3', 'min/vs'), // 使用 monaco-editor CDN 链接
  monacoEditorUrl: `${publicPath}libs/monaco-editor/vs`, // 使用 monaco-editor 本地代码
  tinymceUrl: splicingPluginUrl('tinymce', '5.7.0', 'tinymce.min.js')
}
