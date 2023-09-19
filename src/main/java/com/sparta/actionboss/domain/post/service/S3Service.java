package com.sparta.actionboss.domain.post.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.sparta.actionboss.global.exception.CommonException;
import com.sparta.actionboss.global.exception.errorcode.ClientErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String s3Bucket;

    public List<String> upload(
            List<MultipartFile> images,
            String folderName
    ) throws IOException {
        List<String> fileNameList = new ArrayList<>();
        for (MultipartFile image : images) {
            String fileName = upload(image, folderName);
            fileNameList.add(fileName);
        }
        return fileNameList;
    }

    public String upload(
            MultipartFile multipartFile,
            String folderName
    ) throws IOException {
        if(!multipartFile.getContentType().startsWith("image")) {
            throw new CommonException(ClientErrorCode.ONLY_PERMIT_IMAGE);
        }
        String fileName = UUID.randomUUID().toString()
                .substring(19) + "-" + multipartFile.getOriginalFilename();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getInputStream().available());
        amazonS3Client.putObject(
                new PutObjectRequest(s3Bucket, "images/" + folderName + "/"
                        + fileName, multipartFile.getInputStream(), objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead)
        );

        return fileName;
    }


    public void deleteFolder(String requestFolderName) {
        ObjectListing objectListing = amazonS3Client
                .listObjects(s3Bucket, "images/" + requestFolderName);
        List<DeleteObjectsRequest.KeyVersion> keyVersions = new ArrayList<>();
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            keyVersions.add(new DeleteObjectsRequest.KeyVersion(objectSummary.getKey()));
        }
        if (!keyVersions.isEmpty()) {
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(s3Bucket).withKeys(keyVersions);
            amazonS3Client.deleteObjects(deleteObjectsRequest);
        }
    }
}
