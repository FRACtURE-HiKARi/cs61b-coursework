package gitlet;

import java.util.*;
import java.io.File;

public class BlobContainer {
    private Set<Blob> blobs;
    public static final File BASE = Repository.BLOB_DIR;

    public BlobContainer() {
        blobs = new HashSet<>();
    }

    public BlobContainer(Set<Blob> blobs) {
        this.blobs = blobs;
    }

    public void add(Blob blob, byte[] data) {
        blobs.add(blob);
        String sha1 = blob.getHash();
        File storeFolder = Utils.join(BASE, sha1.substring(0, 2));
        storeFolder.mkdirs();
        File file = Utils.join(storeFolder, sha1);
        if (data != null)
            Utils.writeContents(file, (Object) data);
        else Utils.writeContents(file, "removed");
    }

    public void remove(Blob blob) {
        blobs.remove(blob);
        String sha1 = blob.getHash();
        File storeFolder = Utils.join(BASE, sha1.substring(0, 2));
        if (!storeFolder.exists()) throw new GitletException("Could not find store folder");
        File file = Utils.join(storeFolder, sha1);
        if (!file.delete()) throw new GitletException("Could not delete store file");
    }

    public Set<Blob> getBlobs() {
        return blobs;
    }

    public byte[] getContents(Blob blob) {
        return Utils.readContents(new File(getFolder(blob), blob.getHash()));
    }

    public String getContentsAsString(Blob blob) {
        return Utils.readContentsAsString(new File(getFolder(blob), blob.getHash()));

    }
    private File getFolder(Blob blob){
        File storeFolder = Utils.join(BASE, blob.getHash().substring(0, 2));
        if (!storeFolder.exists()) throw new GitletException("Could not find store folder");
        return storeFolder;
    }

}
