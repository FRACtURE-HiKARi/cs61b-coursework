package gitlet;

import java.io.Serializable;

public class Branch implements Serializable{
    public String name;
    public Commit head;
    public Commit start;
    public boolean isCurrentBranch;
    public Branch(String name, Commit head) {
        this.name = name;
        this.head = head;
        this.start = head;
    }

    public void printBranch(){
        if (isCurrentBranch) System.out.print("*");
        System.out.println(name);
    }
}
