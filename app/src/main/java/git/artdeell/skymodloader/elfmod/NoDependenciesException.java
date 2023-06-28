package git.artdeell.skymodloader.elfmod;

public class NoDependenciesException extends Exception {
    ElfModUIMetadata failureReason;
    ElfModMetadata[] failedDependencies;
    public NoDependenciesException(ElfModUIMetadata failureReason, ElfModMetadata[] failedDependencies) {
        this.failureReason = failureReason;
        this.failedDependencies = failedDependencies;
    }
}
