export const styleImportPlugin = {
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
}
