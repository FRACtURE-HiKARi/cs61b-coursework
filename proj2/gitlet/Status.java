package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class Status implements Serializable {
    public Commit head;
    public Branch branch;
    public Set<Commit> commits;
    public Set<Blob> blobs;
    public Map<File, FileStatus> stagedFiles;
    public Set<Branch> branches;
    public Map<String, File> remoteRepos;

    public transient Set<File> removedFile;
    public transient Set<File> modifiedFile;
    public transient Set<File> untrackedFile;
    transient File workingDirectory;

    public Status(RepositoryBase repo) {
        initWithRepo(repo);
        checkFileStatus();
    }

    public Status(RepositoryBase repo, Commit targetCommit) {
        initWithRepo(repo);
        checkFileStatus(targetCommit);
    }

    private void initWithRepo(RepositoryBase repo) {
        this.head = repo.head;
        this.commits = repo.commits;
        this.blobs = repo.blobs.getBlobs();
        this.stagedFiles = repo.stagedFiles;
        this.branch = repo.currentBranch;
        this.branches = repo.branches;
        this.removedFile = new HashSet<>();
        this.modifiedFile = new HashSet<>();
        this.untrackedFile = new HashSet<>();
        this.remoteRepos = repo.remoteRepos;
        this.workingDirectory = repo.CWD;
    }

    public void printStatus() {
        System.out.println("=== Branches ===");
        this.branch.printBranch();
        for (Branch b : branches) {
            if (!b.isCurrentBranch){
                b.printBranch();
            }
        }
        System.out.println("\n=== Staged Files ===");
        for (File f : stagedFiles.keySet())
            if (!stagedFiles.get(f).equals(FileStatus.Removed)) {
                System.out.println(f.getName());
            }
        System.out.println("\n=== Removed Files ===");
        for (File f : stagedFiles.keySet()) {
            if (stagedFiles.get(f).equals(FileStatus.Removed)) {
                System.out.println(f.getName());
            }
        }
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        for (File f: modifiedFile) {
            System.out.println(f.getName() + " (modified)");
        }
        for (File f: removedFile) {
            System.out.println(f.getName() + " (deleted)");
        }
        System.out.println("\n=== Untracked Files ===");
        for (File f: untrackedFile) {
            System.out.println(f.getName());
        }
        System.out.println();
    }

    public void checkFileStatus() {
        checkFileStatus(head);
    }

    public void checkFileStatus(Commit c) {
        File[] files = Utils.listFiles(workingDirectory);
        assert files != null;
        Set<File> fileSet = new HashSet<>();
        for (File f: files) {
            fileSet.add(f);
            if (!c.contains(f) && !stagedFiles.containsKey(f)) {
                untrackedFile.add(f);
            }
            else if (!stagedFiles.containsKey(f) && Repository.differs(c, f)) {
                modifiedFile.add(f);
            }
        }
        for (File f: c.getFiles()) {
            if (!fileSet.contains(f) && !stagedFiles.containsKey(f)) {
                removedFile.add(f);
            }
        }
    }
}