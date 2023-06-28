package git.artdeell.skymodloader.elfmod;

import java.util.Objects;

public class ElfModMetadata {
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
}
