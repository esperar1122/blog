// Vetur配置文件
module.exports = {
  // Vetur 0.x 配置
  projects: [
    {
      // 项目根目录
      root: '.',
      // 项目名
      name: 'blog-frontend',
      // tsconfig.json路径
      tsconfig: './tsconfig.json',
      // vetur特定的配置
      vetur: {
        // 使用workspace版本
        useWorkspaceDependencies: true,
        // 启用实验性功能
        experimental: {
          templateInterpolationService: true,
        },
      },
    },
  ],
  // 全局设置
  settings: {
    // 启用类型检查
    'vetur.validation.template': true,
    'vetur.validation.script': true,
    'vetur.validation.style': true,
    // 启用完整的类型检查
    'vetur.validation.interpolation': true,
    // TypeScript支持
    'vetur.completion.autoImport': true,
    'vetur.completion.scaffoldSnippet': true,
  },
}