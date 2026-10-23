import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  fileUploadLink: 'a[href="/upload"]',
  fileInput: 'input[name="file"]',
  uploadButton: "#file-submit",
  uploadedMessage: "h3",
} as const;

type UploadFile = {
  IMAGE_FILE_LOCATION: string;
};

export async function navigateToFileUplaodPage(screen: ScreenContext): Promise<void> {
  await screen.page.locator(LOCATORS.fileUploadLink).click();
}

export async function uploadFile(screen: ScreenContext, file: UploadFile): Promise<void> {
  const filePath = `${process.cwd()}${file.IMAGE_FILE_LOCATION}`;
  await screen.page.locator(LOCATORS.fileInput).setInputFiles(filePath);
  await screen.page.locator(LOCATORS.uploadButton).click();
}

export async function getFileUploadText(screen: ScreenContext): Promise<string> {
  const uploadedMessage = await screen.page.locator(LOCATORS.uploadedMessage).textContent();
  return uploadedMessage?.trim() ?? "";
}
