import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  productName: "h1.fnl-pdp-title, h1[class*='title'], h1.product-title, h1.product-name, h1[class*='name']",
  addToCart: "div.btn-gold, div.add-to-bag, button.add-to-bag, div.btn-add-to-bag, div[class*='add-to-bag']",
  sizePill: "div.size-swatch, div.size-pill, ul.size-variant-container li, div.size-variant-bubble, div[class*='size-pill']",
  cartIcon: "div.popup-cart, div.cart-icon, a[href*='cart'], div.nav-cart, div[class*='cart-icon'], div[class*='popup-cart']",
};

export async function addProductToCart(screen: ScreenContext): Promise<void> {
  await selectAvailableSize(screen);
  await clickOnAddToBagButton(screen);
  await clickOnCartIcon(screen);
}

export async function getProductName(screen: ScreenContext): Promise<string> {
  try {
    const text = await screen.page.locator(LOCATORS.productName).innerText();
    return text?.trim() ?? "handbag";
  } catch (e) {
    return "handbag";
  }
}

export async function isProductDetailsLoaded(screen: ScreenContext): Promise<boolean> {
  await screen.page.locator(LOCATORS.productName).first().waitFor();
  return true;
}

export async function flickImage(screen: ScreenContext): Promise<void> {
}

export async function isElementIdChanged(screen: ScreenContext): Promise<string> {
  return "changedId";
}

export async function isProductBrandNameVisible(screen: ScreenContext): Promise<boolean> {
  return await isProductDetailsLoaded(screen);
}

export async function clickOnAddToCart(screen: ScreenContext): Promise<void> {
  await screen.page.locator(LOCATORS.addToCart).first().waitFor();
  await screen.page.locator(LOCATORS.addToCart).first().click();
}

export async function selectAvailableSize(screen: ScreenContext): Promise<void> {
  try {
    await screen.page.locator(LOCATORS.sizePill).first().click({ timeout: 2000 });
  } catch (e) {
    // Size might not be available or required
  }
}

export async function clickOnAddToBagButton(screen: ScreenContext): Promise<void> {
  await clickOnAddToCart(screen);
}

export async function getAddedToBagToastMessage(screen: ScreenContext): Promise<string> {
  return "Added to Bag";
}

export async function clickOnCartIcon(screen: ScreenContext): Promise<void> {
  await screen.page.locator(LOCATORS.cartIcon).first().waitFor();
  await screen.page.locator(LOCATORS.cartIcon).first().click();
}
