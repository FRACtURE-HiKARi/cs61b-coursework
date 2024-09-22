package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Blob implements Serializable {
    int version;
    File file;
    String sha1;
    private int hashCode = 0;
    public transient byte[] data;

    public Blob(int version, File file, byte[] data) {
        this.version = version;
        this.file = file;
        this.data = data;
        if (data != null)
            sha1 = Utils.sha1(file.getAbsolutePath(), (Object) data);
        else
            sha1 = Utils.sha1(file.getAbsolutePath());
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

    public String toString(){
        return file.getName() + " (" + version + ") " + getHash().substring(0, 4);
    }

    public void save() {
        Utils.writeContents(getBlobFile(), (Object) data);
    }

    public File getFolder(){
        return Utils.join(BlobContainer.BASE, sha1.substring(0, 2));
    }

    public File getBlobFile(){
        return Utils.join(getFolder(), sha1);
    }

    public byte[] getContents(){
        return (data == null) ? Utils.readContents(getBlobFile()) : data;
    }

    public void recoverFile(){
        data = getContents();
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Utils.writeContents(file, (Object)data);
    }
}
