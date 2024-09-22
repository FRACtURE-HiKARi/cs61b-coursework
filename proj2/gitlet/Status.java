package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

public class Status implements Serializable {
    public Commit head;
    public Branch branch;
    public Set<Commit> commits;
    public Set<Blob> blobs;
    public Set<File> stagedFiles;
    public Set<Branch> branches;

    public transient Set<File> removedFile;
    public transient Set<File> modifiedFile;
    public transient Set<File> untrackedFile;

    public Status(Repository repo){
        initWithRepo(repo);
        checkFileStatus();
    }

    public Status(Repository repo, Commit targetCommit){
        initWithRepo(repo);
        checkFileStatus(targetCommit);
    }

    private void initWithRepo(Repository repo){
        this.head = repo.head;
        this.commits = repo.commits;
        this.blobs = repo.blobs.getBlobs();
        this.stagedFiles = repo.stagedFiles;
        this.branch = repo.currentBranch;
        this.branches = repo.branches;
        this.removedFile = new HashSet<>();
        this.modifiedFile = new HashSet<>();
        this.untrackedFile = new HashSet<>();
    }

    public void printStatus(){
        System.out.println("=== Branches ===");
        for (Branch b : branches) b.printBranch();
        System.out.println("\n=== Staged Files ===");
        for (File f : stagedFiles) System.out.println(f.getName());
        System.out.println("\n=== Removed Files ===");
        for (File f: removedFile) System.out.println(f.getName());
        System.out.println("\n=== Modifications Not Staged ===");
        for (File f: modifiedFile) System.out.println(f.getName());
        System.out.println("\n=== Untracked Files ===");
        for (File f: untrackedFile) System.out.println(f.getName());
        System.out.println();
    }

    public void checkFileStatus(){
        checkFileStatus(head);
    }

    public void checkFileStatus(Commit c){
        File[] files = Utils.listFiles(Repository.CWD);
        assert files != null;
        Set<File> fileSet = new HashSet<>();
        for (File f: files){
            fileSet.add(f);
            if (!c.contains(f) && !stagedFiles.contains(f)) untrackedFile.add(f);
            else if (!stagedFiles.contains(f) & Repository.differs(c, f)) modifiedFile.add(f);
        }
        for (File f: c.getFiles()){
            if (!fileSet.contains(f)) removedFile.add(f);
        }
    }
}