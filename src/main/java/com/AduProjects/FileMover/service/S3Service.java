package com.AduProjects.FileMover.service;

import java.io.File;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.IOException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    public void uploadFile(MultipartFile file, SseEmitter emitter) throws IOException {

        long totalSize = file.getSize();

        InputStream originalStream = file.getInputStream();
        InputStream progressStream = new ProgressTrackingInputStream(
                originalStream,
                uploadedBytes -> {
                    int progress = (int) ((uploadedBytes * 100) / totalSize);
                    try {
                        emitter.send("Uploaded: " + progress + "%");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );

        RequestBody requestBody = RequestBody.fromInputStream(
                progressStream,
                totalSize
        );

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(file.getOriginalFilename())
                        .build(),
                requestBody
        );

        try {
            emitter.send("Upload complete!");
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    public byte[] downloadFile(String key) {
        ResponseBytes<GetObjectResponse> objectAsByte = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );

        return objectAsByte.asByteArray();
    }

    public void  uploadFileLocal(MultipartFile file) throws IOException{
        // LOCAL DIRECTORY PATH
        String localPath = "/home/adix/Documents/FileMover/myFiles/";

        // Create folder if it does not exist
        File directory = new File(localPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Create full file path
        File localFile = new File(localPath + file.getOriginalFilename());

        // Save multipart file to local path
        file.transferTo(localFile);

        System.out.println("File saved to: " + localFile.getAbsolutePath());
    }

}
