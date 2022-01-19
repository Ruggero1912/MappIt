package it.unipi.dii.inginf.lsmsdb.mapsproject.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import it.unipi.dii.inginf.lsmsdb.mapsproject.file.FileService;
import it.unipi.dii.inginf.lsmsdb.mapsproject.file.LoadFile;
import org.springframework.beans.factory.annotation.Autowired;
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

    private FileService fileService;

    @RequestMapping(path = "/upload", method = POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> upload(@RequestPart("file")MultipartFile file) throws IOException {
        fileService = new FileService();
        String imageObjId = fileService.uploadImage(file);
        if(imageObjId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"Error\":\" unsupported file type\"}");
        return ResponseEntity.status(HttpStatus.OK).body(imageObjId);
    }

    /*@GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String id) throws IOException {
        LoadFile loadFile = fileService.downloadFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(loadFile.getFileType() ))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + loadFile.getFilename() + "\"")
                .body(new ByteArrayResource(loadFile.getFile()));
    }*/
}
