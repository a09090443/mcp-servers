// @ts-check
import {themes as prismThemes} from 'prism-react-renderer';

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'MCP Servers',
  tagline: '八個以 Kotlin + Quarkus 打造的 Model Context Protocol 服務',
  favicon: 'img/favicon.ico',

  future: {
    v4: true,
  },

  url: 'https://zipe.github.io',
  baseUrl: '/mcp-servers/',

  organizationName: 'zipe',
  projectName: 'mcp-servers',

  onBrokenLinks: 'throw',

  i18n: {
    defaultLocale: 'zh-Hant',
    locales: ['zh-Hant'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
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

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      colorMode: {
        respectPrefersColorScheme: true,
      },
      navbar: {
        title: 'MCP Servers',
        logo: {
          alt: 'MCP Servers Logo',
          src: 'img/logo.svg',
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'mainSidebar',
            position: 'left',
            label: '總覽',
          },
          {
            type: 'dropdown',
            label: '模組',
            position: 'left',
            items: [
              {type: 'docSidebar', sidebarId: 'dateSidebar', label: 'date'},
              {type: 'docSidebar', sidebarId: 'excelSidebar', label: 'excel'},
              {type: 'docSidebar', sidebarId: 'filesystemSidebar', label: 'filesystem'},
              {type: 'docSidebar', sidebarId: 'cwaTwSidebar', label: 'cwa-tw'},
              {type: 'docSidebar', sidebarId: 'twStockSidebar', label: 'tw-stock'},
              {type: 'docSidebar', sidebarId: 'gmailSidebar', label: 'gmail'},
              {type: 'docSidebar', sidebarId: 'googleDriveSidebar', label: 'google-drive'},
              {type: 'docSidebar', sidebarId: 'googleMapSidebar', label: 'google-map'},
            ],
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: '入門',
            items: [
              {label: '總覽', to: '/'},
              {label: '快速開始', to: '/getting-started'},
              {label: '架構', to: '/architecture'},
            ],
          },
          {
            title: '模組',
            items: [
              {label: 'tw-stock', to: '/modules/tw-stock'},
              {label: 'cwa-tw', to: '/modules/cwa-tw'},
              {label: 'google-drive', to: '/modules/google-drive'},
            ],
          },
          {
            title: '參考',
            items: [
              {label: 'MCP 協議', href: 'https://modelcontextprotocol.io/'},
              {label: 'Quarkus', href: 'https://quarkus.io/'},
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} MCP Servers. Built with Docusaurus.`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
        additionalLanguages: ['kotlin', 'java', 'properties', 'bash', 'json'],
      },
    }),
};

export default config;
