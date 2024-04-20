package com.dream.minio.util;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.UUID;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MinioUtils {

    @Value("${minio.defaultBucket}")
    private String defaultBucket;

    @Resource
    private MinioClient minioClient;

    /**
     * 文件上传
     * @param file 文件
     * @param bucketName 桶名称
     */
    @SneakyThrows
    public String uploadFile(MultipartFile file, String bucketName) {
        return uploadFile(bucketName,file.getOriginalFilename(), file.getInputStream(), file.getContentType());
    }

    /**
     * 文件上传到默认桶
     * @param file 文件
     */
    @SneakyThrows
    public String uploadFile(MultipartFile file) {
        return uploadFile(file,defaultBucket);
    }

    /**
     * 流文件上传到默认桶
     * @param fileName 文件名
     * @param stream 文件流
     * @param contentType 文件类型
     */
    public String uploadFile(String fileName, InputStream stream, String contentType) {
        return uploadFile(defaultBucket,fileName,stream,contentType);
    }

    /**
     * 流文件上传
     * @param bucketName 桶名称
     * @param fileName 文件名
     * @param stream 文件流
     * @param contentType 文件类型
     */
    public String uploadFile(String bucketName, String fileName, InputStream stream, String contentType) {
        if(StringUtils.hasText(bucketName)){
            bucketName = defaultBucket;
        }

        if(!bucketExists(bucketName)){
            return null;
        }
//        文件后缀
        String extName = Objects.requireNonNull(FileNameUtil.extName(fileName)).toLowerCase();
//        文件存放路径
        String filePath = new SimpleDateFormat("yyyy/MM/dd/").format(new Date());
//        UUID
        String fileUUid = UUID.randomUUID().toString().replaceAll("-", "");
//        新定义的文件名
        String objectName = filePath + fileUUid + "." + extName;
//        上传文件
        putObject(bucketName, objectName, stream,contentType);
        return objectName;
    }

    /**
     * 文件上传
     *
     * @param bucketName    桶名称
     * @param multipartFile 文件
     * @param filename      文件名
     */
    @SneakyThrows
    public void putObject(String bucketName, String filename, MultipartFile multipartFile) {
        InputStream inputStream = multipartFile.getInputStream();
        putObject(bucketName, filename, inputStream, multipartFile.getContentType());
        inputStream.close();
    }

    /**
     * 流文件上传
     *
     * @param bucketName  桶名称
     * @param filename    文件名
     * @param inputStream 流
     * @param contentType 文件类型
     */
    @SneakyThrows
    public void putObject(String bucketName, String filename, InputStream inputStream, String contentType) {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .stream(inputStream, inputStream.available(), -1)
                        .contentType(contentType)
                        .build());
        inputStream.close();
    }

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 桶名称
     */
    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
        return minioClient.bucketExists(bucketExistsArgs);
    }


    /**
     * 获取所有桶名称
     */
    public List<String> listBucketNames() {
        return listBuckets().stream().map(Bucket::name).collect(Collectors.toList());
    }

    /**
     * 获取所有桶
     */
    @SneakyThrows
    public List<Bucket> listBuckets() {
        return minioClient.listBuckets();
    }

    /**
     * 列出存储桶中的所有对象
     */
    @SneakyThrows
    public Iterable<Result<Item>> listObjects(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            return minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .recursive(true)
                    .build());
        }
        return null;
    }

    /**
     * 列出存储桶中的所有对象名称
     */
    @SneakyThrows
    public List<String> listObjectNames(String bucketName) {
        List<String> listObjectNames = new ArrayList<>();
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<Result<Item>> myObjects = listObjects(bucketName);
            for (Result<Item> result : myObjects) {
                Item item = result.get();
                listObjectNames.add(item.objectName());
            }
        }
        return listObjectNames;
    }

    /**
     * 清空并删除桶
     */
    @SneakyThrows
    public boolean clearAndRemoveBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            List<String> list = listObjectNames(bucketName);
            removeObject(bucketName,list);
            return removeBucket(bucketName);
        }
        return false;
    }

    /**
     * 删除存储桶
     */
    @SneakyThrows
    public boolean removeBucket(String bucketName) {
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<Result<Item>> myObjects = listObjects(bucketName);
            for (Result<Item> result : myObjects) {
                Item item = result.get();
                // 有对象文件，则删除失败
                if (item.size() > 0) {
                    return false;
                }
            }
            // 删除存储桶，注意，只有存储桶为空时才能删除成功。
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            flag = bucketExists(bucketName);
            return !flag;
        }
        return false;
    }

    /**
     * 以流的形式获取一个文件对象
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     */
    @SneakyThrows
    public  InputStream getObject(String bucketName, String objectName) {
        if (bucketExists(bucketName)) {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
        }
        return null;
    }

    /**
     * 以流的形式获取一个文件对象（断点下载）
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     * @param offset     起始字节的位置
     * @param length     要读取的长度 (可选，如果无值则代表读到文件结尾)
     * @return
     */
    @SneakyThrows
    public InputStream getObject(String bucketName, String objectName, long offset, Long length) {
        if (bucketExists(bucketName)) {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).offset(offset).length(length).build());
        }
        return null;
    }

    /**
     * 删除一个对象
     *
     * @param bucketName 存储桶名称
     * @param objectName 存储桶里的对象名称
     */
    @SneakyThrows
    public String removeObject(String bucketName, String objectName) {
        List<String> objectNames = new ArrayList<>();
        objectNames.add(objectName);
        List<String> list = removeObject(bucketName,objectNames);
        if(list.isEmpty()){
            return null;
        }else{
            return list.get(0);
        }
    }

    /**
     * 删除指定桶的多个文件对象,返回删除错误的对象列表，全部删除成功，返回空列表
     *
     * @param bucketName  存储桶名称
     * @param objectNames 含有要删除的多个object名称的迭代器对象
     */
    @SneakyThrows
    public List<String> removeObject(String bucketName, List<String> objectNames) {
        List<DeleteObject> objects = new LinkedList<>();
        objectNames.forEach(object->{
            objects.add(new DeleteObject(object));
        });
        List<String> deleteErrorNames = new ArrayList<>();
        boolean flag = bucketExists(bucketName);
        if (flag) {
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(objects).build());
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                deleteErrorNames.add(error.objectName());
            }
        }
        return deleteErrorNames;
    }

    /**
     * 获取存储桶策略
     *
     * @param bucketName 存储桶名称
     */
    @SneakyThrows
    public String getBucketPolicy(String bucketName){
        return minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket(bucketName).build());
    }

    /**
     * 生成文件的临时链接，需要将桶设置为private，或者custom并设置存读策略，当策略为public时候，临时链接无效，始终可以访问，没有时间限制
     * @param bucketName 桶名称
     * @param objectName 文件名称
     * @param time 链接有效时长
     */
    @SneakyThrows
    public String getTemporaryUrl(String bucketName,String objectName,int time){
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .expiry(time)
                        .object(objectName)
                        .method(Method.GET)
                .build());
    }

}
