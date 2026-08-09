import type { ScreenContext } from "../screen-context.ts";
import { stayOnCurrentScreen } from "../screen-route.ts";

const LOCATORS = {
  meetingInfoIcon: '//div[@class="icon pointer"]',
  micLabel: '//div[contains(@class, "mic-section")]//img',
  meetingId: '//div[text()="Meeting ID"]/following-sibling::div',
  password: '//div[text()="Password"]/following-sibling::div',
  microphoneButton: '//div[@id = "toggleMicButton"]//div[contains(@class, "img-holder")]',
} as const;

async function revealMeetingControls(screen: ScreenContext): Promise<void> {
  await screen.page.locator(`xpath=${LOCATORS.meetingInfoIcon}`).hover();
}

async function readMeetingDetail(screen: ScreenContext, locator: string): Promise<string> {
  const infoIcon = screen.page.locator(`xpath=${LOCATORS.meetingInfoIcon}`);
  await infoIcon.click();
  const detail = (await screen.page.locator(`xpath=${locator}`).textContent())?.replace(/\s/g, "") ?? "";
  await infoIcon.click();
  return detail;
}

export async function isMeetingStarted(screen: ScreenContext): Promise<boolean> {
  await revealMeetingControls(screen);
  await screen.page.locator(`xpath=${LOCATORS.micLabel}`).waitFor();
  return true;
}

export async function getMeetingId(screen: ScreenContext): Promise<string> {
  return readMeetingDetail(screen, LOCATORS.meetingId);
}

export async function getMeetingPassword(screen: ScreenContext): Promise<string> {
  return readMeetingDetail(screen, LOCATORS.password);
}

export async function unmute(screen: ScreenContext): Promise<object> {
  await revealMeetingControls(screen);
  await screen.page.locator(`xpath=${LOCATORS.microphoneButton}`).click();
  return stayOnCurrentScreen();
}

export async function mute(screen: ScreenContext): Promise<object> {
  await revealMeetingControls(screen);
  await screen.page.locator(`xpath=${LOCATORS.microphoneButton}`).click();
  return stayOnCurrentScreen();
}

export async function getMicLabelText(screen: ScreenContext): Promise<string> {
  await revealMeetingControls(screen);
  return (await screen.page.locator(`xpath=${LOCATORS.micLabel}`).textContent())?.trim() ?? "";
}

export async function openJioMeetNotification(): Promise<never> {
  throw new Error("Jio Meet Device Notification of Meeting is not available for Web");
}
