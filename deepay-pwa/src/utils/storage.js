export const storage = {
  get:    (key, fallback = null) => { try { const v = localStorage.getItem(key); return v !== null ? JSON.parse(v) : fallback } catch { return fallback } },
  set:    (key, value)           => { try { localStorage.setItem(key, JSON.stringify(value)) } catch {} },
  remove: (key)                  => { try { localStorage.removeItem(key) } catch {} },
  clear:  ()                     => { try { localStorage.clear() } catch {} },
}
export const uid = () => {
  let id = storage.get('deepay_uid')
  if (!id) { id = 'u_' + Math.random().toString(36).slice(2,10) + '_' + Date.now().toString(36); storage.set('deepay_uid', id) }
  return id
}
export const isLoggedIn = () => !!storage.get('deepay_token')
