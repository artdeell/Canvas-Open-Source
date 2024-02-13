package git.artdeell.skymodloader.elfmod;

import android.util.Log;

import java.util.Objects;
import java.io.File;

public class ElfModMetadata {
    public File modFile;
    boolean modIsValid = false;
    String name;
    String displayName;
    int majorVersion;
    int minorVersion;
    int patchVersion;
    ElfModMetadata[] dependencies;
    boolean displaysUI;
    boolean dev;
    boolean selfManagedUI;
    String githubReleasesUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElfModMetadata metadata = (ElfModMetadata) o;
        return modIsValid == metadata.modIsValid && name.equals(metadata.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modIsValid, name);
    }

    public String getGithubReleasesUrl() {
        return this.githubReleasesUrl;
    }

    public String getName() {
        return this.name;
    }

    public String getLibName() {
        return name;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getPatchVersion() {
        return patchVersion;
    }
}
