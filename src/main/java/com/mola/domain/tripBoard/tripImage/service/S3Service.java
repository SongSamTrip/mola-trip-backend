package com.mola.domain.tripBoard.tripImage.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.mola.global.exception.CustomException;
import com.mola.global.exception.GlobalErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service implements ImageService{

    private final AmazonS3 amazonS3;

    private final String bucket;

    public S3Service(AmazonS3 amazonS3, @Value("${cloud.aws.s3.bucket}")String bucket) {
        this.amazonS3 = amazonS3;
        this.bucket = bucket;
    }

    @Transactional
    public String upload(MultipartFile multipartFile) {
        String s3FileName = UUID.randomUUID().toString() + "-"  + multipartFile.getOriginalFilename();

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try {
            amazonS3.putObject(bucket, s3FileName, multipartFile.getInputStream(), objectMetadata);
        } catch (IOException e) {
            throw new CustomException(GlobalErrorCode.InvalidImageType);
        }

        return amazonS3.getUrl(bucket, s3FileName).toString();
    }

    @Override
    public void delete(String fileName) {
        amazonS3.deleteObject(bucket, fileName);
    }
}
