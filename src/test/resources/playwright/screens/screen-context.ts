import type { BrowserContext, Frame, Page } from "playwright";

export type ScreenContext = {
  session: unknown;
  context: BrowserContext;
  page: Page;
  root: Page | Frame;
};
