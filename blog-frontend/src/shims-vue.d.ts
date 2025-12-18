declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module '@/api/*' {
  const content: any
  export default content
}

declare module '@/components/*' {
  const content: any
  export default content
}

declare module '@/types/*' {
  const content: any
  export default content
}

declare module '@/utils/*' {
  const content: any
  export default content
}

declare module '@/views/*' {
  const content: any
  export default content
}

declare module '@/assets/*' {
  const content: any
  export default content
}