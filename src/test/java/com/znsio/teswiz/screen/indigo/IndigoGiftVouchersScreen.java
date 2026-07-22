package com.znsio.teswiz.screen.indigo;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.indigo.IndigoGiftVouchersScreenAndroid;
import com.znsio.teswiz.screen.web.indigo.IndigoGiftVouchersScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class IndigoGiftVouchersScreen {
    private static final String SCREEN_NAME = IndigoGiftVouchersScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

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
