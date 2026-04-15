package sz.lab.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class OssUtils {
    @Resource
    private OSS ossClient;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    public List<String> upload(MultipartFile[] multipartFile) {
        List<String> names = new ArrayList<>(multipartFile.length);
        for (MultipartFile file : multipartFile) {
            String fileName = file.getOriginalFilename();
            String[] split = fileName.split("\\.");
            if (split.length > 1) {
                fileName = split[0] + "_" + System.currentTimeMillis() + "." + split[1];
            } else {
                fileName = fileName + System.currentTimeMillis();
            }
            try (InputStream in = file.getInputStream()) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());
                ossClient.putObject(bucketName, fileName, in, metadata);
            } catch (Exception e) {
                e.printStackTrace();
            }
            names.add(fileName);
        }
        return names;
    }

    public List<String> uploadWithoutTime(MultipartFile[] multipartFile) {
        List<String> names = new ArrayList<>(multipartFile.length);
        for (MultipartFile file : multipartFile) {
            String fileName = file.getOriginalFilename();
            try (InputStream in = file.getInputStream()) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());
                ossClient.putObject(bucketName, fileName, in, metadata);
            } catch (Exception e) {
                e.printStackTrace();
            }
            names.add(fileName);
        }
        return names;
    }

    public ResponseEntity<byte[]> download(String fileName) {
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, fileName);
            try (InputStream in = ossObject.getObjectContent();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                IOUtils.copy(in, out);
                byte[] bytes = out.toByteArray();
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Disposition",
                        "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
                headers.setContentLength(bytes.length);
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setAccessControlExposeHeaders(Arrays.asList("*"));
                return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> listObjectNames(String bucketName) {
        List<String> names = new ArrayList<>();
        ObjectListing listing = ossClient.listObjects(bucketName);
        for (OSSObjectSummary summary : listing.getObjectSummaries()) {
            names.add(summary.getKey());
        }
        return names;
    }

    public boolean removeObject(String bucketName, String objectName) {
        if (ossClient.doesObjectExist(bucketName, objectName)) {
            ossClient.deleteObject(bucketName, objectName);
            return true;
        }
        return false;
    }

    public String createUrl(String fileName) {
        Date expiration = new Date(System.currentTimeMillis() + 5 * 60 * 1000);
        URL url = ossClient.generatePresignedUrl(bucketName, fileName, expiration);
        return url.toString();
    }
}
