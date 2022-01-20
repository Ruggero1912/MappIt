package it.unipi.dii.inginf.lsmsdb.mapsproject.imageFile;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageFileService {

    private static final Logger LOGGER = Logger.getLogger( ImageFileService.class.getName() );


    private GridFSBucket gridFSBucket;
    private static final String[] allowedTypes = {"image/jpeg", "image/gif", "image/png"};

    public ImageFileService(){
        gridFSBucket = GridFSBuckets.create(MongoConnection.getDatabase());
    }


    public String uploadImage(MultipartFile upload) throws IOException {

        if(!Arrays.asList(allowedTypes).contains(upload.getContentType())){
            LOGGER.log(Level.SEVERE, "Error: image file type not supported, please upload [jpeg / png / gif]");
            return null;
        }

        //define additional metadata
        Document metadata = new Document("_contentType", upload.getContentType());
                 metadata.put("fileSize", upload.getSize());

        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(1048576)
                .metadata(metadata);

        //stores in database and gets the objectID
        ObjectId fileId = gridFSBucket.uploadFromStream(upload.getOriginalFilename(), upload.getInputStream(), options);

        //return as a string
        return fileId.toString();
    }

    public ImageFile downloadImage(String id) throws IOException {

        ObjectId fileObjId;
        try{
            fileObjId = new ObjectId(id);
        }catch (Exception e){
            LOGGER.log(Level.SEVERE, e.getMessage());
            return null;
        }

        GridFSFindIterable g = gridFSBucket.find( new Document("_id", fileObjId) );
        GridFSFile gFile = g.first();

        //in this case the file with the specified id was not found
        if(gFile == null){
            return null;
        }

        //save data to LoadFile class
        ImageFile imageFile = new ImageFile(id);

        if (gFile != null && gFile.getMetadata() != null) {
            imageFile.setFilename( gFile.getFilename() );

            imageFile.setFileType( gFile.getMetadata().get("_contentType").toString() );

            imageFile.setFileSize( gFile.getMetadata().get("fileSize").toString() );

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            gridFSBucket.downloadToStream(fileObjId, bs);
            imageFile.setFile( bs.toByteArray() );
        }

        return imageFile;
    }
}
