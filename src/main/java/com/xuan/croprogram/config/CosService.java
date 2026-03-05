package com.xuan.croprogram.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class CosService {

    // 🚨 核心机密：去腾讯云控制台的 "访问管理(CAM)" -> "API密钥管理" 里获取！
    // 千万不要发给我，也不要提交到公共的 Github！
    // 坚决不上传真实秘钥！
    @Value("${tencent.cos.secret-id}")
    private String SECRET_ID;

    @Value("${tencent.cos.secret-key}")
    private String SECRET_KEY;

    // 我已经根据你的截图帮你配好了！
    private static final String REGION_NAME = "ap-chengdu";
    private static final String BUCKET_NAME = "xiaoxuan-1395427682";

    /**
     * 上传图片并返回访问链接
     */
    public String uploadImage(MultipartFile file) {
        // 1. 初始化用户身份信息 (secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(SECRET_ID, SECRET_KEY);
        // 2. 设置 bucket 的地域
        Region region = new Region(REGION_NAME);
        ClientConfig clientConfig = new ClientConfig(region);
        // 3. 生成 cos 客户端
        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            InputStream inputStream = file.getInputStream();
            // 获取原文件名并提取后缀 (比如 .jpg, .png)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";

            // 重新生成一个不重复的文件名，存放在 images/ 目录下
            String key = "images/" + UUID.randomUUID().toString().replaceAll("-", "") + extension;

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            // 4. 发起上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, key, inputStream, objectMetadata);
            cosClient.putObject(putObjectRequest);

            // 5. 拼接返回前端能直接访问的真实图片 URL
            return "https://" + BUCKET_NAME + ".cos." + REGION_NAME + ".myqcloud.com/" + key;

        } catch (Exception e) {
            e.printStackTrace();
            return null; // 简单粗暴，上传失败返回 null
        } finally {
            cosClient.shutdown(); // 记得关闭客户端，释放资源
        }
    }
}