package gitlet;

import java.nio.charset.StandardCharsets;
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

    public void add(Blob blob) {
        blobs.add(blob);
        File storeFolder = blob.getFolder();
        storeFolder.mkdirs();
        blob.save();
    }

    public void remove(Blob blob) {
        blobs.remove(blob);
        File storeFolder = blob.getFolder();
        if (!storeFolder.exists()) throw new GitletException("Could not find store folder");
        File file = blob.getFile();
        if (!file.delete()) throw new GitletException("Could not delete store file");
    }

    public Set<Blob> getBlobs() {
        return blobs;
    }

    public String getContentsAsString(Blob blob) {
        return new String(blob.getContents(), StandardCharsets.UTF_8);
    }

    public Blob createNewBlob(File file, Commit head){
        if (!Repository.differs(head, file)) return null;
        if (!file.exists()) throw new GitletException("File " + file + " does not exist");
        int maxVersion = 0;
        for (Blob blob : blobs) {
            if (blob.getVersion() > maxVersion) maxVersion = blob.getVersion();
        }
        maxVersion++;
        return new Blob(maxVersion, file, Utils.readContents(file));
    }
}
