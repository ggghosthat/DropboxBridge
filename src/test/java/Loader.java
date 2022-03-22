import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.util.IOUtil;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.util.IOUtil.ProgressListener;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class Loader {
    //client to define load-staff
    private DbxClientV2 client;

    //Downloader class presents download operation
    private DbxDownloader<FileMetadata> downloader;

    //FIle input&output streams pretend loading operations
    private FileInputStream inputFileStream;
    private FileOutputStream outputFileStream;



    //No-tested. Will not used on stable branch
    private long CHUNK_SIZE = 8L << 20; // 8MiB
    private ProgressListener progressListener;
    private UploadSessionCursor cursor;
    private CommitInfo commitInfo;


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


    //Chunked-uploading did not test. No-tested. Will not used on stable branch
    public void ChunkedUpload(String dbPath, String uploadPath, int attempts){
        try{
            File file = new File(uploadPath);
            if(file.exists()) {
                long size = file.length();
                long uploaded = 0L;

                DbxException thrownDBException;

                progressListener = new ProgressListener() {
                    @Override
                    public void onProgress(long l) {

                    }
                };

                ChunkedProcess(dbPath, attempts, file, size, uploaded);
            }
            else {
                System.out.println("[ERROR] - Could not find entered upload file. Try again");
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void ChunkedProcess(String dbPath, int attempts, File file, long size, long uploaded) throws IOException, DbxException {
        String sessionId = null;

        for(int i =0; i < attempts;++i){
            if (i > 0) {
                System.out.printf("[INFO] - Retrying chunked upload (%d / %d attempts)\n", i + 1, attempts);
            }

            inputFileStream = new FileInputStream(file);
            inputFileStream.skip(uploaded);

            //1) Start
            if (sessionId == null){
                sessionId = client.files().uploadSessionStart()
                        .uploadAndFinish(inputFileStream,CHUNK_SIZE, progressListener)
                        .getSessionId();
                uploaded += CHUNK_SIZE;
            }

            cursor = new UploadSessionCursor(sessionId, uploaded);

            //2) Append
            while ((size - uploaded) > CHUNK_SIZE) {
                client.files().uploadSessionAppendV2(cursor)
                        .uploadAndFinish(inputFileStream, CHUNK_SIZE, progressListener);
                uploaded += CHUNK_SIZE;
                cursor = new UploadSessionCursor(sessionId, uploaded);
            }

            //3) Finish
            long remaining = size - uploaded;
            commitInfo = CommitInfo.newBuilder(dbPath)
                                .withMode(WriteMode.ADD)
                    .withClientModified(new Date(file.lastModified()))
                    .build();
            FileMetadata metadata = client.files().uploadSessionFinish(cursor, commitInfo)
                    .uploadAndFinish(inputFileStream, remaining, progressListener);

            break;
        }

        System.out.println("[SUCCESS] - Uploaded successfully !");
    }
}
