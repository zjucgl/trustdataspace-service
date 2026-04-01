package sz.lab;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;

@Controller
@ServletComponentScan
@MapperScan({"sz.lab.mapper"})
@EnableScheduling
@SpringBootApplication
public class TrustedDataSpaceBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrustedDataSpaceBackEndApplication.class, args);
    }

}
