package it.unipi.dii.inginf.lsmsdb.mapsproject.imageFile;

import it.unipi.dii.inginf.lsmsdb.mapsproject.config.PropertyPicker;
import org.bson.types.ObjectId;

public class ImageFile {
    private String id;
    private String filename;
    private String fileType;
    private String fileSize;
    private byte[] file;

    private static final String SERVER_DOMAIN_NAME       = PropertyPicker.getProperty("global.server.domain.name");
    private static final String SERVER_GET_FILE_ENDPOINT = "/api/files/images/";

    public ImageFile(ObjectId id) {
        this.id = id.toString();
    }

    public ImageFile(String id){
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getGlobalURI(){
        return SERVER_DOMAIN_NAME + SERVER_GET_FILE_ENDPOINT + id;
    }
    public String getResourceURI(){
        return SERVER_GET_FILE_ENDPOINT + id;
    }

    public static String getResourceURIFromId(String id){
        return SERVER_GET_FILE_ENDPOINT + id;
    }
    public static boolean isServerImageId(String pic){
        if(ObjectId.isValid(pic)){
            return true;
        }
        else{
            return false;
        }
    }
}
