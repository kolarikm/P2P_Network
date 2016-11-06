/**
 * Created by ben on 11/6/16.
 */
public class FileSearchDTO {
    private String fileName;
    private String networkSpeed;
    private String userName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getNetworkSpeed(){
        return networkSpeed;
    }

    public void setNetworkSpeed(String networkSpeed){
        this.networkSpeed = networkSpeed;
    }

    public String getUserName(){
        return this.userName;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public String toString(){
        return "FileName: "+ fileName+ " NetworkSpeed: " + networkSpeed +" UserName: "+ userName;
    }
}
