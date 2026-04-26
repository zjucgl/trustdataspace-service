package sz.lab.config.cfg;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    @Bean(name = "ossPublicSigningClient")
    public OSS ossPublicSigningClient() {
        String publicEndpoint = endpoint.replace("-internal.aliyuncs.com", ".aliyuncs.com");
        return new OSSClientBuilder().build(publicEndpoint, accessKeyId, accessKeySecret);
    }
}
