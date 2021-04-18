package com.mupol.mupolserver.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.mupol.mupolserver.domain.common.MediaType;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@NoArgsConstructor
public class S3Service {
    private AmazonS3 s3Client;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);

        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(this.region)
                .build();
    }

    public String uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        String fileName = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(fileName);
        String filePath = "img/" + userId + "/profile." + extension;
        log.info(filePath + " uploaded");

        s3Client.putObject(new PutObjectRequest(bucket, filePath, file.getInputStream(), null)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return s3Client.getUrl(bucket, filePath).toString();
    }

    public String uploadMediaFolder(File folder, Long userId, Long mediaId, MediaType mediaType) throws IOException {
        String fileUrl = "";
        String fileExtension;

        for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            fileExtension = StringUtils.getFilenameExtension(fileEntry.getName());
            assert fileExtension != null;

            if (fileExtension.equals("ts") || fileExtension.equals("m3u8")) {
                uploadMedia(fileEntry, userId, mediaId, mediaType);
                if (fileExtension.equals("m3u8")) {
                    fileUrl = cloudFrontDomain + "/" + userId + "/" + mediaId + "/" + "s.m3u8";
                }
            }
        }

        return fileUrl;
    }

    public void uploadMedia(File file, Long userId, Long soundId, MediaType mediaType) throws IOException {
        String filePath = "";

        if (mediaType == MediaType.Video) {
            filePath = "video/" + userId + "/" + soundId.toString() + "/" + file.getName();
        } else if (mediaType == MediaType.Sound) {
            filePath = "sound/" + userId + "/" + soundId.toString() + "/" + file.getName();
        }

        log.info(filePath + " uploaded");

        s3Client.putObject(new PutObjectRequest(bucket, filePath, new FileInputStream(file), null)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public void deleteMedia(Long userId, Long mediaId, MediaType mediaType) {
        String filePath = "";
        if(mediaType == MediaType.Video) {
            filePath = "video/" + userId + "/" + mediaId.toString() + "/";
        } else if (mediaType == MediaType.Sound) {
            filePath = "sound/" + userId + "/" + mediaId.toString() + "/";
        }

        ObjectListing objectList = s3Client.listObjects(bucket, filePath);
        List<S3ObjectSummary> objectSummeryList = objectList.getObjectSummaries();
        String[] keysList = new String[objectSummeryList.size()];
        int count = 0;
        for (S3ObjectSummary summery : objectSummeryList) {
            keysList[count++] = summery.getKey();
        }
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket).withKeys(keysList);
        s3Client.deleteObjects(deleteObjectsRequest);
        log.info(filePath + " removed");
    }
}
