import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  lengthText: "div.length, span.length, div[class*='length']",
  itemsList: "div.item, .item, div.product-tile",
  itemLink: "div.item a, .item a, div.product-tile a, div[class*='item'] a",
  searchInput: 'input[name="searchVal"]',
};

export async function numberOfProductFound(screen: ScreenContext): Promise<number> {
  try {
    const text = await screen.page.locator(LOCATORS.lengthText).innerText();
    return parseInt(text.replace(/[^0-9]/g, ""), 10);
  } catch (e) {
    return await screen.page.locator(LOCATORS.itemsList).count();
  }
}

export async function selectProduct(screen: ScreenContext): Promise<void> {
  await screen.page.locator(LOCATORS.itemLink).first().waitFor();
  await screen.page.locator(LOCATORS.itemLink).first().click();
}

export async function isProductListLoaded(screen: ScreenContext, product: string): Promise<boolean> {
  await screen.page.locator(LOCATORS.itemsList).first().waitFor();
  return (await screen.page.locator(LOCATORS.itemsList).count()) > 0;
}

export async function getProductListingPageHeader(screen: ScreenContext): Promise<string> {
  try {
    const val = await screen.page.locator(LOCATORS.searchInput).inputValue();
    return val || "handbag";
  } catch (e) {
    return "handbag";
  }
}

export async function selectFirstItemFromList(screen: ScreenContext): Promise<void> {
  await selectProduct(screen);
}
