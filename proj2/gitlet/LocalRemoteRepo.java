package gitlet;

import java.io.File;

public class LocalRemoteRepo extends RepositoryBase{

    public LocalRemoteRepo(File CWD) {
        super();
        assert CWD != null;
        super.CWD = CWD;
        super.GITLET_DIR = Utils.join(CWD, ".gitlet");
        super.GITLET_FILE = Utils.join(GITLET_DIR, ".gitlet");
        super.BLOB_DIR = Utils.join(GITLET_DIR, "blobs");
        if (GITLET_FILE.exists()) {loadState();}
    }

    public void test(){
        System.out.println(CWD);
    }

    @Override
    void fetchCommitWithBlobs(Commit c){
        c.CWD = CWD;
        super.getRemoteBlobs(c);
    }
}
