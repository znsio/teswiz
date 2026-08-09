package com.znsio.teswiz.visual;

import java.util.List;

import com.applitools.eyes.visualgrid.model.EmulationBaseInfo;
import com.applitools.eyes.visualgrid.model.RenderBrowserInfo;

public final class PlaywrightUfgTargetMapper {
    public List<PlaywrightVisualSessionRequest.UfgTarget> map(List<RenderBrowserInfo> renderBrowserInfos) {
        return renderBrowserInfos.stream()
                .map(this::toTarget)
                .toList();
    }

    private PlaywrightVisualSessionRequest.UfgTarget toTarget(RenderBrowserInfo renderBrowserInfo) {
        EmulationBaseInfo emulationInfo = renderBrowserInfo.getEmulationInfo();
        if (null != emulationInfo) {
            return new PlaywrightVisualSessionRequest.UfgTarget(
                    null,
                    null,
                    null,
                    emulationInfo.getDeviceName(),
                    null == emulationInfo.getScreenOrientation() ? null : emulationInfo.getScreenOrientation().name());
        }
        return new PlaywrightVisualSessionRequest.UfgTarget(
                renderBrowserInfo.getWidth(),
                renderBrowserInfo.getHeight(),
                null == renderBrowserInfo.getBrowserType() ? null : renderBrowserInfo.getBrowserType().name(),
                null,
                null);
    }
}
