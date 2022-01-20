package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.imageFile.ImageFile;
import it.unipi.dii.inginf.lsmsdb.mapsproject.imageFile.ImageFileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    /*
    @RequestMapping(path = "/upload", method = POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> upload(@RequestPart("file")MultipartFile file) throws IOException {
        ImageFileService imageFileService = new ImageFileService();
        String imageObjId = imageFileService.uploadImage(file);
        if(imageObjId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Error\":\" unsupported file type\"}");
        return ResponseEntity.status(HttpStatus.OK).body(imageObjId);
    }*/

    @GetMapping("/api/files/images/{id}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String id) throws IOException {
        ImageFileService imageFileService = new ImageFileService();
        ImageFile imageFile = imageFileService.downloadImage(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imageFile.getFileType() ))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageFile.getFilename() + "\"")
                .body(new ByteArrayResource(imageFile.getFile()));
    }
}
