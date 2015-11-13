package com.lurencun.service.autoupdate.internal;

import com.lurencun.service.autoupdate.Version;


public interface ResponseCallback {
	void onFoundLatestVersion(Version version);
	void onCurrentIsLatest(boolean isShowHint);
}
