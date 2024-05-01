package git.artdeell.skymodloader.elfmod;

public class InvalidModException extends Exception {
    public InvalidModException(String s) {
        super(s);
    }
    public InvalidModException() {}
}

class OutdatedModApiException extends Exception {
    public OutdatedModApiException(String s) {
        super(s);
    }
    public OutdatedModApiException(boolean _isHigherApi) {
        isHigherApi = _isHigherApi;
    }
    boolean isHigherApi;
}

class ModExistsException extends  Exception {

}
