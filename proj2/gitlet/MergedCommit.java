package gitlet;

import java.io.File;
import java.util.Date;

public class MergedCommit extends Commit {

    Commit directParent;
    Commit mergedParent;
    public MergedCommit(
            String message,
            String author,
            Date commitDate,
            Commit directParent,
            Commit mergedParent,
            Branch branch,
            File CWD) {
        super(message, author, commitDate, directParent, branch, CWD);
        files.putAll(mergedParent.files);
        this.directParent = directParent;
        this.mergedParent = mergedParent;
    }

    @Override
    public void printCommit() {
        System.out.print(
                "===\n"
                        + "commit " + getHash() + "\n"
                        + "Merge: " + directParent.getHash().substring(0, 7) + " "
                        + mergedParent.getHash().substring(0, 7) + "\n"
                        + "Date: " + commitDate.toString() + "\n"
                        + message + "\n"
                        + "\n"
        );
    }

}
