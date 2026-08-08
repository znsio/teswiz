import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  searchInput: 'input[name="searchVal"]',
};

let imageSearchTerm = "";

export async function attachFileToDevice(screen: ScreenContext, imageData: any): Promise<void> {
  const sourceFileLocation = imageData.IMAGE_FILE_LOCATION;
  const filename = sourceFileLocation.substring(sourceFileLocation.lastIndexOf('/') + 1);
  imageSearchTerm = filename.substring(0, filename.lastIndexOf('.'));
}

export async function searchByImage(screen: ScreenContext): Promise<void> {
  await screen.page.goto("https://www.ajio.com/");
  await screen.page.locator(LOCATORS.searchInput).fill(imageSearchTerm);
  await screen.page.locator(LOCATORS.searchInput).press("Enter");
}

export async function goToMenu(screen: ScreenContext): Promise<void> {
}

export async function selectProductFromCategory(screen: ScreenContext, product: string, category: string, gender: string): Promise<void> {
  await screen.page.goto("https://www.ajio.com/");
  await screen.page.locator(LOCATORS.searchInput).fill(`${gender} ${product}`);
  await screen.page.locator(LOCATORS.searchInput).press("Enter");
}

export async function searchForTheProduct(screen: ScreenContext, productName: string): Promise<void> {
  await screen.page.goto("https://www.ajio.com/");
  await screen.page.locator(LOCATORS.searchInput).fill(productName);
  await screen.page.locator(LOCATORS.searchInput).press("Enter");
}

export async function clickOnAllowToSendNotifications(screen: ScreenContext): Promise<void> {
}

export async function clickOnAllowLocation(screen: ScreenContext): Promise<void> {
}

export async function clickOnAllowLocationWhileUsingApp(screen: ScreenContext): Promise<void> {
}

export async function relaunchApplication(screen: ScreenContext): Promise<void> {
}
