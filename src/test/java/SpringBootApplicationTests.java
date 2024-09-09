import org.example.Main;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.RandomAccess;
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class SpringBootApplicationTests {
    @Test
    public void testRandomAccessFile1() {
        String filePath = "D:\\upload\\202a7381d1fcbf076ad28349b3242882\\202a7381d1fcbf076ad28349b3242882.conf";

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw")) {
            long length = randomAccessFile.length();
            byte[] buffer = new byte[(int) length];
            randomAccessFile.readFully(buffer);

            // 将字节数组转换为字符串，并指定编码
            String content = new String(buffer, StandardCharsets.UTF_8);
            System.out.println("文件内容：\n" + content);

            // 如果需要查看原始字节数组
            System.out.println("原始字节数组：\n" + bytesToString(buffer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 辅助方法：将字节数组转换为字符串表示形式
    private static String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        /**
         这里使用 String.format 方法将每个字节转换为十六进制形式，
         并确保每个十六进制数至少有两个字符（不足两位时前面补零）。
         例如，字节值 10 被转换为 "0A"，字节值 255 被转换为 "FF"。

         %02X 是一个格式化字符串：
         % 表示开始格式化。
         02 表示至少两个字符，不足时前面补零。
         X 表示十六进制大写形式。
         b 是当前循环中的字节值。
         */

        for (byte b : bytes) {
//            sb.append(String.valueOf(b));
            sb.append(b);
        }
        return sb.toString().trim();
    }

}