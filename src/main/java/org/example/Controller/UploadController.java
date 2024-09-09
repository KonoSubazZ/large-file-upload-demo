package org.example.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@CrossOrigin
@RestController
public class UploadController {

    public static final String UPLOAD_PATH = "D:\\upload\\";
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam MultipartFile file) throws IOException {

        File dstFile = new File(UPLOAD_PATH, String.format("%s.%s", UUID.randomUUID(), StringUtils.getFilename(file.getOriginalFilename())));
        file.transferTo(dstFile);
        return ResponseEntity.ok(Map.of("path", dstFile.getAbsolutePath()));
    }

    /**
     * @param chunkSize   每个分片大小
     * @param chunkNumber 当前分片
     * @param md5         文件总MD5
     * @param file        当前分片文件数据
     * @return
     * @throws IOException
     */
    @RequestMapping("/uploadBig")
    public ResponseEntity<Map<String, String>> uploadBig(@RequestParam Long chunkSize, @RequestParam Integer totalNumber, @RequestParam Long chunkNumber, @RequestParam String md5, @RequestParam MultipartFile file) throws Exception {

        //文件存放位置
        String dstFile = String.format("%s\\%s\\%s.%s", UPLOAD_PATH, md5, md5, StringUtils.getFilenameExtension(file.getOriginalFilename()));
        //上传分片信息存放位置
        String confFile = String.format("%s\\%s\\%s.conf", UPLOAD_PATH, md5, md5);
        //第一次创建分片记录文件
        //创建目录
        File dir = new File(dstFile).getParentFile();
        if (!dir.exists()) {
            dir.mkdir();
            //所有分片状态设置为0
            byte[] bytes = new byte[totalNumber];
            Files.write(Path.of(confFile), bytes);
        }
        //随机分片写入文件
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(dstFile, "rw");
             RandomAccessFile randomAccessConfFile = new RandomAccessFile(confFile, "rw");
             InputStream inputStream = file.getInputStream()) {
            //定位到该分片的偏移量
            randomAccessFile.seek(chunkNumber * chunkSize);
            //写入该分片数据
            randomAccessFile.write(inputStream.readAllBytes());
            //定位到当前分片状态位置
            randomAccessConfFile.seek(chunkNumber);
            //设置当前分片上传状态为1
            randomAccessConfFile.write(1);
        }
        return ResponseEntity.ok(Map.of("path", dstFile));
    }


    /**
     * 获取文件分片状态，检测文件MD5合法性
     * @param md5
     * @return
     * @throws Exception
     */
    @RequestMapping("/checkFile")
    public ResponseEntity<Map<String, String>> checkFile(@RequestParam String md5) throws Exception {
        String uploadPath = String.format("%s\\%s\\%s.conf", UPLOAD_PATH, md5, md5);
        Path path = Path.of(uploadPath);
        //MD5目录不存在文件从未上传过
        if (!Files.exists(path.getParent())) {
            return ResponseEntity.ok(Map.of("msg", "文件未上传"));
        }
        //判断文件是否上传成功
        StringBuilder stringBuilder = new StringBuilder();
        byte[] bytes = Files.readAllBytes(path);
        for (byte b : bytes) {
            stringBuilder.append(String.valueOf(b));
        }
        //所有分片上传完成计算文件MD5
        if (!stringBuilder.toString().contains("0")) {
            File file = new File(String.format("%s\\%s\\", UPLOAD_PATH, md5));
            File[] files = file.listFiles();
            String filePath = "";
            for (File f : files) {
                // 计算文件MD5是否相等
                if (!f.getName().contains("conf")) {
                    filePath = f.getAbsolutePath();
                    try (InputStream inputStream = new FileInputStream(f)) {
                        // 摘要生成MD5
                        String md5pwd = DigestUtils.md5DigestAsHex(inputStream);
                        if (!md5pwd.equalsIgnoreCase(md5)) {
                            return ResponseEntity.ok(Map.of("msg", "文件上传失败"));
                        }
                    }
                }
            }
            return ResponseEntity.ok(Map.of("path", filePath));
        } else {

            //文件未上传完成，反回每个分片状态，前端将未上传的分片继续上传
            return ResponseEntity.ok(Map.of("chucks", stringBuilder.toString()));
        }

    }

}
