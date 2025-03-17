package com.example.S3Images;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {
    @Autowired
    private AmazonS3 s3Client;

    private String bucketName = "my-file-bucket-for-project";

    public Image uploadImage(MultipartFile file) throws IOException {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);

        String url = s3Client.getUrl(bucketName, fileName).toString();

        return new Image(
                UUID.randomUUID().toString(),
                fileName,
                file.getContentType(),
                file.getSize(),
                url,
                LocalDateTime.now()
        );
    }

    public List<Image> listImages(int page, int size) {
        int startIndex = page * size;
        List<Image> images = new ArrayList<>();

        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withMaxKeys(size);

        if (startIndex > 0) {
            // For pagination, we need to get all objects up to the starting index
            // This is a limitation of S3 API, it doesn't support true pagination
            ListObjectsV2Result firstResult = s3Client.listObjectsV2(request);
            if (firstResult.getObjectSummaries().size() >= startIndex) {
                request.setContinuationToken(firstResult.getNextContinuationToken());
            } else {
                return images; // Not enough objects
            }
        }

        ListObjectsV2Result result = s3Client.listObjectsV2(request);
        for (S3ObjectSummary summary : result.getObjectSummaries()) {
            String url = s3Client.getUrl(bucketName, summary.getKey()).toString();

            Image image = new Image(
                    UUID.randomUUID().toString(),
                    summary.getKey(),
                    s3Client.getObjectMetadata(bucketName, summary.getKey()).getContentType(),
                    summary.getSize(),
                    url,
                    LocalDateTime.ofInstant(summary.getLastModified().toInstant(), java.time.ZoneId.systemDefault())
            );

            images.add(image);
        }

        return images;
    }

    public int getTotalImages() {
        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName);
        ListObjectsV2Result result = s3Client.listObjectsV2(request);
        return result.getKeyCount();
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + extension;
    }
}
