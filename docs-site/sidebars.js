// @ts-check

/**
 * 每個模組各有一個獨立 sidebar：進入該模組頁面後，左側只會顯示該模組的內容，
 * 不會看到其他七個模組。共用的入門／架構／開發文件集中在 mainSidebar。
 *
 * 新增模組時要同步三處：這裡加一個 sidebar、docusaurus.config.js 的「模組」
 * 下拉選單加一筆 docSidebar item、以及 docs/modules/ 底下的頁面。
 *
 * @type {import('@docusaurus/plugin-content-docs').SidebarsConfig}
 */
const sidebars = {
  mainSidebar: [
    'index',
    'getting-started',
    'architecture',
    'development',
  ],

  dateSidebar: ['modules/date'],
  excelSidebar: [
    'modules/excel/index',
    'modules/excel/tools',
    'modules/excel/conventions',
    'modules/excel/development',
  ],
  filesystemSidebar: ['modules/filesystem'],
  cwaTwSidebar: ['modules/cwa-tw'],
  twStockSidebar: [
    'modules/tw-stock/index',
    'modules/tw-stock/tools',
    'modules/tw-stock/prompts',
    'modules/tw-stock/conventions',
    'modules/tw-stock/development',
  ],
  gmailSidebar: ['modules/gmail'],
  googleDriveSidebar: [
    'modules/google-drive/index',
    'modules/google-drive/tools',
    'modules/google-drive/setup',
    'modules/google-drive/conventions',
    'modules/google-drive/development',
  ],
  googleMapSidebar: ['modules/google-map'],
};

export default sidebars;
