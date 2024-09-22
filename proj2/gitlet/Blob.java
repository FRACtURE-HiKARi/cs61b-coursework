package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    private int version;
    private File file;
    private String sha1;
    private int hashCode = 0;
    private boolean isDeleteBlob = false;

    public Blob(int version, File file, byte[] data) {
        this.version = version;
        this.file = file;
        if (data != null)
            sha1 = Utils.sha1(file.getAbsolutePath(), (Object) data);
        else
            sha1 = Utils.sha1(file.getAbsolutePath());
        if (data == null) isDeleteBlob = true;
    }

    public int getVersion() { return version;}
    public File getFile() { return file;}
    public String getHash() { return sha1;}

    @Override
    public int hashCode(){
        if (hashCode == 0) {
            hashCode = version;
            hashCode = 31 * hashCode + file.hashCode();
            hashCode = 31 * hashCode + sha1.hashCode();
        }
        return hashCode;
    }

    public boolean isDeleteBlob() { return isDeleteBlob; }

    public String toString(){
        return file.getName() + " (" + version + ") " + getHash().substring(0, 4);
    }

}
