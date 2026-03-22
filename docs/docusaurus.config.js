// @ts-check
import { themes as prismThemes } from 'prism-react-renderer';

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Meu Site',
  tagline: 'Documentação incrível',
  favicon: 'img/favicon.ico',

  url: 'https://iisabelledantas.github.io',
  baseUrl: '/PonderadaLista/',
  organizationName: 'iisabelledantas',
  projectName: 'PonderadaLista',
  deploymentBranch: 'gh-pages',
  trailingSlash: false,

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  i18n: {
    defaultLocale: 'pt',
    locales: ['pt'],
  },

  presets: [
    [
      'classic',
      ({
        docs: {
          sidebarPath: './sidebars.js',
          routeBasePath: '/',
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  themeConfig: ({
    navbar: {
      title: 'Meu Site',
      logo: { alt: 'Logo', src: 'img/logo.svg' },
      items: [
        {
          href: 'https://github.com/iisabelledantas/PonderadaLista',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      copyright: `© ${new Date().getFullYear()} Minha Empresa`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
    },
  }),
};

export default config;