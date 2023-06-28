package git.artdeell.skymodloader.updater;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class ParcelableException implements Parcelable {
    public Exception exception;

    public ParcelableException(Exception exception) {
        this.exception = exception;
    }
    private ParcelableException(Parcel in) {
        exception = (Exception) in.readSerializable();
    }

    public static final Creator<ParcelableException> CREATOR = new Creator<ParcelableException>() {
        @Override
        public ParcelableException createFromParcel(Parcel in) {
            return new ParcelableException(in);
        }

        @Override
        public ParcelableException[] newArray(int size) {
            return new ParcelableException[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeSerializable(exception);
    }
}
