package it.unipi.dii.inginf.lsmsdb.mapsproject.file;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import it.unipi.dii.inginf.lsmsdb.mapsproject.persistence.connection.MongoConnection;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileService {

    private static final Logger LOGGER = Logger.getLogger( FileService.class.getName() );


    private GridFSBucket gridFSBucket;
    private static final String[] allowedTypes = {"image/jpeg", "image/gif", "image/png"};

    public FileService(){
        gridFSBucket = GridFSBuckets.create(MongoConnection.getDatabase());
    }


    public String uploadImage(MultipartFile upload) throws IOException {

        if(!Arrays.asList(allowedTypes).contains(upload.getContentType())){
            LOGGER.log(Level.SEVERE, "Error: image file type not supported, please upload [jpeg / png / gif]");
            return null;
        }

        //define additional metadata
        DBObject metadata = new BasicDBObject();
        metadata.put("fileSize", upload.getSize());

        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(1048576)
                .metadata(new Document("type", upload.getContentType()));

        //store in database which returns the objectID
        ObjectId fileId = gridFSBucket.uploadFromStream(upload.getOriginalFilename(), upload.getInputStream(), options);

        //return as a string
        return fileId.toString();
    }

    /*public LoadFile downloadImage(String id) throws IOException {

        //search file
        GridFSFile gridFSFile = template.findOne( new Query(Criteria.where("_id").is(id)) );

        //convert uri to byteArray
        //save data to LoadFile class
        LoadFile loadFile = new LoadFile();

        if (gridFSFile != null && gridFSFile.getMetadata() != null) {
            loadFile.setFilename( gridFSFile.getFilename() );

            loadFile.setFileType( gridFSFile.getMetadata().get("_contentType").toString() );

            loadFile.setFileSize( gridFSFile.getMetadata().get("fileSize").toString() );

            loadFile.setFile( IOUtils.toByteArray(operations.getResource(gridFSFile).getInputStream()) );
        }

        return loadFile;
    }*/
}
