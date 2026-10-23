package com.znsio.teswiz.runner;

import com.znsio.teswiz.mobile.session.MobileDriverSession;

@Deprecated(forRemoval = false)
public class DriverSession extends MobileDriverSession {
    private final MobileDriverSession delegate;

    public DriverSession() {
        this.delegate = this;
    }

    private DriverSession(MobileDriverSession delegate) {
        this.delegate = delegate;
    }

    static DriverSession from(MobileDriverSession session) {
        if (null == session) {
            return null;
        }
        return session instanceof DriverSession driverSession ? driverSession : new DriverSession(session);
    }

    @Override
    public String getPlatformName() {
        return delegate == this ? super.getPlatformName() : delegate.getPlatformName();
    }

    @Override
    public String getUdid() {
        return delegate == this ? super.getUdid() : delegate.getUdid();
    }

    @Override
    public String getDeviceName() {
        return delegate == this ? super.getDeviceName() : delegate.getDeviceName();
    }
}
