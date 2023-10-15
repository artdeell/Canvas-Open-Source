package git.artdeell.skymodloader.elfmod;

public interface LoadingListener {
    DummyListener DUMMY = new DummyListener();
    void refreshModList(boolean mode, int which);
    void onLoadingUpdated();
    void signalModRemovalUnsafe();
    void signalModRemovalError();
    void signalModAddException();
}
class DummyListener implements LoadingListener{
    @Override
    public void refreshModList(boolean mode, int which) {

    }

    @Override
    public void onLoadingUpdated() {

    }


    @Override
    public void signalModRemovalUnsafe() {

    }


    @Override
    public void signalModRemovalError() {

    }

    @Override
    public void signalModAddException() {

    }

}
