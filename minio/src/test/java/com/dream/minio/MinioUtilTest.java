package com.dream.minio;

import com.dream.minio.util.MinioUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Slf4j
@SpringBootTest
public class MinioUtilTest {

    @Resource
    private MinioUtils minioUtils;

    @Test
    @SneakyThrows
    void testUploadFile(){
        File file = new File("src/main/resources/application.yml");

        FileInputStream fileInputStream = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(file.getName(),file.getName(),"text/plain",fileInputStream);

        String path = minioUtils.uploadFile(multipartFile);
        log.info("文件路径：{}",path);
    }

    @Test
    void testGetObject(){
        InputStream inputStream = minioUtils.getObject("demo","2024/04/20/d276b40be346405c80cbba744e7578f1.yml");
    }

    @Test
    void testGetTemporaryUrl(){
        String url = minioUtils.getTemporaryUrl("demo","psb.jpeg",60);
        log.info("临时链接{}",url);
    }

}
