package git.artdeell.skymodloader.elfmod;

import java.io.File;
import java.io.FileFilter;

public class SharedObjectFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        return pathname.isFile() && pathname.getName().endsWith(".so");
    }
}
