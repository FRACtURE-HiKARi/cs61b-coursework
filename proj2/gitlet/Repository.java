package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository{
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */
    public Commit head;
    public Set<Commit> commits;
    public BlobContainer blobs;
    public Set<File> stagedFiles;
    public Set<Branch> branches;
    public Branch currentBranch;
    private byte[] cachedData;

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File GITLET_FILE = join(GITLET_DIR, ".gitlet");
    public static final File BLOB_DIR = join(GITLET_DIR, "blobs");

    /* TODO: fill in the rest of this class. */

    public Repository(){
        if (GITLET_DIR.exists()){
            loadState();
        } else {
            commits = new HashSet<>();
            blobs = new BlobContainer();
            stagedFiles = new HashSet<>();
            branches = new HashSet<>();
            head = null;
            currentBranch = null;
        }
    }

    public void saveState(){
        if (!GITLET_FILE.exists()) return;
        writeObject(GITLET_FILE, new Status(this));
    }

    public void loadState(){
        Status status = readObject(GITLET_FILE, Status.class);
        head = status.head;
        commits = status.commits;
        blobs = new BlobContainer(status.blobs);
        stagedFiles = status.stagedFiles;
        branches = status.branches;
        currentBranch = status.branch;
    }

    public void init(){
        if (!GITLET_DIR.exists()){
            if (!GITLET_DIR.mkdir())
                throw new GitletException("Unable to create directory " + GITLET_DIR);
            try{
                if (!GITLET_FILE.createNewFile())
                    throw new GitletException("Unable to create file " + GITLET_FILE);
            } catch (IOException e){
                e.printStackTrace();
            }
            if (!BLOB_DIR.mkdir())
                throw new GitletException("Unable to create directory " + BLOB_DIR);

            // TODO: solve Linux Start timestamp
            makeCommit("Initial commit.", "initial commit", new Date((long) 0));
            branch("master");
            head.branch = currentBranch;
        } else {
            throw new GitletException(
                    "A Gitlet version-control system already exists in the current directory."
            );
        }
    }

    private void updateHead(Commit c){
        head = c;
        if (currentBranch != null) currentBranch.head = c;
    }

    private void switchBranchStatus(Branch b){
        if(currentBranch != null) currentBranch.active = false;
        currentBranch = b;
        currentBranch.active = true;
    }

    public void makeMergeCommit(String message, String author, Commit mergedParent){
        updateHead(new MergedCommit(message, author, new Date(), head, mergedParent, currentBranch));
        updateFilesHead();
    }

    private void makeCommit(String message, String author, Date date){
        updateHead(new Commit(message, author, date, head, currentBranch));
        updateFilesHead();
    }

    public void makeCommit(String message, String author){
        makeCommit(message, author, new Date());
    }

    private void updateFilesHead(){
        Status status = new Status(this);
        for (File file: stagedFiles){
            Blob blob = null;
            cachedData = null;
            if (status.removedFile.contains(file)){
                blob = new Blob(-1, file, null);
            } else {
                blob = createNewBlob(file);
            }
            blobs.add(blob, cachedData);
            head.addFile(file, blob);
        }
        stagedFiles.clear();
        commits.add(head);
    }

    public void add(String file){
        add(join(CWD, file));
    }

    private void add(File file){
        stagedFiles.add(file);
    }

    private Blob createNewBlob(File file){
        if (differs(file)){
            Blob blob = head.getBlob(file);
            int version = (blob == null) ? 0 : blob.getVersion() + 1;
            return new Blob(version, file, cachedData);
        } else {
            return null;
        }
    }

    public void remove(String file){
        remove(join(CWD, file));
    }

    private void remove(File file){
        if (!file.exists()){
            throw new GitletException("File does not exist: " + file);
        } else {
            if (!stagedFiles.contains(file)){
                throw new GitletException("File does not tracked: " + file);
            }
            stagedFiles.remove(file);
        }
    }

    public void status(){
        new Status(this).printStatus();
    }

    public void log(){
        printCommitsRecursive(head, 0, 10000000);
    }

    public void globalLog(){
        Commit[] array = new Commit[commits.size()];
        commits.toArray(array);
        Arrays.sort(array, Comparator.comparing(Commit::getDate).reversed());
        for (Commit c: array){
            c.printCommit();
        }
    }

    private void printCommitsRecursive(Commit commit, int depth, int stop_depth){
        if (depth >= stop_depth) return;
        if (commit == null) return;
        commit.printCommit();
        printCommitsRecursive(commit.getParent(), depth + 1, stop_depth);
    }

    public void find(String msg){
        for (Commit commit: commits){
            if (commit.getMessage().contains(msg)){
                commit.printCommit();
            }
        }
    }

    public void checkout(String arg){
        // search with file name
        for (File file: head.getFiles()) {
            if (file.getName().equals(arg)){
                checkoutFileInCommit(head, file);
                return;
            }
        }
        // search with branch name
        System.out.println("Checking out branch " + arg);
        checkoutBranch(getBranch(arg));
    }

    public void checkout(String commitID, String fileName){
        for (Commit commit: commits){
            if (commit.getHash().indexOf(commitID) == 0 && commit.contains(join(CWD, fileName))){
                checkoutFileInCommit(commit, join(CWD, fileName));
                return;
            }
        }
        throw new GitletException("Commit " + commitID + " with file " + fileName + " not found.");
    }

    private void checkoutBranch(Branch b){
        checkoutCommit(b.head);
    }

    private void checkoutCommit(Commit c){
        switchBranchStatus(c.branch);
        System.out.println("Checking out commit:");
        c.printCommit();
        for (File file: c.getFiles()) {
            checkoutFileInCommit(c, file);
        }
        updateHead(c);
    }

    private void checkoutFileInCommit(Commit c, File f){
        Blob blob = c.getBlob(f);
        assert blob != null;
        System.out.println(f + " -- " + blob.getHash());
        File file = blob.getFile();
        if (!blob.isDeleteBlob()) {
            if (!file.exists()) try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writeContents(f, (Object) blobs.getContents(blob));
        } else {
            restrictedDelete(file);
        }
    }

    public void branch(String branch){
        for (Branch b: branches){
            if (b.name.equals(branch))
                throw new GitletException("Branch " + b.name + " already exists.");
        }
        currentBranch = new Branch(branch, head);
        for (Branch b: branches) b.active = false;
        branches.add(currentBranch);
        currentBranch.active = true;
    }

    public void rmBranch(String branch){
        branches.remove(getBranch(branch));
    }

    public void reset(String commitID){
        for (Commit commit: commits){
            if (commit.getHash().indexOf(commitID) == 0) {
                checkoutCommit(commit);
                return;
            }
        }
        throw new GitletException("Commit " + commitID + " not found.");
    }

    public void merge(String branch){
        merge(getBranch(branch));
    }

    public void merge(Branch branch){
        Status currentStatus = new Status(this, head);
        if (!currentStatus.modifiedFile.isEmpty() && !currentStatus.stagedFiles.isEmpty())
            throw new GitletException("Working directory not clean. Make commit first.");
        Status targetStatus = new Status(this, branch.head);
        for (File file: listFiles(CWD)) {
            if (head.contains(file) && branch.head.contains(file)){
                if (branch.start.contains(file)){
                    // Case 1
                    if (
                            notModifedBetweenCommits(file, head, branch.start) &&
                            !notModifedBetweenCommits(file, branch.head, branch.start)
                    ){
                        checkoutFileInCommit(branch.head, file);
                        add(file);
                    }
                    // Case 2 stays untouched
                // Case 8
                } else if (!notModifedBetweenCommits(file, branch.head, head)){
                    mergeConflictFiles(file, head, branch.head);
                    add(file);
                }
            } else if (head.contains(file)){
                // Case 4 here
                // Case 6
                if (notModifedBetweenCommits(file, head, branch.start)){
                    remove(file);
                }
            } else if (branch.head.contains(file)){
                // Case 5
                if (!branch.start.contains(file)){
                    checkoutFileInCommit(branch.head, file);
                    add(file);
                }
                // Case 7 here
            }
        }
        String msg = "Merging commits " + head.getHash().substring(0, 7) + " and " + branch.head.getHash().substring(0, 7);
        makeMergeCommit(msg, System.getProperty("user.name"), branch.head);
    }

    private void mergeConflictBlobs(File f, Blob b1, Blob b2){
        String c1 = blobs.getContentsAsString(b1);
        String c2 = blobs.getContentsAsString(b2);
        String output = "<<<<<<< HEAD\n" + c1 + "=======\n" + c2 + ">>>>>>>\n";
        Utils.writeContents(f, output);
    }

    private void mergeConflictFiles(File f, Commit c1, Commit c2){
        System.out.println("File " + f + "conflicts in two commits. Keeping both contents");
        mergeConflictBlobs(f, c1.getBlob(f), c2.getBlob(f));
    }

    private boolean sameBlob(Blob a, Blob b){
        return Objects.equals(a.getHash(), b.getHash());
    }

    private boolean notModifedBetweenCommits(File f, Commit a, Commit b){
        if (
                a.contains(f)
                && b.contains(f)
                && sameBlob(a.getBlob(f), b.getBlob(f))
        ) return true;
        else return false;
    }

    private static boolean checkDifference(Commit c, File file, byte[] data){
        String name = file.getAbsolutePath();
        String hash = sha1(name, data);
        Blob blob = c.getBlob(file);
        if (blob == null || !Objects.equals(blob.getHash(), hash)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean differs(File file){
        if (!file.exists()){
            throw new GitletException("File does not exist: " + file);
        } else {
            cachedData = readContents(file);
            return checkDifference(head, file, cachedData);
        }
    }

    private Branch getBranch(String branch){
        for (Branch b: branches){
            if (b.name.equals(branch)) return b;
        }
        throw new GitletException("Branch " + branch + " not found.");
    }

    public static boolean differs(Commit c, File file){
        if (!file.exists()){
            throw new GitletException("File does not exist: " + file);
        } else {
            byte[] data = readContents(file);
            return checkDifference(c, file, data);
        }
    }

    public void debug(){
        currentBranch.head.printCommit();
    }
}
