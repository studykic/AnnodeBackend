package com.ikchi.annode.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.ikchi.annode.Enum.FileExceptionMessage;
import com.ikchi.annode.Enum.ServiceEnum.FileUploadServiceEnum;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private AmazonS3 amazonS3;

    @PostConstruct
    private void initializeAmazon() {
        this.amazonS3 = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(this.accessKey, this.secretKey)))
            .withRegion(this.region)
            .build();
    }

    public List<String> storeFiles(List<MultipartFile> profileImageFiles, String userIdentifier) {
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile profileImageFile : profileImageFiles) {
            try {
                // 파일 본래이름과 확장자를 변수로 선언 (확장자는 jpg 또는 gif만 허용)
                String originalFilename = profileImageFile.getOriginalFilename();
                String fileExtension = getFileExtension(originalFilename);

                fileExtensionCheck(fileExtension);

                // 파일 이름을 유저를 상위 디렉토리로 한뒤 현재 시간 + UUID + 파일확장자로 설정함
                String fileName = uploadDir + "/" + userIdentifier + "/"
                    + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern(FileUploadServiceEnum.getDateTimeFormatPattern()))
                    + UUID.randomUUID().toString().substring(0, 8)
                    + fileExtension;

                // 저장소에 파일 저장
                amazonS3.putObject(bucketName, fileName, profileImageFile.getInputStream(),
                    new ObjectMetadata());

                // 파일의 S3 URL을 얻음
                String fileUrl = amazonS3.getUrl(bucketName, fileName).toString();

                fileUrls.add(fileUrl);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException(
                    FileExceptionMessage.IMAGE_UPLOAD_ERROR.getMessage());
            }
        }

        return fileUrls;
    }


    private void fileExtensionCheck(String fileExtension) {
        List<String> allowedFileExtensions = FileUploadServiceEnum.getAllowedFileExtensions();

        if (!allowedFileExtensions.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException(
                FileExceptionMessage.UNSUPPORTED_FILE_FORMAT.getMessage());
        }
    }

    // 파일 확장자를 가져오는 메소드
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex == -1) ? "" : "." + fileName.substring(dotIndex + 1);
    }


    public void deleteImgFile(List<String> fileUrlList) {
        try {
            for (String fileUrl : fileUrlList) {

                AmazonS3URI s3Uri = new AmazonS3URI(fileUrl);

                amazonS3.deleteObject(s3Uri.getBucket(), s3Uri.getKey());

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(FileExceptionMessage.IMAGE_DELETE_ERROR.getMessage());
        }
    }


}
