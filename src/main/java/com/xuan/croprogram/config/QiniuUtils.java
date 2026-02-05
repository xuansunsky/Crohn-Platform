package com.xuan.croprogram.config; // ⚠️ 改成你自己的包名

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
public class QiniuUtils {

    // 1. 填入你的“四大法宝”
    // 兄弟，正式上线建议放配置文件里，现在为了爽先写死
    private static final String ACCESS_KEY = "XPm8RtmoMxGFyJdgYhdXzyCx4DKViZcOJA56K1vz";
    private static final String SECRET_KEY = "nQVgHan7Gv0JIai0QZ6nnFOPCgq5TJRC1tvMGmMu";
    private static final String BUCKET = "xiaoxuan666";
    // 你的测试域名 (注意要加 http://)
    private static final String DOMAIN = "http://t9yv15p3w.hd-bkt.clouddn.com";

    public String upload(MultipartFile file) {
        // 2. 构造一个带指定 Region 对象的配置类
        // 你的域名带 hd-bkt，说明是华东机房 (Huadong)
        Configuration cfg = new Configuration(Region.huadong());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本

        UploadManager uploadManager = new UploadManager(cfg);

        // 3. 生成文件名 (UUID 防止重名)
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String key = UUID.randomUUID().toString() + suffix; // 比如：asd-123.jpg

        try {
            // 4. 生成上传凭证 (Token)
            Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
            String upToken = auth.uploadToken(BUCKET);

            // 5. 开始上传
            Response response = uploadManager.put(file.getInputStream(), key, upToken, null, null);

            // 6. 解析结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);

            // 7. 拼接出真正的图片链接并返回
            // 比如：http://t9yv15p3w.../asd-123.jpg
            return DOMAIN + "/" + putRet.key;

        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                // ignore
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}