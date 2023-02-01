export const isDevMode = () => {
  const dev = import.meta.env.VITE_DEV
  if (dev && dev === true) {
    return true
  } else {
    return false
  }
}
