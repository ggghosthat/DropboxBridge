import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;


//This class represents user authentication process to Dropbox
public class Authenticator {
    //user credentials
    private String clientIdentifier;
    private String ACCESS_TOKEN;

    //Dropbox authentication structures
    private DbxRequestConfig dropboxConfig;
    private DbxClientV2 clientV2;
    private FullAccount account;

    //param: clientIdentifier - user dropbox application name
    //param: access_token - user access token
    public Authenticator(String clientIdentifier, String access_token){
        this.clientIdentifier = clientIdentifier;
        this.ACCESS_TOKEN = access_token;
    }

    //This method try to authenticate user.
    // In right case it return a user full account.
    //In other case it return null-value
    public FullAccount Authenticate() {
        try {
            dropboxConfig = new DbxRequestConfig(this.clientIdentifier);
            clientV2 = new DbxClientV2(this.dropboxConfig, this.ACCESS_TOKEN);
            this.account = clientV2.users().getCurrentAccount();
            System.out.println("[AUTHENTICATED] - Account " + this.account.getName() + " have benn successfully authenticated.");
            return this.account;
        }
        catch (DbxException dbxException) {
            dbxException.printStackTrace();
        }
        catch(Exception exception){
            exception.printStackTrace();
        }

        return null;
    }
}
