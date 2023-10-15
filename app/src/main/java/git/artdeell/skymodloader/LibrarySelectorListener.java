package git.artdeell.skymodloader;

public class LibrarySelectorListener{
    public static native void onModLibrary(String path, boolean willDraw, String displayName, boolean dev, boolean selfManagedUI);
}
