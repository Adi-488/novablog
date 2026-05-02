/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      colors: {
        surface: {
          DEFAULT: '#f4f5f1',
          dim: '#e6e7e3',
          bright: '#ffffff',
          'container-lowest': '#ffffff',
          'container-low': '#fafafa',
          container: '#f5f5f5',
          'container-high': '#ebebeb',
          'container-highest': '#e0e0e0',
        },
        'on-surface': {
          DEFAULT: '#1a1a1a',
          variant: '#666666',
        },
        primary: {
          DEFAULT: '#1a1a1a',
          container: '#dcfce7',
        },
        'on-primary': {
          DEFAULT: '#ffffff',
          container: '#166534',
        },
        secondary: {
          DEFAULT: '#ffffff',
          container: '#e0f2fe',
        },
        'on-secondary': {
          DEFAULT: '#1a1a1a',
          container: '#075985',
        },
        accent: {
          1: '#dcfce7',
          2: '#e0f2fe',
          3: '#fae8ff',
          4: '#fef08a',
          5: '#e2e8f0',
        }
      },
      borderRadius: {
        '2xl': '2.5rem',
        '3xl': '3rem',
      }
    },
  },
  plugins: [],
}
