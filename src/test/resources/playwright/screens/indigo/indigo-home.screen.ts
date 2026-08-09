import type { ScreenContext } from "../screen-context.ts";
import { stayOnCurrentScreen } from "../screen-route.ts";

const LOCATORS = {
  closeCookieBanner: "a.close-cookie",
  fromInput: 'input[placeholder="From"]',
  toInput: 'input[placeholder="To"]',
  popularDestinationsLabel: '..//div[text()="Popular Destinations"]',
  passengerInput: 'input[name="passenger"]',
  journeyTypeDropdown: "//div[@class='filter-option-inner-inner']",
  journeyTypeOneWay: "//a[contains(@class,'one-way-tab')]",
  extraSeatTooltipClose: "//i[@class='icon-close close-extraseat-tooltip']",
  passengerSelectionDone: "//button[text()='Done']",
  decreaseAdultPassengerCount: "//button[@title='Decrease Adult Passenger Count']",
  increaseAdultPassengerCount: "//button[@title='Increase Adult Passenger Count']",
  adultPassengerCount: "//input[@class='counter adult-pax']",
  searchFlightButton: "//span[text()='Search Flight']",
  bookMenu: "//a[@title='Book']",
  giftVoucherMenu: "//div[@class='menu-wrapper-child']//div[text()='Gift Voucher']",
} as const;

async function dismissCookieBanner(screen: ScreenContext): Promise<void> {
  const closeCookieBanner = screen.page.locator(LOCATORS.closeCookieBanner);
  if (await closeCookieBanner.count()) {
    await closeCookieBanner.first().click();
  }
}

async function dismissExtraSeatTooltip(screen: ScreenContext): Promise<void> {
  const closeTooltip = screen.page.locator(`xpath=${LOCATORS.extraSeatTooltipClose}`);
  if (await closeTooltip.count()) {
    await closeTooltip.first().click();
  }
}

function fromOptionLocator(from: string): string {
  return `..//div[@data-name='${from}']`;
}

function toOptionLocator(destination: string): string {
  return `..//div[@data-name='${destination}']`;
}

async function getSelectedAdultPassengerCount(screen: ScreenContext): Promise<number> {
  const selectedCount = await screen.page.locator(`xpath=${LOCATORS.adultPassengerCount}`).inputValue();
  return Number.parseInt(selectedCount, 10);
}

export async function selectFrom(screen: ScreenContext, from: string): Promise<object> {
  await dismissCookieBanner(screen);
  const fromInput = screen.page.locator(LOCATORS.fromInput);
  await fromInput.click();
  await fromInput.fill(from);
  await fromInput.locator(`xpath=${fromOptionLocator(from)}`).click();
  return stayOnCurrentScreen();
}

export async function selectTo(screen: ScreenContext, destination: string): Promise<object> {
  await dismissCookieBanner(screen);
  const toInput = screen.page.locator(LOCATORS.toInput);
  await toInput.click();
  await toInput.fill(destination);
  await toInput.locator(`xpath=${LOCATORS.popularDestinationsLabel}`).waitFor();
  await toInput.locator(`xpath=${toOptionLocator(destination)}`).click();
  return stayOnCurrentScreen();
}

export async function selectNumberOfAdultPassengers(
  screen: ScreenContext,
  numberOfAdultsToSelect: number,
): Promise<object> {
  await dismissCookieBanner(screen);
  await screen.page.locator(LOCATORS.passengerInput).click();
  await dismissExtraSeatTooltip(screen);

  let numberOfAdultsSelected = await getSelectedAdultPassengerCount(screen);
  while (numberOfAdultsToSelect < numberOfAdultsSelected && numberOfAdultsSelected !== 1) {
    await screen.page.locator(`xpath=${LOCATORS.decreaseAdultPassengerCount}`).click();
    numberOfAdultsSelected -= 1;
  }
  while (numberOfAdultsToSelect > numberOfAdultsSelected) {
    await screen.page.locator(`xpath=${LOCATORS.increaseAdultPassengerCount}`).click();
    numberOfAdultsSelected += 1;
  }

  await screen.page.locator(`xpath=${LOCATORS.passengerSelectionDone}`).click();
  return stayOnCurrentScreen();
}

export async function selectJourneyType(screen: ScreenContext, _journeyType: string): Promise<object> {
  await dismissCookieBanner(screen);
  await screen.page.locator(`xpath=${LOCATORS.journeyTypeDropdown}`).click();
  await screen.page.locator(`xpath=${LOCATORS.journeyTypeOneWay}`).click();
  return stayOnCurrentScreen();
}

export async function searchFlightOptions(screen: ScreenContext): Promise<void> {
  await dismissCookieBanner(screen);
  await screen.page.locator(`xpath=${LOCATORS.searchFlightButton}`).click();
}

export async function selectGiftVouchers(screen: ScreenContext): Promise<void> {
  await dismissCookieBanner(screen);
  await screen.page.locator(`xpath=${LOCATORS.bookMenu}`).hover();
  await screen.page.locator(`xpath=${LOCATORS.giftVoucherMenu}`).click();
}
