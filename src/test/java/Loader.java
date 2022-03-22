import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Loader {
    //client to define load-staff
    private DbxClientV2 client;

    //Downloader class presents download operation
    private DbxDownloader<FileMetadata> downloader;

    //FIle input&output streams pretend loading operations
    private FileInputStream inputFileStream;
    private FileOutputStream outputFileStream;

    public Loader(DbxClientV2 client){
        this.client = client;
    }

    public void Upload(String dbPath, String uploadPath){
        try{
            File file = new File(uploadPath);
            if(file.exists()) {
                String name = file.getName();
                inputFileStream = new FileInputStream(uploadPath);
                FileMetadata metadata = client.files().uploadBuilder(dbPath + name).uploadAndFinish(inputFileStream);
                System.out.println("[SUCCESS] - Uploaded successfully !");
            }
            else {
                System.out.println("[ERROR] - Could not find entered upload file. Try again");
            }
        }
        catch(DbxException dbxException){
            dbxException.printStackTrace();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void Download(String dbPath, String outPath){
        try{
            downloader = client.files().download(dbPath);
            outputFileStream = new FileOutputStream(outPath);
            downloader.download(outputFileStream);
            outputFileStream.close();
            System.out.println("[SUCCESS] - Downloaded successfully !");
        }
        catch(DbxException dbxException){
            System.out.println("[ERROR-DROPBOX] - " + dbxException.getMessage());
        }
        catch(Exception ex){
            System.out.println("[ERROR] - " + ex.getMessage());
        }
    }
}
