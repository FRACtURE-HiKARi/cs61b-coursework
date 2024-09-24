package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Blob implements Serializable {
    int version;
    String fileName;
    String sha1;
    private int hashCode = 0;
    public transient byte[] data;
    File base;
    File CWD;

    public Blob(int version, File file, byte[] data, RepositoryBase repo) {
        this.version = version;
        this.fileName = file.getName();
        this.data = data;
        this.base = repo.BLOB_DIR;
        this.CWD = repo.CWD;
        if (data != null)
            sha1 = Utils.sha1(file.getName(), (Object) data);
        else
            sha1 = Utils.sha1(file.getName());
    }

    public Blob(Blob b){
        this.version = b.version;
        this.fileName = b.fileName;
        this.sha1 = b.sha1;
        this.hashCode = b.hashCode;
        this.data = b.data;
        this.base = b.base;
        this.CWD = b.CWD;
    }

    public int getVersion() { return version;}
    public File getFile() { return Utils.join(CWD, fileName);}
    public String getHash() { return sha1;}

    @Override
    public int hashCode(){
        if (hashCode == 0) {
            hashCode = version;
            hashCode = 31 * hashCode + fileName.hashCode();
            hashCode = 31 * hashCode + sha1.hashCode();
        }
        return hashCode;
    }

    public String toString(){
        return fileName + " (" + version + ") " + getHash().substring(0, 4);
    }

    public void save() {
        Utils.writeContents(getBlobFile(), (Object) data);
    }

    public File getFolder(){
        return Utils.join(base, sha1.substring(0, 2));
    }

    public File getBlobFile(){
        return Utils.join(getFolder(), sha1);
    }

    public byte[] getContents(){
        return (data == null) ? Utils.readContents(getBlobFile()) : data;
    }

    public void recoverFile(){
        data = getContents();
        File file = getFile();
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
