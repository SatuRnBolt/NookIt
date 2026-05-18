import { defineStore } from 'pinia'
import { ref } from 'vue'

export const THEME_LIST = [
  {
    id: 'blue',
    name: '经典蓝',
    primary:        '#1a5cc8',
    primaryDark:    '#003893',
    primaryLight:   '#eef3ff',
    primaryBorder:  '#d0def8',
    primaryShadow:  'rgba(26,92,200,0.2)',
    elLight3:       '#6a9ae0',
    elLight5:       '#d0def8',
    elLight7:       '#e4eefb',
    elLight9:       '#eef3ff',
  },
  {
    id: 'green',
    name: '自然绿',
    primary:        '#17bf6a',
    primaryDark:    '#0d9e55',
    primaryLight:   '#edfdf5',
    primaryBorder:  '#a7f3d0',
    primaryShadow:  'rgba(23,191,106,0.2)',
    elLight3:       '#7de0b0',
    elLight5:       '#a7f3d0',
    elLight7:       '#cff8e7',
    elLight9:       '#edfdf5',
  },
  {
    id: 'red',
    name: '热情红',
    primary:        '#cc1a1a',
    primaryDark:    '#9e1414',
    primaryLight:   '#fff0f0',
    primaryBorder:  '#fca5a5',
    primaryShadow:  'rgba(204,26,26,0.2)',
    elLight3:       '#e87070',
    elLight5:       '#fca5a5',
    elLight7:       '#fdd5d5',
    elLight9:       '#fff0f0',
  },
]

export const useThemeStore = defineStore('theme', () => {
  const theme = ref(localStorage.getItem('nookit_theme') || 'blue')

  function apply(id) {
    const t = THEME_LIST.find(t => t.id === id)
    if (!t) return
    const root = document.documentElement
    root.style.setProperty('--nt-primary',        t.primary)
    root.style.setProperty('--nt-primary-dark',   t.primaryDark)
    root.style.setProperty('--nt-primary-light',  t.primaryLight)
    root.style.setProperty('--nt-primary-border', t.primaryBorder)
    root.style.setProperty('--nt-primary-shadow', t.primaryShadow)
    root.style.setProperty('--el-color-primary',          t.primary)
    root.style.setProperty('--el-color-primary-dark-2',   t.primaryDark)
    root.style.setProperty('--el-color-primary-light-3',  t.elLight3)
    root.style.setProperty('--el-color-primary-light-5',  t.elLight5)
    root.style.setProperty('--el-color-primary-light-7',  t.elLight7)
    root.style.setProperty('--el-color-primary-light-9',  t.elLight9)
  }

  function setTheme(id) {
    theme.value = id
    localStorage.setItem('nookit_theme', id)
    apply(id)
  }

  apply(theme.value)

  return { theme, themeList: THEME_LIST, setTheme }
})
