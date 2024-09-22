package gitlet;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class RemoveBlob extends Blob{
    public RemoveBlob(File f){
        super(-1, f, "removed".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void save(){}

    @Override
    public byte[] getContents(){
        return new byte[0];
    }

    @Override
    public void recoverFile(){
        if (file.exists()) Utils.restrictedDelete(file);
    }
}
