package util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * 图形验证码生成工具。
 *
 * <p>
 * 它同时完成两件事：生成随机验证码文字，并把文字绘制成带干扰线的 JPEG 图片。 Servlet 会把返回的文字保存在 Session
 * 中，把图片二进制写给浏览器。
 */
public final class CheckCodeUtil
{
    /** 去掉 0/O、1/I 等容易混淆字符，降低用户误读验证码的概率。 */
    private static final String CODES = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    /** 纯工具类不保存对象状态，因此禁止外部实例化。 */
    private CheckCodeUtil()
    {
    }

    /**
     * 生成并输出一张验证码图片。
     *
     * @param width
     *            图片宽度，单位为像素
     * @param height
     *            图片高度，单位为像素
     * @param out
     *            HTTP 响应提供的输出流，图片最终写入这里
     * @param size
     *            验证码字符数量
     * @return 图片中实际绘制的验证码文字
     */
    public static String outputVerifyImage(int width, int height, OutputStream out, int size) throws IOException
    {
        // Random 用于选择字符、颜色和坐标，使每次生成结果都不同。
        Random random = new Random();
        // StringBuilder 适合在循环中逐个追加字符，比反复使用字符串 + 更高效。
        StringBuilder code = new StringBuilder();
        // 随机取 size 次字符，拼成最终验证码。
        for (int i = 0; i < size; i++)
        {
            code.append(CODES.charAt(random.nextInt(CODES.length())));
        }
        // 在内存中创建 RGB 位图，相当于准备一张空白画布。
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Graphics2D 是画笔对象，后续背景、线条和文字都由它绘制。
        Graphics2D g = image.createGraphics();
        // 开启抗锯齿，让文字和线条边缘更平滑。
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 先用浅蓝色填满整个背景。
        g.setColor(new Color(239, 246, 255));
        g.fillRect(0, 0, width, height);
        // 绘制 10 条随机干扰线，增加机器识别难度。
        for (int i = 0; i < 10; i++)
        {
            g.setColor(new Color(120 + random.nextInt(100), 140 + random.nextInt(90), 160 + random.nextInt(80)));
            g.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }
        // 设置验证码字体：Arial、粗体、24 像素。
        g.setFont(new Font("Arial", Font.BOLD, 24));
        // 每个字符使用略有不同的颜色和纵向位置，减少固定模板特征。
        for (int i = 0; i < size; i++)
        {
            g.setColor(new Color(20 + random.nextInt(70), 60 + random.nextInt(80), 130 + random.nextInt(100)));
            g.drawString(String.valueOf(code.charAt(i)), 10 + i * 23, 28 + random.nextInt(5));
        }
        // 释放系统绘图资源；不释放可能造成服务器长期运行后资源泄漏。
        g.dispose();
        // 把内存位图编码成 JPEG 并写入 HTTP 输出流。
        ImageIO.write(image, "jpg", out);
        // 返回文字，调用者需要把它存入 Session，后续与用户输入进行比较。
        return code.toString();
    }
}
