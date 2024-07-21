package git.artdeell.skymodloader.ui;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.ViewModel;

public class BaseViewModel extends ViewModel implements Observable {
    private transient PropertyChangeRegistry mCallbacks;

    public BaseViewModel() {
    }

    @Override
    public void addOnPropertyChangedCallback(@NonNull Observable.OnPropertyChangedCallback callback) {
        synchronized(this) {
            if (this.mCallbacks == null) {
                this.mCallbacks = new PropertyChangeRegistry();
            }
        }

        this.mCallbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(@NonNull Observable.OnPropertyChangedCallback callback) {
        synchronized(this) {
            if (this.mCallbacks == null) {
                return;
            }
        }

        this.mCallbacks.remove(callback);
    }

    public void notifyChange() {
        synchronized(this) {
            if (this.mCallbacks == null) {
                return;
            }
        }

        this.mCallbacks.notifyCallbacks(this, 0, (Void) null);
    }

    public void notifyPropertyChanged(int fieldId) {
        synchronized(this) {
            if (this.mCallbacks == null) {
                return;
            }
        }

        this.mCallbacks.notifyCallbacks(this, fieldId, (Void) null);
    }

}
