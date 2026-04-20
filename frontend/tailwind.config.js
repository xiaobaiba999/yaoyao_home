/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        cream: '#FFF8F0',
        'pink-soft': '#F5C6C6',
        'pink-light': '#FDE8E8',
        'blue-soft': '#B8D4E3',
        'blue-light': '#DBEAFE',
        'pink-accent': '#E8A0A0',
        'blue-accent': '#93B5CF',
      },
      fontFamily: {
        sans: ['"Inter"', '"PingFang SC"', '"Hiragino Sans GB"', '"Microsoft YaHei"', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
