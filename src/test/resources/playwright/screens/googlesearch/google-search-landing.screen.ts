import type { ScreenContext } from "../screen-context.ts";

const PAGE_URL = "https://google.com";

const LOCATORS = {
  searchInput: 'textarea[name="q"], input[name="q"]',
} as const;

export async function searchFor(screen: ScreenContext, searchText: string): Promise<void> {
  await screen.page.goto(PAGE_URL, { waitUntil: "load" });
  await screen.page.locator(LOCATORS.searchInput).first().fill(searchText);
  await screen.page.locator(LOCATORS.searchInput).first().press("Enter");
}
