package com.znsio.teswiz.screen.indigo;


public abstract class IndigoGiftVouchersScreen {

    public static IndigoGiftVouchersScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(IndigoGiftVouchersScreen.class);
    }

    public abstract IndigoGiftVouchersScreen select(String numberOfGiftVouchersToPurchase,
                                                    String denomination);

    public abstract int getTotalPrice();

    public abstract IndigoGiftVouchersScreen select(String numberOfGiftVouchersToPurchase,
                                                    String denomination, String forWhom,
                                                    String customMessage);

    public abstract IndigoGiftVouchersScreen preview();
}
