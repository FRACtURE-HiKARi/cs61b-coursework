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
    protected String message;
    protected String author;
    //private String email;
    protected Date commitDate;
    protected Commit parent;
    //private Collection<Commit> children;
    protected Map<File, Blob> files;
    protected int hashCode = 0;
    protected String SHA1 = null;
    protected Branch branch;
    public static DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.ENGLISH);

    /* TODO: fill in the rest of this class. */
    public Commit(String message, String author, Date commitDate, Commit parent, Branch branch) {
        this.message = message;
        this.author = author;
        this.commitDate = commitDate;
        this.parent = parent;
        this.branch = branch;
        files = new HashMap<>();
        if (parent != null) files.putAll(parent.files);
    }

    public void addFile(String fileName, Blob blob){
        files.put(new File(fileName), blob);
    }

    public void addFile(File file, Blob blob){
        files.put(file, blob);
    }

    public Blob getBlob(File file){
        return files.get(file);
    }

    public boolean contains(File file){
        return files.containsKey(file);
    }

    public String getHash(){
        if (SHA1 == null) {
            Object[] objects = new Object[files.size() + 3];
            int i = 0;
            objects[i++] = message;
            objects[i++] = author;
            objects[i++] = commitDate.toString();
            for (File file : files.keySet()) {
                objects[i++] = files.get(file).getHash();
            }
            SHA1 = Utils.sha1(objects);
        }
        return SHA1;
    }

    public Commit getParent() { return parent; }

    public String getMessage() { return message; }

    public Set<File> getFiles() { return files.keySet(); }

    public Date getDate() { return commitDate; }

    @Override
    public int hashCode(){
        if (hashCode == 0) {
            hashCode = this.message.hashCode();
            hashCode = 31 * hashCode + this.author.hashCode();
            hashCode = 31 * hashCode + this.commitDate.hashCode();
            hashCode = 31 * hashCode + this.files.hashCode();
            //hashCode = 31
        }
        return hashCode;
    }

    public void printCommit(){
        System.out.print(
                  "===\n"
                + "commit " + getHash() + "\n"
                + "Date: " + dateFormat.format(commitDate) + "\n"
                + message + "\n"
        );
        //printBlobs();
        System.out.println();
    }

    public void printBlobs(){
        for (Blob blob : files.values()) {
            System.out.println(blob.toString());
        }
    }
}
