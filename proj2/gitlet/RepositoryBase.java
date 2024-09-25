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
            switchBranchStatus(getBranch("master"));
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
        boolean found = false;
        for (Commit commit: commits) {
            if (commit.getMessage().contains(msg)) {
                System.out.println(commit.getHash());
                found = true;
            }
        }
        exitOnCondition(!found, "Found no commit with that message.");
    }

    public void checkoutBranch(String branchName) {
        // search with branch name
        //System.out.println("Checking out branch " + arg);
        Branch target = getBranch(branchName);
        exitOnCondition(target == null, "No such branch exists.");
        exitOnCondition(target.equals(currentBranch), "No need to checkout the current branch.");
        checkoutBranch(target);
    }

    public void checkoutFile(String filename){
        for (File file: head.getFiles()) {
            if (file.getName().equals(filename)) {
                checkoutFileInCommit(head, file);
                return;
            }
        }
        exitOnCondition(true, "File does not exist in that commit.");
    }

    public void checkout(String commitID, String fileName) {
        Commit c = getCommit(commitID);
        exitOnCondition(c == null, "No commit with that id exists.");
        if (c.contains(join(CWD, fileName))) {
            checkoutFileInCommit(c, join(CWD, fileName));
            return;
        }
        exitOnCondition(true, "File does not exist in that commit.");
    }

    public Commit getCommit(String shaVal) {
        for (Commit c: commits)
            if (c.getHash().startsWith(shaVal)) {
                return c;
            }
        return null;
    }

    public void checkoutBranch(Branch b) {
        switchBranchStatus(b);
        checkoutCommit(b.head);
    }

    public void checkoutCommit(Commit c) {
        Status s = new Status(this);
        s.checkFileStatus(c);
        for (File file: head.getFiles()) {
            restrictedDelete(file);
        }
        for (File file: c.getFiles()) {
            exitOnCondition(s.untrackedFile.contains(file) && differs(c, file),
                    "There is an untracked file in the way; delete it, or add and commit it first.");
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

    public void branch(String branchName) {
        for (Branch b: branches) {
            exitOnCondition(b.name.equals(branchName), "A branch with that name already exists.");
        }
        branches.add(new Branch(branchName, head));
    }

    public void rmBranch(String branch) {
        Branch target = getBranch(branch);
        exitOnCondition(target == null, "A branch with that name does not exist.");
        exitOnCondition(target.equals(currentBranch), "Cannot remove the current branch.");
        branches.remove(target);
    }

    public void rmBranch(Branch branch) {
        branches.remove(branch);
    }

    public void reset(String commitID) {
        Commit c = getCommit(commitID);
        exitOnCondition(c == null, "No commit with that id exists.");
        stagedFiles.clear();
        checkoutCommit(c);
    }

    public void merge(String branch) {
        Branch target = getBranch(branch);
        exitOnCondition(target == null, "A branch with that name does not exist.");
        merge(target);
        checkoutBranch(currentBranch);
    }

    public void merge(Branch main, Branch other) {
        exitOnCondition(Objects.equals(main, other), "Cannot merge a branch with itself.");
        if(main.containsCommit(other.head)) {
            System.out.println("Given branch is an ancestor of the current branch.");
        }
        if (other.containsCommit(main.head)) {
            System.out.println("Current branch fast-forwarded.");
        }
        Status s = new Status(this, main.head);
        exitOnCondition(!s.untrackedFile.isEmpty(),
                "There is an untracked file in the way; delete it, or add and commit it first.");
        exitOnCondition(!s.stagedFiles.isEmpty(), "You have uncommitted changes.");

        Commit start = getCommonParent(other.head, main.head);
        Set<File> filesToCheck = new HashSet<>();
        filesToCheck.addAll(main.head.getFiles());
        filesToCheck.addAll(other.head.getFiles());
        for (File file: filesToCheck) {
            if (main.head.contains(file) && other.head.contains(file)) {
                if (start.contains(file)) {
                    // Case 1
                    if (
                            notModifedBetweenCommits(file, main.head, start) &&
                                    !notModifedBetweenCommits(file, other.head, start)
                    ) {
                        checkoutFileInCommit(other.head, file);
                        add(file);
                        continue;
                    }
                    // Case 2 stays untouched
                    else if (notModifedBetweenCommits(file, other.head, start)){
                        continue;
                    }
                    // Case 8
                }
                if (!notModifedBetweenCommits(file, other.head, main.head)) {
                    mergeConflictFiles(file, main.head, other.head);
                    add(file);
                }
            } else if (main.head.contains(file)) {
                // Case 4 here
                // Case 6
                if (notModifedBetweenCommits(file, main.head, start)) {
                    remove(file);
                } else if (start.contains(file)){
                    // case 8
                    mergeConflictFiles(file, main.head, other.head);
                    add(file);
                }
            } else if (other.head.contains(file)) {
                // Case 5
                if (!start.contains(file)) {
                    checkoutFileInCommit(other.head, file);
                    add(file);
                }
                // Case 7 here
                else {
                    if (notModifedBetweenCommits(file, other.head, start)){
                        stagedFiles.put(file, FileStatus.Removed);
                    } else {
                        // case 8
                        mergeConflictFiles(file, main.head, other.head);
                        add(file);
                    }
                }
            }
        }
        String msg = "Merged " + other.name + " into " + main.name + ".";
        makeMergeCommit(msg, "61b-student", other.head);
    }

    public void merge(Branch branch) {
        merge(currentBranch, branch);
    }

    public void addRemote(String name, String destination) {
        exitOnCondition(remoteRepos.containsKey(name), "A remote with that name already exists.");
        remoteRepos.put(name, join(CWD, destination));
    }

    public void rmRemote(String name) {
        exitOnCondition(remoteRepos.remove(name) == null, "A remote with that name does not exist.");
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
        exitOnCondition(!remoteRepo.GITLET_DIR.exists(), "Remote directory not found.");
        return remoteRepo;
    }

    private void mergeConflictBlobs(File f, Blob b1, Blob b2) {
        String c1 = blobs.getContentsAsString(b1);
        String c2 = blobs.getContentsAsString(b2);
        String output = "<<<<<<< HEAD\n" + c1 + "=======\n" + c2 + ">>>>>>>\n";
        Utils.writeContents(f, output);
    }

    private void mergeConflictFiles(File f, Commit c1, Commit c2) {
        System.out.println("Encountered a merge conflict.");
        mergeConflictBlobs(f, c1.getBlob(f), c2.getBlob(f));
    }

    private boolean sameBlob(Blob a, Blob b) {
        return Objects.equals(a.getHash(), b.getHash());
    }

    private boolean notModifedBetweenCommits(File f, Commit a, Commit b) {
        boolean aContains = a.contains(f);
        boolean bContains = b.contains(f);
        if (Objects.equals(a, b) || !(aContains || bContains)) {
            return true;
        } else if (aContains ^ bContains) {
            return false;
        }
        else {
            return sameBlob(a.getBlob(f), b.getBlob(f));
        }
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
        PriorityQueue<Commit> queue = new PriorityQueue<>(Comparator.comparing(Commit::height).reversed());
        queue.add(c1);
        queue.add(c2);
        Commit c = null;
        while (!queue.isEmpty()) {
            c = queue.remove();
            if (queue.contains(c)) {
                return c;
            }
            queue.addAll(c.getAllParents());
        }
        // it must be the commit at height 0
        return c;
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
