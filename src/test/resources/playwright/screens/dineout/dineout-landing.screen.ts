import type { ScreenContext } from "../screen-context.ts";
import { stayOnCurrentScreen, unsupportedScreenAction } from "../screen-route.ts";

const LOCATORS = {
  defaultCity: '//a[@aria-label="Mumbai"]',
  restaurantSearch: "#restaurantSearch",
  locationFilter: '//li[text()="Location"]',
  submitCity: '//section//button[@value="Submit"]',
} as const;

export async function selectDefaultCity(screen: ScreenContext): Promise<object> {
  await screen.page.locator(`xpath=${LOCATORS.defaultCity}`).click();
  return stayOnCurrentScreen();
}

export async function selectCity(screen: ScreenContext, city: string): Promise<object> {
  const restaurantSearch = screen.page.locator(LOCATORS.restaurantSearch);
  await restaurantSearch.clear();
  await restaurantSearch.fill(city);
  await screen.page.locator(`xpath=${LOCATORS.locationFilter}`).click();
  await screen.page.locator(`xpath=${LOCATORS.submitCity}`).click();
  return stayOnCurrentScreen();
}

export function searchCuisine(): object {
  return unsupportedScreenAction(
    "Dineout cuisine search is not supported on web for WEB_ENGINE=playwright-ts.",
  );
}
