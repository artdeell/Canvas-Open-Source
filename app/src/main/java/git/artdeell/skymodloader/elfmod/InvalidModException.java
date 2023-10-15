package git.artdeell.skymodloader.elfmod;

public class InvalidModException extends Exception{
    public InvalidModException(String s) {
        super(s);
    }
    public InvalidModException() {}
}
class ModExistsException extends  Exception {

}
