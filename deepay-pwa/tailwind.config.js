export default {
  content: ['./index.html', './src/**/*.{vue,js,ts}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: { accent: '#1abc9c', 'accent-dark': '#16a085' },
      fontFamily: { sans: ['Inter', 'PingFang SC', 'sans-serif'] },
      screens: { xs: '375px', sm: '640px', md: '768px', lg: '1024px', xl: '1280px' }
    }
  },
  plugins: []
}
