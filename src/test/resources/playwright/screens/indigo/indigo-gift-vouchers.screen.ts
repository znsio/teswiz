import type { ScreenContext } from "../screen-context.ts";
import { stayOnCurrentScreen } from "../screen-route.ts";

const LOCATORS = {
  selectedVoucherValue: "#SelectedVoucherValue",
  selectedVoucherQuantity: "#SelectedVoucherQuantity",
  totalAmount: "#lblTotal",
  personaliseCheckbox: "#chkPersonal",
  recipientName: "#Per_Fname",
  customMessage: "#Message",
  previewButton: "input.preview-btn",
  previewVoucherHeading: "div.heading h2:has-text(\"Preview Your Voucher\")",
} as const;

async function selectDropdownValue(screen: ScreenContext, locator: string, value: string): Promise<void> {
  await screen.page.locator(locator).selectOption(value);
}

export async function select(
  screen: ScreenContext,
  numberOfGiftVouchersToPurchase: string,
  denomination: string,
  forWhom?: string,
  customMessage?: string,
): Promise<object> {
  await selectDropdownValue(screen, LOCATORS.selectedVoucherValue, denomination);
  await selectDropdownValue(screen, LOCATORS.selectedVoucherQuantity, numberOfGiftVouchersToPurchase);

  if (forWhom !== undefined && customMessage !== undefined) {
    await screen.page.locator(LOCATORS.personaliseCheckbox).click();
    await screen.page.locator(LOCATORS.recipientName).fill(forWhom);
    await screen.page.locator(LOCATORS.customMessage).fill(customMessage);
  }

  return stayOnCurrentScreen();
}

export async function getTotalPrice(screen: ScreenContext): Promise<number> {
  const totalAmount = (await screen.page.locator(LOCATORS.totalAmount).textContent())?.trim() ?? "";
  return Number.parseInt(totalAmount.split(" ")[1], 10);
}

export async function preview(screen: ScreenContext): Promise<object> {
  await screen.page.locator(LOCATORS.previewButton).click();
  await screen.page.locator(LOCATORS.previewVoucherHeading).waitFor();
  return stayOnCurrentScreen();
}
