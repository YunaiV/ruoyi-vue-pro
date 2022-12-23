!(function (t, e) {
    typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = e() : typeof define === 'function'
		&& define.amd ? define(e) : t.dayjs = e()
}(this, () => {
    'use strict'

    const t = 'millisecond'
    const e = 'second'
    const n = 'minute'
    const r = 'hour'
    const i = 'day'
    const s = 'week'
    const u = 'month'
    const a = 'quarter'
    const o = 'year'
    const f = 'date'
    const h = /^(\d{4})[-/]?(\d{1,2})?[-/]?(\d{0,2})[^0-9]*(\d{1,2})?:?(\d{1,2})?:?(\d{1,2})?.?(\d+)?$/
    const c = /\[([^\]]+)]|Y{1,4}|M{1,4}|D{1,2}|d{1,4}|H{1,2}|h{1,2}|a|A|m{1,2}|s{1,2}|Z{1,2}|SSS/g
    const d = {
        name: 'en',
        weekdays: 'Sunday_Monday_Tuesday_Wednesday_Thursday_Friday_Saturday'.split('_'),
        months: 'January_February_March_April_May_June_July_August_September_October_November_December'.split('_')
    }
    const $ = function (t, e, n) {
        const r = String(t)
        return !r || r.length >= e ? t : `${Array(e + 1 - r.length).join(n)}${t}`
    }
    const l = {
        s: $,
        z(t) {
            const e = -t.utcOffset()
            const n = Math.abs(e)
            const r = Math.floor(n / 60)
            const i = n % 60
            return `${(e <= 0 ? '+' : '-') + $(r, 2, '0')}:${$(i, 2, '0')}`
        },
        m: function t(e, n) {
            if (e.date() < n.date()) return -t(n, e)
            const r = 12 * (n.year() - e.year()) + (n.month() - e.month())
            const i = e.clone().add(r, u)
            const s = n - i < 0
            const a = e.clone().add(r + (s ? -1 : 1), u)
            return +(-(r + (n - i) / (s ? i - a : a - i)) || 0)
        },
        a(t) {
            return t < 0 ? Math.ceil(t) || 0 : Math.floor(t)
        },
        p(h) {
            return {
                M: u,
                y: o,
                w: s,
                d: i,
                D: f,
                h: r,
                m: n,
                s: e,
                ms: t,
                Q: a
            }[h] || String(h || '').toLowerCase().replace(/s$/, '')
        },
        u(t) {
            return void 0 === t
        }
    }
    let y = 'en'
    const M = {}
    M[y] = d
    const m = function (t) {
        return t instanceof S
    }
    const D = function (t, e, n) {
        let r
        if (!t) return y
        if (typeof t === 'string') M[t] && (r = t), e && (M[t] = e, r = t)
        else {
            const i = t.name
            M[i] = t, r = i
        }
        return !n && r && (y = r), r || !n && y
    }
    const v = function (t, e) {
        if (m(t)) return t.clone()
        const n = typeof e === 'object' ? e : {}
        return n.date = t, n.args = arguments, new S(n)
    }
    const g = l
    g.l = D, g.i = m, g.w = function (t, e) {
        return v(t, {
            locale: e.$L,
            utc: e.$u,
            x: e.$x,
            $offset: e.$offset
        })
    }
    var S = (function () {
        function d(t) {
            this.$L = D(t.locale, null, !0), this.parse(t)
        }
        const $ = d.prototype
        return $.parse = function (t) {
            this.$d = (function (t) {
                const e = t.date
                const n = t.utc
                if (e === null) return new Date(NaN)
                if (g.u(e)) return new Date()
                if (e instanceof Date) return new Date(e)
                if (typeof e === 'string' && !/Z$/i.test(e)) {
                    const r = e.match(h)
                    if (r) {
                        const i = r[2] - 1 || 0
                        const s = (r[7] || '0').substring(0, 3)
                        return n ? new Date(Date.UTC(r[1], i, r[3] || 1, r[4] || 0, r[5] || 0, r[6] || 0, s)) : new Date(r[1], i, r[3]
								|| 1, r[4] || 0, r[5] || 0, r[6] || 0, s)
                    }
                }
                return new Date(e)
            }(t)), this.$x = t.x || {}, this.init()
        }, $.init = function () {
            const t = this.$d
            this.$y = t.getFullYear(), this.$M = t.getMonth(), this.$D = t.getDate(), this.$W = t.getDay(), this.$H = t.getHours(),
            this.$m = t.getMinutes(), this.$s = t.getSeconds(), this.$ms = t.getMilliseconds()
        }, $.$utils = function () {
            return g
        }, $.isValid = function () {
            return !(this.$d.toString() === 'Invalid Date')
        }, $.isSame = function (t, e) {
            const n = v(t)
            return this.startOf(e) <= n && n <= this.endOf(e)
        }, $.isAfter = function (t, e) {
            return v(t) < this.startOf(e)
        }, $.isBefore = function (t, e) {
            return this.endOf(e) < v(t)
        }, $.$g = function (t, e, n) {
            return g.u(t) ? this[e] : this.set(n, t)
        }, $.unix = function () {
            return Math.floor(this.valueOf() / 1e3)
        }, $.valueOf = function () {
            return this.$d.getTime()
        }, $.startOf = function (t, a) {
            const h = this
            const c = !!g.u(a) || a
            const d = g.p(t)
            const $ = function (t, e) {
                const n = g.w(h.$u ? Date.UTC(h.$y, e, t) : new Date(h.$y, e, t), h)
                return c ? n : n.endOf(i)
            }
            const l = function (t, e) {
                return g.w(h.toDate()[t].apply(h.toDate('s'), (c ? [0, 0, 0, 0] : [23, 59, 59, 999]).slice(e)), h)
            }
            const y = this.$W
            const M = this.$M
            const m = this.$D
            const D = `set${this.$u ? 'UTC' : ''}`
            switch (d) {
            case o:
                return c ? $(1, 0) : $(31, 11)
            case u:
                return c ? $(1, M) : $(0, M + 1)
            case s:
                var v = this.$locale().weekStart || 0
                var S = (y < v ? y + 7 : y) - v
                return $(c ? m - S : m + (6 - S), M)
            case i:
            case f:
                return l(`${D}Hours`, 0)
            case r:
                return l(`${D}Minutes`, 1)
            case n:
                return l(`${D}Seconds`, 2)
            case e:
                return l(`${D}Milliseconds`, 3)
            default:
                return this.clone()
            }
        }, $.endOf = function (t) {
            return this.startOf(t, !1)
        }, $.$set = function (s, a) {
            let h; const c = g.p(s)
            const d = `set${this.$u ? 'UTC' : ''}`
            const $ = (h = {}, h[i] = `${d}Date`, h[f] = `${d}Date`, h[u] = `${d}Month`, h[o] = `${d}FullYear`, h[r] = `${d}Hours`,
            h[n] = `${d}Minutes`, h[e] = `${d}Seconds`, h[t] = `${d}Milliseconds`, h)[c]
            const l = c === i ? this.$D + (a - this.$W) : a
            if (c === u || c === o) {
                const y = this.clone().set(f, 1)
                y.$d[$](l), y.init(), this.$d = y.set(f, Math.min(this.$D, y.daysInMonth())).$d
            } else $ && this.$d[$](l)
            return this.init(), this
        }, $.set = function (t, e) {
            return this.clone().$set(t, e)
        }, $.get = function (t) {
            return this[g.p(t)]()
        }, $.add = function (t, a) {
            let f; const
                h = this
            t = Number(t)
            const c = g.p(a)
            const d = function (e) {
                const n = v(h)
                return g.w(n.date(n.date() + Math.round(e * t)), h)
            }
            if (c === u) return this.set(u, this.$M + t)
            if (c === o) return this.set(o, this.$y + t)
            if (c === i) return d(1)
            if (c === s) return d(7)
            const $ = (f = {}, f[n] = 6e4, f[r] = 36e5, f[e] = 1e3, f)[c] || 1
            const l = this.$d.getTime() + t * $
            return g.w(l, this)
        }, $.subtract = function (t, e) {
            return this.add(-1 * t, e)
        }, $.format = function (t) {
            const e = this
            if (!this.isValid()) return 'Invalid Date'
            const n = t || 'YYYY-MM-DDTHH:mm:ssZ'
            const r = g.z(this)
            const i = this.$locale()
            const s = this.$H
            const u = this.$m
            const a = this.$M
            const o = i.weekdays
            const f = i.months
            const h = function (t, r, i, s) {
                return t && (t[r] || t(e, n)) || i[r].substr(0, s)
            }
            const d = function (t) {
                return g.s(s % 12 || 12, t, '0')
            }
            const $ = i.meridiem || function (t, e, n) {
                const r = t < 12 ? 'AM' : 'PM'
                return n ? r.toLowerCase() : r
            }
            const l = {
                YY: String(this.$y).slice(-2),
                YYYY: this.$y,
                M: a + 1,
                MM: g.s(a + 1, 2, '0'),
                MMM: h(i.monthsShort, a, f, 3),
                MMMM: h(f, a),
                D: this.$D,
                DD: g.s(this.$D, 2, '0'),
                d: String(this.$W),
                dd: h(i.weekdaysMin, this.$W, o, 2),
                ddd: h(i.weekdaysShort, this.$W, o, 3),
                dddd: o[this.$W],
                H: String(s),
                HH: g.s(s, 2, '0'),
                h: d(1),
                hh: d(2),
                a: $(s, u, !0),
                A: $(s, u, !1),
                m: String(u),
                mm: g.s(u, 2, '0'),
                s: String(this.$s),
                ss: g.s(this.$s, 2, '0'),
                SSS: g.s(this.$ms, 3, '0'),
                Z: r
            }
            return n.replace(c, (t, e) => e || l[t] || r.replace(':', ''))
        }, $.utcOffset = function () {
            return 15 * -Math.round(this.$d.getTimezoneOffset() / 15)
        }, $.diff = function (t, f, h) {
            let c; const d = g.p(f)
            const $ = v(t)
            const l = 6e4 * ($.utcOffset() - this.utcOffset())
            const y = this - $
            let M = g.m(this, $)
            return M = (c = {}, c[o] = M / 12, c[u] = M, c[a] = M / 3, c[s] = (y - l) / 6048e5, c[i] = (y - l) / 864e5, c[r] =					y / 36e5, c[n] = y / 6e4, c[e] = y / 1e3, c)[d] || y, h ? M : g.a(M)
        }, $.daysInMonth = function () {
            return this.endOf(u).$D
        }, $.$locale = function () {
            return M[this.$L]
        }, $.locale = function (t, e) {
            if (!t) return this.$L
            const n = this.clone()
            const r = D(t, e, !0)
            return r && (n.$L = r), n
        }, $.clone = function () {
            return g.w(this.$d, this)
        }, $.toDate = function () {
            return new Date(this.valueOf())
        }, $.toJSON = function () {
            return this.isValid() ? this.toISOString() : null
        }, $.toISOString = function () {
            return this.$d.toISOString()
        }, $.toString = function () {
            return this.$d.toUTCString()
        }, d
    }())
    const p = S.prototype
    return v.prototype = p, [
        ['$ms', t],
        ['$s', e],
        ['$m', n],
        ['$H', r],
        ['$W', i],
        ['$M', u],
        ['$y', o],
        ['$D', f]
    ].forEach((t) => {
        p[t[1]] = function (e) {
            return this.$g(e, t[0], t[1])
        }
    }), v.extend = function (t, e) {
        return t.$i || (t(e, S, v), t.$i = !0), v
    }, v.locale = D, v.isDayjs = m, v.unix = function (t) {
        return v(1e3 * t)
    }, v.en = M[y], v.Ls = M, v.p = {}, v
}))
