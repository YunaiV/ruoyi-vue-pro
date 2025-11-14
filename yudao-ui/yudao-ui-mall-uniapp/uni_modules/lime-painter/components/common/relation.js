const styles = (v ='') =>  v.split(';').filter(v => v && !/^[\n\s]+$/.test(v)).map(v => {
						const key = v.slice(0, v.indexOf(':'))
						const value = v.slice(v.indexOf(':')+1)
						return {
							[key
								.replace(/-([a-z])/g, function() { return arguments[1].toUpperCase()})
								.replace(/\s+/g, '')
							]: value.replace(/^\s+/, '').replace(/\s+$/, '') || ''
						}
					})
export function parent(parent) {
	return {
		provide() {
			return {
				[parent]: this
			}
		},
		data() {
			return {
				el: {
					id: null,
					css: {},
					views: []
				},
			}
		},
		watch: {
			css: { 
				handler(v) {
					if(this.canvasId) {
						this.el.css = (typeof v == 'object' ? v : v && Object.assign(...styles(v))) || {}
						this.canvasWidth = this.el.css && this.el.css.width || this.canvasWidth
						this.canvasHeight = this.el.css && this.el.css.height || this.canvasHeight
					}
				},
				immediate: true
			}
		}
	}
}
export function children(parent, options = {}) {
	const indexKey = options.indexKey || 'index'
	return {
		inject: {
			[parent]: {
				default: null
			}
		},
		watch: {
			el: {
				handler(v, o) {
					if(JSON.stringify(v) != JSON.stringify(o))
						this.bindRelation()
				},
				deep: true,
				immediate: true
			},
			src: {
				handler(v, o) {
					if(v != o)
						this.bindRelation()
				},
				immediate: true
			},
			text: {
				handler(v, o) {
					if(v != o) this.bindRelation()
				},
				immediate: true
			},
			css: {
				handler(v, o) {
					if(v != o)
						this.el.css = (typeof v == 'object' ? v : v && Object.assign(...styles(v))) || {}
				},
				immediate: true
			},
			replace: {
				handler(v, o) {
					if(JSON.stringify(v) != JSON.stringify(o))
						this.bindRelation()
				},
				deep: true,
				immediate: true
			}
		},
		created() {
			if(!this._uid) {
				this._uid = this._.uid
			}
			Object.defineProperty(this, 'parent', {
				get: () => this[parent] || [],
			})
			Object.defineProperty(this, 'index', {
				get: () =>  {
					this.bindRelation();
					const {parent: {el: {views=[]}={}}={}} = this
					return views.indexOf(this.el)
				},
			});
			this.el.type = this.type
			if(this.uid) {
				this.el.uid = this.uid
			}
			this.bindRelation()
		},
		// #ifdef VUE3
		beforeUnmount() {
			this.removeEl()
		},
		// #endif
		// #ifdef VUE2
		beforeDestroy() {
			this.removeEl()
		},
		// #endif
		methods: {
			removeEl() {
				if (this.parent) {
					this.parent.el.views = this.parent.el.views.filter(
						(item) => item._uid !== this._uid
					);
				}
			},
			bindRelation() {
				if(!this.el._uid) {
					this.el._uid = this._uid 
				}
				if(['text','qrcode'].includes(this.type)) {
					this.el.text = this.$slots && this.$slots.default && this.$slots.default[0].text || `${this.text || ''}`.replace(/\\n/g, '\n')
				}
				if(this.type == 'image') {
					this.el.src = this.src
				}
				if (!this.parent) {
					return;
				}
				let views = this.parent.el.views || [];
				if(views.indexOf(this.el) !== -1) {
					this.parent.el.views = views.map(v => v._uid == this._uid ? this.el : v)
				} else {
					this.parent.el.views = [...views, this.el];
				}
			}
		},
		mounted() {
			// this.bindRelation()
		},
	}
}