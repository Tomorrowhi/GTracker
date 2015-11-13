package com.lurencun.service.autoupdate.internal;

public interface VersionDialogListener {
	void doUpdate(boolean laterOnWifi);
	void doIgnore();
}
