// IUpdaterConnection.aidl
package git.artdeell.skymodloader.updater;

oneway interface IUpdaterConnection {
    void onStateChanged();
    void onProgressBarChanged();
}