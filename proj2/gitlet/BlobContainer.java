package gitlet;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.File;

public class BlobContainer {
    private Set<Blob> blobs;
    public File BASE;
    RepositoryBase repo;

    public BlobContainer(RepositoryBase repo) {
        blobs = new HashSet<>();
        this.repo = repo;
        this.BASE = repo.BLOB_DIR;
    }

    public BlobContainer(Set<Blob> blobs, RepositoryBase repo) {
        this.blobs = blobs;
        this.repo = repo;
        this.BASE = repo.BLOB_DIR;
    }

    public void add(Blob blob) {
        blobs.add(blob);
        File storeFolder = blob.getFolder();
        storeFolder.mkdirs();
        blob.save();
    }

    public void add(Blob blob, byte[] overwriteData) {
        blobs.add(blob);
        File storeFolder = blob.getFolder();
        storeFolder.mkdirs();
        blob.data = overwriteData;
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

    public Blob createNewBlob(File file, Commit head) {
        if (!Repository.differs(head, file)) return null;
        if (!file.exists()) throw new GitletException("File " + file + " does not exist");
        int maxVersion = 0;
        for (Blob blob : blobs) {
            if (blob.getVersion() > maxVersion) maxVersion = blob.getVersion();
        }
        maxVersion++;
        return new Blob(maxVersion, file, Utils.readContents(file), repo);
    }
}
