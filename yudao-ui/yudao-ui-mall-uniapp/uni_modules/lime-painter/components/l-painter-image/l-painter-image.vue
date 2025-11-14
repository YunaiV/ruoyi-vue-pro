<template>
	
</template>

<script>
	import {parent, children} from '../common/relation';
	export default {
		name: 'lime-painter-image',
		mixins:[children('painter')],
		props: {
			id: String,
			css: [String, Object],
			src: String
		},
		data() {
			return {
				type: 'image',
				el: {
					css: {},
					src: null
				},
			}
		}
	}
</script>

<style>
</style>
