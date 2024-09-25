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
public class RepositoryBase {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */
    Commit head;
    Set<Commit> commits;
    BlobContainer blobs;
    Map<File, FileStatus> stagedFiles;
    Set<Branch> branches;
    Branch currentBranch;
    Map<String, File> remoteRepos;

    private byte[] cachedData;

    /** The current working directory. */
    File CWD;
    //  = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    File GITLET_DIR;
    File GITLET_FILE;
    File BLOB_DIR;

    /* TODO: fill in the rest of this class. */

    public RepositoryBase() {
        commits = new HashSet<>();
        blobs = new BlobContainer(this);
        stagedFiles = new HashMap<>();
        branches = new HashSet<>();
        remoteRepos = new HashMap<>();
        head = null;
        currentBranch = null;
    }

    public void saveState() {
        if (!GITLET_FILE.exists()) {
            return;
        }
        writeObject(GITLET_FILE, new Status(this));
    }

    public void loadState() {
        Status status = readObject(GITLET_FILE, Status.class);
        head = status.head;
        commits = status.commits;
        blobs = new BlobContainer(status.blobs, this);
        stagedFiles = status.stagedFiles;
        branches = status.branches;
        currentBranch = status.branch;
        remoteRepos = status.remoteRepos;
    }

    public void init() {
        if (!GITLET_DIR.exists()) {
            if (!GITLET_DIR.mkdir()) {
                throw new GitletException("Unable to create directory " + GITLET_DIR);
            }
            try{
                if (!GITLET_FILE.createNewFile()) {
                    throw new GitletException("Unable to create file " + GITLET_FILE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!BLOB_DIR.mkdir()) {
                throw new GitletException("Unable to create directory " + BLOB_DIR);
            }

            makeCommit("initial commit", "initial commit", new Date((long) 0));
            branch("master");
            head.branch = currentBranch;
        } else {
            exitOnCondition(true, "A Gitlet version-control system already exists in the current directory.");
        }
    }

    public void updateHead(Commit c) {
        head = c;
        if (currentBranch != null) {
            currentBranch.head = c;
        }
    }

    private void switchBranchStatus(Branch branch) {
        for (Branch b: branches) b.isCurrentBranch = false;
        currentBranch = branch;
        currentBranch.isCurrentBranch = true;
    }

    public void makeMergeCommit(String message, String author, Commit mergedParent) {
        updateHead(new MergedCommit(message, author, new Date(), head, mergedParent, currentBranch, CWD));
        applyStagedFiles();
    }

    private void makeCommit(String message, String author, Date date) {
        updateHead(new Commit(message, author, date, head, currentBranch, CWD));
        applyStagedFiles();
    }

    public void makeCommit(String message, String author) {
        exitOnCondition(stagedFiles.isEmpty(), "No changes added to the commit.");
        exitOnCondition(message.isEmpty(), "Please enter a commit message.");
        makeCommit(message, author, new Date());
    }

    private void applyStagedFiles() {
        for (File file: stagedFiles.keySet()) {
            if (stagedFiles.get(file).equals(FileStatus.NotModified)) {
                continue;
            }
            Blob blob = null;
            cachedData = null;
            if (stagedFiles.get(file).equals(FileStatus.Removed)) {
                blob = new RemoveBlob(file, this);
            } else {
                blob = blobs.createNewBlob(file, head);
            }
            if (blob == null) {
                continue;
            }
            blobs.add(blob);
            head.addFile(file, blob);
        }
        stagedFiles.clear();
        commits.add(head);
    }

    public void add(String file) {
        add(join(CWD, file));
    }

    private void add(File file) {
        exitOnCondition(!file.exists(), "File does not exist.");
        FileStatus status = checkStatus(file);
        if (status == FileStatus.NotModified) {
            exitOnCondition(!Objects.equals(stagedFiles.get(file), FileStatus.Removed), null);
            stagedFiles.remove(file);
        } else {
            stagedFiles.put(file, checkStatus(file));
        }
    }

    public void remove(String file) {
        remove(join(CWD, file));
    }

    private void remove(File file) {
        if (head.contains(file)) {
            stagedFiles.put(file, FileStatus.Removed);
            if (file.exists()) {
                restrictedDelete(file);
            }
        } else if (stagedFiles.containsKey(file)) {
            stagedFiles.remove(file);
        } else {
            exitOnCondition(true, "No reason to remove the file.");
        }
    }

    public void status() {
        new Status(this).printStatus();
    }

    public void log() {
        printCommitsRecursive(head, 0, 10000000);
    }

    public void globalLog() {
        Commit[] array = new Commit[commits.size()];
        commits.toArray(array);
        Arrays.sort(array, Comparator.comparing(Commit::getDate).reversed());
        for (Commit c: array) {
            c.printCommit();
        }
    }

    private void printCommitsRecursive(Commit commit, int depth, int stop_depth) {
        if (depth >= stop_depth) return;
        if (commit == null) return;
        commit.printCommit();
        printCommitsRecursive(commit.getParent(), depth + 1, stop_depth);
    }

    public void find(String msg) {
        Boolean found = false;
        for (Commit commit: commits) {
            if (commit.getMessage().contains(msg)) {
                commit.printCommit();
                found = true;
            }
        }
        exitOnCondition(!found, "Found no commit with that message.");
    }

    public void checkout(String arg) {
        // search with file name
        for (File file: head.getFiles()) {
            if (file.getName().equals(arg)) {
                checkoutFileInCommit(head, file);
                return;
            }
        }
        // search with branch name
        //System.out.println("Checking out branch " + arg);
        Branch target = getBranch(arg);
        if (target == null) {
            throw new GitletException("Branch " + arg + " not found.");
        }
        checkoutBranch(target);
    }

    public void checkout(String commitID, String fileName) {
        Commit c = getCommit(commitID);
        if (c != null && c.contains(join(CWD, fileName))) {
            checkoutFileInCommit(c, join(CWD, fileName));
        }
        else
            throw new GitletException("Commit " + commitID + " with file " + fileName + " not found.");
    }

    public Commit getCommit(String shaVal) {
        for (Commit c: commits)
            if (c.getHash().startsWith(shaVal)) {
                return c;
            }
        return null;
    }

    public void checkoutBranch(Branch b) {
        checkoutCommit(b.head);
    }

    public void checkoutCommit(Commit c) {
        switchBranchStatus(c.branch);
        //System.out.println("Checking out commit:");
        //c.printCommit();
        for (File file: c.getFiles()) {
            checkoutFileInCommit(c, file);
        }
        updateHead(c);
    }

    private void checkoutFileInCommit(Commit c, File f) {
        Blob blob = c.getBlob(f);
        assert blob != null;
        //System.out.println(f + " -- " + blob.getHash());
        blob.recoverFile();
    }

    public void branch(String branch) {
        for (Branch b: branches) {
            if (b.name.equals(branch)) {
                throw new GitletException("Branch " + b.name + " already exists.");
            }
        }
        currentBranch = new Branch(branch, head);
        switchBranchStatus(currentBranch);
        branches.add(currentBranch);
    }

    public void rmBranch(String branch) {
        Branch target = getBranch(branch);
        if (target == null) {
            throw new GitletException("Branch " + branch + " not found.");
        }
        branches.remove(target);
    }

    public void rmBranch(Branch branch) {
        branches.remove(branch);
    }

    public void reset(String commitID) {
        Commit c = getCommit(commitID);
        if (c != null)
            checkoutCommit(c);
        else
            throw new GitletException("Commit " + commitID + " not found.");
    }

    public void merge(String branch) {
        Branch target = getBranch(branch);
        if (target == null) throw new GitletException("Branch " + branch + " not found.");
        merge(target);
        checkoutBranch(currentBranch);
    }

    public void merge(Branch a, Branch b) {
        if (Objects.equals(a, b)) return;
        Status currentStatus = new Status(this, a.head);
        if (!currentStatus.modifiedFile.isEmpty() && !currentStatus.stagedFiles.isEmpty())
            throw new GitletException("Working directory not clean. Make commit first.");
        Commit start = getCommonParent(b.head, a.head);
        for (File file: listFiles(CWD)) {
            if (a.head.contains(file) && b.head.contains(file)) {
                if (start.contains(file)) {
                    // Case 1
                    if (
                            notModifedBetweenCommits(file, a.head, start) &&
                                    !notModifedBetweenCommits(file, b.head, start)
                    ) {
                        checkoutFileInCommit(b.head, file);
                        add(file);
                    }
                    // Case 2 stays untouched
                    continue;
                    // Case 8
                }
                if (!notModifedBetweenCommits(file, b.head, a.head)) {
                    mergeConflictFiles(file, a.head, b.head);
                    add(file);
                }
            } else if (a.head.contains(file)) {
                // Case 4 here
                // Case 6
                if (notModifedBetweenCommits(file, a.head, start)) {
                    remove(file);
                }
            } else if (b.head.contains(file)) {
                // Case 5
                if (!start.contains(file)) {
                    checkoutFileInCommit(b.head, file);
                    add(file);
                }
                // Case 7 here
            }
        }
        String msg = "Merging commits " + a.head.getHash().substring(0, 7) + " and " + b.head.getHash().substring(0, 7);
        makeMergeCommit(msg, "61b-student", b.head);
    }

    public void merge(Branch branch) {
        merge(currentBranch, branch);
    }

    public void addRemote(String name, String destination) {
        remoteRepos.put(name, join(CWD, destination));
    }

    public void rmRemote(String name) {
        remoteRepos.remove(name);
    }

    public Branch addNewBranch(Branch remoteBranch) {
        Branch localBranch = getBranch(remoteBranch.name);
        remoteBranch = new Branch(remoteBranch);
        if (localBranch != null) {
            remoteBranch.rename(remoteBranch.name + "(remote)");
        }

        Commit c = remoteBranch.head;
        Stack<Commit> stack = new Stack<>();
        while (c != null) {
            //c.printCommit();
            c = new Commit(c);
            if (!stack.isEmpty()) {
                stack.firstElement().parent = c;
            }
            stack.push(c);

            if (commits.contains(c.parent)) {
                c.parent = getCommit(c.parent.getHash());
                //remoteBranch.start = c.parent;
                while (!stack.isEmpty()) {
                    c = stack.pop();
                    c.branch = remoteBranch;
                    fetchCommitWithBlobs(c);
                }

                if (localBranch != null) {
                    combineBranches(remoteBranch, localBranch);
                    rmBranch(remoteBranch);
                    return localBranch;
                } else {
                    branches.add(remoteBranch);
                    remoteBranch.head = getCommit(remoteBranch.head.getHash());
                    return remoteBranch;
                }
            }
            c = c.parent;
        }

        throw new GitletException("Unable to add branch "+ remoteBranch.name + ": no matching start commit.");
    }

    // combine stuff from source to target
    public void combineBranches(Branch source, Branch target) {
        if (source.containsCommit(target.head)) {
            Commit c;
            Commit sourceHead = getCommit(source.head.getHash());
            for (c = sourceHead; !c.parent.equals(target.head); c = c.parent) {
                c.branch = target;
            }
            c.parent = target.head;
            c.branch = target;
            target.head = sourceHead;
        }
    }

    void fetchCommitWithBlobs(Commit c) {
        c.CWD = Repository.CWD;
        getRemoteBlobs(c);
    }

    void getRemoteBlobs(Commit c) {
        commits.add(c);
        for (String name: c.files.keySet()) {
            Blob b = c.files.get(name);
            byte[] data = b.getContents();
            b.CWD = c.CWD;
            b.base = join(c.CWD, ".gitlet/blobs");
            blobs.add(b, data);
        }
    }

    public RepositoryBase getRemote(String remoteName) {
        LocalRemoteRepo remoteRepo = new LocalRemoteRepo(remoteRepos.get(remoteName));
        if (!remoteRepo.GITLET_DIR.exists())
            throw new GitletException("Remote repository does not exist");
        return remoteRepo;
    }

    private void mergeConflictBlobs(File f, Blob b1, Blob b2) {
        String c1 = blobs.getContentsAsString(b1);
        String c2 = blobs.getContentsAsString(b2);
        String output = "<<<<<<< HEAD\n" + c1 + "=======\n" + c2 + ">>>>>>>\n";
        Utils.writeContents(f, output);
    }

    private void mergeConflictFiles(File f, Commit c1, Commit c2) {
        System.out.println("File " + f + "conflicts in two commits. Keeping both contents");
        mergeConflictBlobs(f, c1.getBlob(f), c2.getBlob(f));
    }

    private boolean sameBlob(Blob a, Blob b) {
        return Objects.equals(a.getHash(), b.getHash());
    }

    private boolean notModifedBetweenCommits(File f, Commit a, Commit b) {
        if (Objects.equals(a, b)) return true;
        else return a.contains(f)
                && b.contains(f)
                && sameBlob(a.getBlob(f), b.getBlob(f));
    }

    private static boolean checkDifference(Commit c, File file, byte[] data) {
        String name = file.getName();
        String hash = sha1(name, data);
        Blob blob = c.getBlob(file);
        return blob == null || !Objects.equals(blob.getHash(), hash);
    }

    private boolean differs(File file) {
        if (!file.exists()) {
            throw new GitletException("File does not exist: " + file);
        } else {
            cachedData = readContents(file);
            return checkDifference(head, file, cachedData);
        }
    }

    public Commit getCommonParent(Commit c1, Commit c2) {
        if (c1.height > c2.height) {
            while (c1.height > c2.height) {
                c1 = c1.parent;
            }
        }
        else if (c1.height < c2.height) {
            while (c1.height < c2.height) {
                c2 = c2.parent;
            }
        }
        while (c1 != c2) {
            c1 = c1.parent;
            c2 = c2.parent;
        }
        return c1;
    }

    public Branch getBranch(String branch) {
        for (Branch b: branches) {
            if (b.name.equals(branch)) return b;
        }
        //throw new GitletException("Branch " + branch + " not found.");
        return null;
    }

    /*
     * sets cachedData
     */
    public static boolean differs(Commit c, File file) {
        if (!file.exists()) {
            throw new GitletException("File does not exist: " + file);
        } else {
            byte[] data = readContents(file);
            return checkDifference(c, file, data);
        }
    }

    private FileStatus checkStatus(File f) {
        if (differs(f)) {
            return FileStatus.Modified;
        }
        if (!head.contains(f)) {
            return FileStatus.New;
        }
        else {
            return FileStatus.NotModified;
        }
    }

    public void debug() {
        System.out.println("Base debug function called.");
    }
}
