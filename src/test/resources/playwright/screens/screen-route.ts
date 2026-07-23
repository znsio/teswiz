export function stayOnCurrentScreen() {
  return { teswizScreenRoute: "current" } as const;
}

export function returnNoScreen() {
  return { teswizScreenRoute: "none" } as const;
}
