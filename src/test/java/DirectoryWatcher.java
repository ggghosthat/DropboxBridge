import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

public class DirectoryWatcher {
    //user-client & FileRequest structures
    private DbxClientV2 client;
    private DbxUserFilesRequests filesRequests;


    public DirectoryWatcher(DbxClientV2 client){
        this.client = client;
        this.filesRequests = client.files();
    }


    //These both methods implement fetching directory from dropbox system
    public void FetchRootDir() throws DbxException {
        ListFolderResult result = filesRequests.listFolder("");
        IterateDir(result);
    }

    //param: path - path to a special folder
    public void FetchListDir(String path) throws DbxException {
        ListFolderResult result = filesRequests.listFolder(path);
        IterateDir(result);
    }

    //Iterates directory
    private void IterateDir(ListFolderResult result) throws DbxException {
        char space = '\t';
        int space_count = 1;
        while (true){
            int indention = (space * space_count);

            for (Metadata metadata : result.getEntries()){
                System.out.println(indention + "[METADATA] - " + metadata.getName() + "\n" + indention + '\t' + metadata.getPathLower());
            }

            if(!result.getHasMore())
                break;

            result = filesRequests.listFolderContinue(result.getCursor());
            space_count++;
        }
    }
}
