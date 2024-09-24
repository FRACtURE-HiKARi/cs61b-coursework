package gitlet;

import java.io.Serializable;
import java.util.*;

public class Branch implements Serializable{
    public String name;
    public Commit head;
    public boolean isCurrentBranch;
    private int hashCode = 0;

    public Branch(String name, Commit head) {
        this.name = name;
        this.head = head;
    }

    public Branch(Branch b) {
        this.name = b.name;
        this.head = b.head;
        this.isCurrentBranch = b.isCurrentBranch;
        this.hashCode = b.hashCode();
    }

    public void printBranch() {
        if (isCurrentBranch) System.out.print("*");
        System.out.println(name);
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = name.hashCode();
        }
        return hashCode;
    }

    public boolean containsCommit(Commit commit) {
        Commit current = head;
        while (current != null) {
            if (current.equals(commit))
                return true;
            current = current.getParent();
        }
        return false;
    }

    public void rename(String name) {
        this.name = name;
        this.hashCode = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Branch)
            return hashCode() == obj.hashCode();
        else return false;
    }
}
