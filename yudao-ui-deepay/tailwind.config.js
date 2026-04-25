/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js}'],
  theme: {
    extend: {
      colors: {
        bg:             '#0a0a0a',
        surface:        '#1a1a1a',
        surface2:       '#222222',
        surface3:       '#2a2a2a',
        surface4:       '#333333',
        border:         '#2a2a2a',
        border2:        '#3a3a3a',
        accent:         '#1abc9c',
        'accent-hover': '#16a085',
        'accent-light': '#22d3b4',
        'accent-glow':  'rgba(26,188,156,0.4)',
        'accent-bg':    'rgba(26,188,156,0.08)',
        text:           '#e0e0e0',
        'text-bright':  '#ffffff',
        'text-sub':     '#a0a0a0',
        muted:          '#666666',
        danger:         '#ef4444',
        warning:        '#f59e0b',
        success:        '#1abc9c',
      },
      borderRadius: {
        xl:    '12px',
        '2xl': '16px',
        '3xl': '24px',
        '4xl': '32px',
      },
      fontFamily: {
        sans: ['Inter', '-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'Roboto', 'sans-serif'],
        mono: ['"JetBrains Mono"', '"Fira Code"', 'monospace'],
      },
      boxShadow: {
        'glow-xs':      '0 0 8px rgba(26,188,156,0.25)',
        'glow-sm':      '0 0 14px rgba(26,188,156,0.35)',
        'glow':         '0 0 24px rgba(26,188,156,0.45)',
        'glow-lg':      '0 0 40px rgba(26,188,156,0.55)',
        'card':         '0 4px 24px rgba(0,0,0,0.5)',
        'card-hover':   '0 8px 40px rgba(0,0,0,0.7)',
        'inset-accent': 'inset 0 0 0 1px rgba(26,188,156,0.3)',
      },
      keyframes: {
        'fade-up': {
          '0%':   { opacity: '0', transform: 'translateY(16px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'fade-in': {
          '0%':   { opacity: '0' },
          '100%': { opacity: '1' },
        },
        'scale-in': {
          '0%':   { opacity: '0', transform: 'scale(0.94)' },
          '100%': { opacity: '1', transform: 'scale(1)' },
        },
        'slide-right': {
          '0%':   { opacity: '0', transform: 'translateX(-12px)' },
          '100%': { opacity: '1', transform: 'translateX(0)' },
        },
        'slide-up': {
          '0%':   { opacity: '0', transform: 'translateY(16px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        'glow-pulse': {
          '0%, 100%': { boxShadow: '0 0 8px rgba(26,188,156,0.3)' },
          '50%':      { boxShadow: '0 0 24px rgba(26,188,156,0.7)' },
        },
        'spin-ring': {
          '0%':   { transform: 'rotate(0deg)' },
          '100%': { transform: 'rotate(360deg)' },
        },
        shimmer: {
          '0%':   { backgroundPosition: '-400% 0' },
          '100%': { backgroundPosition:  '400% 0' },
        },
        'bar-slide': {
          '0%':   { transform: 'translateX(-100%)' },
          '50%':  { transform: 'translateX(-15%)' },
          '100%': { transform: 'translateX(110%)' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%':      { transform: 'translateY(-5px)' },
        },
        'typing-bounce': {
          '0%, 80%, 100%': { transform: 'scale(0)', opacity: '0.4' },
          '40%':           { transform: 'scale(1)', opacity: '1' },
        },
        ping: {
          '0%':   { opacity: '0.8', transform: 'scale(1)' },
          '100%': { opacity: '0',   transform: 'scale(2.2)' },
        },
      },
      animation: {
        'fade-up':       'fade-up 0.5s ease both',
        'fade-in':       'fade-in 0.4s ease both',
        'scale-in':      'scale-in 0.35s ease both',
        'slide-right':   'slide-right 0.35s ease both',
        'slide-up':      'slide-up 0.35s ease both',
        'glow-pulse':    'glow-pulse 2.4s ease-in-out infinite',
        shimmer:         'shimmer 1.8s linear infinite',
        'bar-slide':     'bar-slide 1.6s ease-in-out infinite',
        float:           'float 3s ease-in-out infinite',
        'spin-ring':     'spin-ring 0.8s linear infinite',
        ping:            'ping 1.5s ease-out infinite',
        'typing-bounce': 'typing-bounce 1.4s ease-in-out infinite',
      },
    },
  },
  plugins: [],
}
