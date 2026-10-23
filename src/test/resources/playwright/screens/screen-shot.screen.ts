import type { ScreenContext } from "./screen-context.ts";
import { stayOnCurrentScreen } from "./screen-route.ts";

const PAGE_URL = "https://github.com/znsio/teswiz";

export async function takeScreenshot(screen: ScreenContext): Promise<object> {
  await screen.page.goto(PAGE_URL, { waitUntil: "load" });
  return stayOnCurrentScreen();
}
