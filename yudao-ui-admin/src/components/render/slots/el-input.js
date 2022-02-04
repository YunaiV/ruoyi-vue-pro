export default {
  prepend(h, conf, key) {
    return <template slot="prepend">{conf.__slot__[key]}</template>
  },
  append(h, conf, key) {
    return <template slot="append">{conf.__slot__[key]}</template>
  }
}
