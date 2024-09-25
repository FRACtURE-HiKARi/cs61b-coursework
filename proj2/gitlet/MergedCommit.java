package gitlet;

import java.io.File;
import java.util.Collection;
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
        super(message, author, commitDate, mergedParent, branch, CWD);
        files.putAll(directParent.files);
        this.parent = this.directParent = directParent;
        this.mergedParent = mergedParent;
        if (directParent.height < mergedParent.height) {
            this.height = mergedParent.height + 1;
        }
    }

    @Override
    public void printCommit() {
        System.out.print(
                "===\n"
                        + "commit " + getHash() + "\n"
                        + "Merge: " + directParent.getHash().substring(0, 7) + " "
                        + mergedParent.getHash().substring(0, 7) + "\n"
                        + "Date: " + dateFormat.format(commitDate) + "\n"
                        + message + "\n"
                        + "\n"
        );
    }

    @Override
    public Collection<Commit> getAllParents(){
        Collection<Commit> c = super.getAllParents();
        c.add(mergedParent);
        return c;
    }
}
