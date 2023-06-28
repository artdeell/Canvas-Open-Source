package git.artdeell.skymodloader.elfmod;

import java.util.Objects;

public class ElfFileReference implements Comparable<ElfFileReference>{
    ElfModMetadata modMeta;
    int referenceCount = 1;

    public ElfFileReference(ElfModMetadata modMeta) {
        this.modMeta = modMeta;
    }

    @Override
    public int compareTo(ElfFileReference o) {
        return o.referenceCount - referenceCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElfFileReference reference = (ElfFileReference) o;
        return Objects.equals(modMeta.name, reference.modMeta.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modMeta.name);
    }
}
