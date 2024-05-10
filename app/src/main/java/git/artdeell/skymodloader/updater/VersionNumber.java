package git.artdeell.skymodloader.updater;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionNumber implements Serializable {
    public int major, minor, patch;
    public VersionNumber(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static VersionNumber parseVersion(String tag) {
        Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        Matcher matcher = pattern.matcher(tag);
        if(!matcher.find()) return null;
        String majorString = matcher.group(1), minorString = matcher.group(2), patchString = matcher.group(3);
        if(majorString == null || minorString == null || patchString == null) return null;
        return new VersionNumber(
                Integer.parseInt(majorString),
                Integer.parseInt(minorString),
                Integer.parseInt(patchString)
        );
    }

    /**
     * Compare two version numbers
     * @param version the version number to compare against
     * @return -1 if {@code this} is behind {@code version}
     *         0 if {@code this} is equal to {@code version}
     *         1 if {@code this} is ahead of {@code version}
     */
    public int compare(VersionNumber version) {
        int result = Integer.compare(this.major, version.major);
        if(result == 0) result = Integer.compare(this.minor, version.minor);
        if(result == 0) result = Integer.compare(this.patch, version.patch);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionNumber that = (VersionNumber) o;
        return major == that.major && minor == that.minor && patch == that.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }
}
