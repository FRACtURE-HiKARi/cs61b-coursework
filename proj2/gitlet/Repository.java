package gitlet;

import java.io.File;
import java.util.*;

public class Repository extends RepositoryBase{
    public static File CWD = new File(System.getProperty("user.dir"));
    public static File GITLET_DIR = new File(CWD, ".gitlet");
    public static File GITLET_FILE = new File(GITLET_DIR, ".gitlet");
    public static File BLOB_DIR = new File(GITLET_DIR, "blobs");

    public Repository() {
        super();
        super.CWD = CWD;
        super.GITLET_DIR = GITLET_DIR;
        super.GITLET_FILE = GITLET_FILE;
        super.BLOB_DIR = BLOB_DIR;
        super.blobs = new BlobContainer(this);
        if (GITLET_DIR.exists()) {
            loadState();
        }
    }

    @Override
    public void debug() {
        //getCommonParent(getBranch("nb").head, getBranch("master").head).printCommit();
        remoteStatus();
    }

    public void remoteStatus() {
        RepositoryBase r = getRemote("origin");
        r.status();
        r.log();
    }

    public Branch fetch(String remoteName, String branchName) {
        RepositoryBase remoteRepo = getRemote(remoteName);
        Branch remoteBranch = remoteRepo.getBranch(branchName);
        if (remoteBranch == null) {
            throw new GitletException("Branch " + branchName + " in remote " + remoteName + " not found.");
        }
        return addNewBranch(remoteBranch);
    }

    public void pull(String remoteName, String branchName) {
        Branch newBranch = fetch(remoteName, branchName);
        //System.err.println(newBranch.name + " " + currentBranch.name);
        merge(newBranch);
        rmBranch(newBranch);
        checkoutBranch(currentBranch);
    }

    public void push(String remoteName, String branchName) {
        Branch b = getBranch(branchName);
        if (b == null) {
             throw new GitletException("Branch " + branchName + " not found.");
        }
        RepositoryBase remoteRepo = getRemote(remoteName);
        remoteRepo.checkoutBranch(remoteRepo.addNewBranch(b));
        remoteRepo.saveState();
    }

}
