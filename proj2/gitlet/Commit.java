package gitlet;

// TODO: any imports you need here
import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
// TODO: You'll likely use this in this class

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */

public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    String message;
    String author;
    //private String email;
    Date commitDate;
    Commit parent;
    // fileName -> Blob
    Map<String, Blob> files;
    int hashCode = 0;
    String SHA1 = null;
    Branch branch;
    File CWD;
    int height;
    public static DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);

    /* TODO: fill in the rest of this class. */
    public Commit(String message, String author, Date commitDate, Commit parent, Branch branch, File CWD) {
        this.message = message;
        this.author = author;
        this.commitDate = commitDate;
        this.parent = parent;
        this.branch = branch;
        this.CWD = CWD;
        files = new HashMap<>();
        if (parent != null) {
            files.putAll(parent.files);
            height = parent.height + 1;
        }else {
            height = 0;
        }
    }

    public Commit(Commit c) {
        this.message = c.message;
        this.author = c.author;
        this.commitDate = c.commitDate;
        this.parent = c.parent;
        this.branch = c.branch;
        this.hashCode = c.hashCode();
        this.SHA1 = c.SHA1;
        this.CWD = c.CWD;
        this.height = c.height;
        this.files = new HashMap<>();
        for (String file : c.files.keySet()) {
            this.files.put(file, new Blob(c.files.get(file)));
        }
    }

    public void addFile(String fileName, Blob blob) {
        files.put(fileName, blob);
    }

    public void addFile(File file, Blob blob) {
        files.put(file.getName(), blob);
    }

    public Blob getBlob(File file) {
        return files.get(file.getName());
    }

    public boolean contains(File file) {
        Blob b = getBlob(file);
        return b != null && !(b instanceof RemoveBlob);
    }

    public String getHash() {
        if (SHA1 == null) {
            Object[] objects = new Object[files.size() + 3];
            int i = 0;
            objects[i++] = message;
            objects[i++] = author;
            objects[i++] = commitDate.toString();
            for (String fileName : files.keySet()) {
                objects[i++] = files.get(fileName).getHash();
            }
            SHA1 = Utils.sha1(objects);
        }
        return SHA1;
    }

    public Commit getParent() { return parent; }

    public String getMessage() { return message; }

    public Set<File> getFiles() {
        Set<File> files = new HashSet<>();
        for (String name: this.files.keySet()) {
            if (this.files.get(name) instanceof RemoveBlob) {
                continue;
            }
            files.add(Utils.join(CWD, name));
        }
        return files;
    }

    public Date getDate() { return commitDate; }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = this.message.hashCode();
            hashCode = 31 * hashCode + this.author.hashCode();
            hashCode = 31 * hashCode + this.commitDate.hashCode();
            hashCode = 31 * hashCode + this.files.hashCode();
            //hashCode = 31
        }
        return hashCode;
    }

    public void printCommit() {
        System.out.print(
                  "===\n"
                + "commit " + getHash() + "\n"
                + "Date: " + dateFormat.format(commitDate) + "\n"
                + message + "\n"
        );
        //printBlobs();
        System.out.println();
    }

    public void printBlobs() {
        for (Blob blob : files.values()) {
            System.out.println(blob.toString());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Commit) {
            return hashCode() == ((Commit) obj).hashCode();
        }
        else return false;
    }
}
