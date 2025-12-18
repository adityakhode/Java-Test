package com.AduProjects.FileMover.controller;


import com.AduProjects.FileMover.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;


@RestController
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    private static SseEmitter sseEmitter;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file")MultipartFile file) throws IOException{
        sseEmitter = new SseEmitter();
        s3Service.uploadFile(file, sseEmitter);
        return ResponseEntity.ok("File Uploaded Successfully");
    }

    @GetMapping("/progress")
    public SseEmitter progress() {
        if (sseEmitter == null) {
            sseEmitter = new SseEmitter();
        }
        return sseEmitter;
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> download(@PathVariable String filename){
        byte[] data = s3Service.downloadFile(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachments value")
                .body(data);
    }
}
