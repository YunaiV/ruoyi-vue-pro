/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js}'],
  theme: {
    extend: {
      colors: {
        bg:       '#0B0B0B',
        surface:  '#111111',
        surface2: '#1A1A1A',
        border:   '#2A2A2A',
        accent:   '#00FF88',
        muted:    '#9CA3AF',
        danger:   '#FF4444',
      },
      borderRadius: {
        xl:   '12px',
        '2xl':'16px',
        '3xl':'24px',
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'Roboto', 'sans-serif'],
      },
      boxShadow: {
        'glow-sm': '0 0 12px rgba(0,255,136,0.35)',
        'glow':    '0 0 22px rgba(0,255,136,0.45)',
        'card':    '0 0 24px rgba(0,0,0,0.35)',
      },
    },
  },
  plugins: [],
}
