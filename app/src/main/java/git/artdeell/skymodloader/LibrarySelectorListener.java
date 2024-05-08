package git.artdeell.skymodloader;

public class LibrarySelectorListener{
    public static native void onModLibrary(
            String _path,
            boolean _isDraw,
            String _displayName,
            String _author,
            String _description,
            String _version,
            boolean _selfManagedUI
    );
}
