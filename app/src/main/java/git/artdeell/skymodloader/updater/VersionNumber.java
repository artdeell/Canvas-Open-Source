package git.artdeell.skymodloader.updater;

import java.io.Serializable;
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

    public static boolean versionCompare(VersionNumber currentVersion, VersionNumber newVersion) {
        // Compares version numbers.
        if (newVersion.major > currentVersion.major) return true;
        if (newVersion.minor > currentVersion.minor) return true;
        return newVersion.patch > currentVersion.patch;
    }
}
