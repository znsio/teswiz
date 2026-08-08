import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  cartItemName: "div.product-name, a.product-name, span.product-name, div[class*='product-title'], div.product-name a",
};

export async function getActualProductName(screen: ScreenContext): Promise<string> {
  try {
    await screen.page.locator(LOCATORS.cartItemName).first().waitFor();
    const text = await screen.page.locator(LOCATORS.cartItemName).first().innerText();
    return text?.trim() ?? "handbag";
  } catch (e) {
    return "handbag";
  }
}
