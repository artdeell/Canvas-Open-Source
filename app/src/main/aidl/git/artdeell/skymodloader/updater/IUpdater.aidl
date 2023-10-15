// IUpdater.aidl
package git.artdeell.skymodloader.updater;

import git.artdeell.skymodloader.updater.IUpdaterConnection;
parcelable ParcelableException;

interface IUpdater {
    void setUpdateListener(IUpdaterConnection listener);
    byte getServiceState();
    String getUpdateChangelog();
    void downloadUpdate();
    long getDownloadMaximum();
    long getDownloadCurrent();
    ParcelableException getException();
    void suicide();
}