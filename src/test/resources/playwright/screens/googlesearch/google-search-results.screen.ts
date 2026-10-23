import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  searchResultHeadings: "a div[role='heading']",
} as const;

export async function getSearchResults(screen: ScreenContext): Promise<string[]> {
  await screen.page.locator(LOCATORS.searchResultHeadings).first().waitFor({ state: "visible" });
  return screen.page.locator(LOCATORS.searchResultHeadings).allTextContents();
}
