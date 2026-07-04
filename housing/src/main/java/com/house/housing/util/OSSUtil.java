package com.house.housing.util;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @version 1.0.0
 * @author Qian
 * @since 1.0.0
 */

// Aliyun OSS官方文档
// https://www.alibabacloud.com/help/zh/oss/user-guide/simple-upload?spm=a2c63.p38356.help-menu-31815.d_0_3_1_0.453949fc8zCcpZ

@Slf4j
@Component
public class OSSUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.region}")
    private String region;

    public static final String MEDIA_FILE_DIR_PREFIX = "media_file/";
    public static final String THUMBNAIL_DIR_PREFIX = "thumbnail/";
    public static final String ANALYSIS_REPORT_DIR_PREFIX = "analysis_report/";

    private static final List<String> IMAGE_EXTS = List.of(
        "jpg","jpeg","png","bmp","gif"
    );

    private static final List<String> VIDEO_EXTS = List.of(
        "mp4","avi","wav","m4a","mov","flv","mkv"
    );

    public String upload(String fileName_, MultipartFile file) {

        log.info("OSS 配置 - endpoint: {}, bucketName: {}", endpoint, bucketName);

        // 填写Object完整路径，完整路径中不能包含Bucket名称，例exampledir/exampleobject.txt如。
        String fileName = MEDIA_FILE_DIR_PREFIX + fileName_;

        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId,accessKeySecret);

        // 创建OSSClient实例。
        // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file.getInputStream());
            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);

            // 上传文件。
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            StringBuilder savedUrlBuilder = new StringBuilder("https://");
            savedUrlBuilder.append(bucketName);
            savedUrlBuilder.append(".");
            savedUrlBuilder.append(endpoint);
            savedUrlBuilder.append("/");
            savedUrlBuilder.append(fileName);
            log.info("文件名:{}", savedUrlBuilder.toString());
            return savedUrlBuilder.toString();

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } catch(IOException ioException) {
            System.out.println("Caught an IOException.");
            System.out.println("Error Message:" + ioException.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return "";
    }

    public String uploadAnalysisReport(String fileName_, MultipartFile file) {

        log.info("OSS 配置 - endpoint: {}, bucketName: {}", endpoint, bucketName);

        // 填写Object完整路径，完整路径中不能包含Bucket名称，例exampledir/exampleobject.txt如。
        String fileName = ANALYSIS_REPORT_DIR_PREFIX + fileName_;

        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId,accessKeySecret);

        // 创建OSSClient实例。
        // 当OSSClient实例不再使用时，调用shutdown方法以释放资源。
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, file.getInputStream());
            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);

            // 上传文件。
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            StringBuilder savedUrlBuilder = new StringBuilder("https://");
            savedUrlBuilder.append(bucketName);
            savedUrlBuilder.append(".");
            savedUrlBuilder.append(endpoint);
            savedUrlBuilder.append("/");
            savedUrlBuilder.append(fileName);
            log.info("文件名:{}", savedUrlBuilder.toString());
            return savedUrlBuilder.toString();

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } catch(IOException ioException) {
            System.out.println("Caught an IOException.");
            System.out.println("Error Message:" + ioException.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return "";
    }

    public void deleteFile(String fileName) {
        // 创建 OSSClient 实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 删除指定文件
            ossClient.deleteObject(bucketName, fileName);
            System.out.println("文件删除成功: " + fileName);
        } catch (Exception e) {
            System.err.println("文件删除失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭 OSSClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


    public String generateFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String randomUUID = UUID.randomUUID().toString().replace("-","");
        String fileExt = originalFileName.substring(originalFileName.lastIndexOf("."));
        return randomUUID + fileExt;
    }

    public static String getFileType(String fileExt) {
        if(IMAGE_EXTS.contains(fileExt)) {
            return "image";
        } else if(VIDEO_EXTS.contains(fileExt)) {
            return "video";
        }
        throw new IllegalArgumentException("不支持的文件类型: " + fileExt);
    }

}
