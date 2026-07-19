package util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * 图形验证码生成工具 —— 高扭曲版本。
 *
 * <p>
 * 在满足"至少包含数字、小写字母和大写字母各一个"的前提下，
 * 对每个字符施加大幅旋转、随机错切、缩放和纵向偏移，
 * 并叠加密集噪点、多层干扰曲线和贯穿字符的干扰直线，
 * 使自动 OCR 难以分割识别，同时保持人工可辨。
 * </p>
 *
 * <p>
 * 需求文档 1.1 节要求："验证码固定 4 位，每次至少包含数字、小写字母和大写字母各一个，
 * 并对字符做旋转、倾斜和干扰处理"。
 * </p>
 */
public final class CheckCodeUtil
{

    /* ---- 字符池：排除易混淆字符（0/O/o、1/I/l 等） ---- */

    private static final String DIGITS = "23456789";
    private static final String LOWERCASE = "abcdefghjkmnpqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final String ALL_CODES = DIGITS + LOWERCASE + UPPERCASE;

    /** 可选字体列表：混用不同字体增加字符形态差异，加大机器识别难度 */
    private static final String[] FONTS =
    {"Arial", "Serif", "SansSerif", "Dialog", "Monospaced"};

    /** 不同字体对应的风格搭配，使每个字符粗细和斜体不一致 */
    private static final int[] FONT_STYLES =
    {Font.BOLD, Font.ITALIC, Font.BOLD | Font.ITALIC, Font.PLAIN, Font.BOLD};

    private CheckCodeUtil()
    {
    }

    /**
     * 生成并输出一张高扭曲验证码图片。
     *
     * @param width
     *            图片宽度（像素）
     * @param height
     *            图片高度（像素）
     * @param out
     *            输出流，JPEG 写入此处
     * @param size
     *            验证码字符数（项目传入 4）
     * @return 图片中的验证码文字
     */
    public static String outputVerifyImage(int width, int height, OutputStream out, int size) throws IOException
    {
        Random r = new Random();
        String code = createCode(size, r);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /* 浅灰蓝背景，与人眼舒适同时为深色字符提供足够对比度 */
        g.setColor(new Color(235, 243, 255));
        g.fillRect(0, 0, width, height);

        /* ---- 干扰层 1：密集随机噪点，覆盖整个画布 ---- */
        drawNoiseDots(g, width, height, r);

        /* ---- 干扰层 2：贝塞尔曲线，与字符形成交叉 ---- */
        drawInterferenceCurves(g, width, height, r);

        /* ---- 干扰层 3：贯穿横线，模拟划线干扰 ---- */
        drawStrikethroughLines(g, width, height, r);

        /* ---- 主层：对每个字符逐一旋转 + 错切 + 缩放 + 偏移 ---- */
        drawDistortedCharacters(g, code, width, height, r);

        /* ---- 干扰层 4：字符之上再覆盖一层稀疏噪点，模拟扫描噪声 ---- */
        drawOverlayNoise(g, width, height, r);

        g.dispose();
        ImageIO.write(image, "jpg", out);
        return code;
    }

    /** 生成包含数字、小写字母和大写字母各至少一个的随机串，末尾打乱顺序。 */
    private static String createCode(int size, Random r)
    {
        List<Character> chars = new ArrayList<>();
        if (size >= 3)
        {
            chars.add(randomChar(DIGITS, r));
            chars.add(randomChar(LOWERCASE, r));
            chars.add(randomChar(UPPERCASE, r));
        }
        while (chars.size() < size)
        {
            chars.add(randomChar(ALL_CODES, r));
        }
        Collections.shuffle(chars, r);

        StringBuilder sb = new StringBuilder(size);
        for (Character c : chars)
        {
            sb.append(c);
        }
        return sb.toString();
    }

    private static char randomChar(String source, Random r)
    {
        return source.charAt(r.nextInt(source.length()));
    }

    /** 绘制大量半透明彩色噪点（约 140 个），直径 1~3px，提高背景复杂度。 */
    private static void drawNoiseDots(Graphics2D g, int w, int h, Random r)
    {
        for (int i = 0; i < 140; i++)
        {
            g.setColor(new Color(80 + r.nextInt(130), 100 + r.nextInt(120), 130 + r.nextInt(110), 90 + r.nextInt(80)));
            int d = 1 + r.nextInt(3);
            g.fillOval(r.nextInt(w), r.nextInt(h), d, d);
        }
    }

    /** 绘制 8 条贝塞尔曲线，来自不同方向，曲折穿插在字符之间。 */
    private static void drawInterferenceCurves(Graphics2D g, int w, int h, Random r)
    {
        for (int i = 0; i < 8; i++)
        {
            g.setColor(new Color(50 + r.nextInt(100), 80 + r.nextInt(100), 120 + r.nextInt(110), 100 + r.nextInt(60)));
            /* 每条曲线从不同侧边出发，终点随机分布在对面 */
            double startX = r.nextBoolean() ? 0 : w;
            double startY = r.nextInt(h);
            double endX = startX < w / 2.0 ? w : 0;
            double endY = r.nextInt(h);
            double ctrlX = r.nextInt(w);
            double ctrlY = r.nextInt(h);
            g.draw(new QuadCurve2D.Double(startX, startY, ctrlX, ctrlY, endX, endY));
        }
    }

    /** 绘制 5 条贯穿字符区域的干扰直线，部分带虚线风格，模拟人工划线。 */
    private static void drawStrikethroughLines(Graphics2D g, int w, int h, Random r)
    {
        for (int i = 0; i < 5; i++)
        {
            g.setColor(new Color(60 + r.nextInt(90), 80 + r.nextInt(90), 120 + r.nextInt(80), 110 + r.nextInt(50)));
            /* 虚线效果：交替实线 */
            if (r.nextBoolean())
            {
                g.setStroke(new BasicStroke(1.0f + r.nextFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]
                {4.0f + r.nextInt(6), 2.0f + r.nextInt(4)}, 0));
            }
            else
            {
                g.setStroke(new BasicStroke(0.8f + r.nextFloat() * 1.2f));
            }
            int x1 = r.nextInt(w / 3);
            int y1 = r.nextInt(h);
            int x2 = w - r.nextInt(w / 3);
            int y2 = r.nextInt(h);
            g.drawLine(x1, y1, x2, y2);
        }
        g.setStroke(new BasicStroke()); // 恢复默认画笔
    }

    /**
     * 核心扭曲绘制：每个字符独立施加旋转、错切、缩放和随机偏移。
     *
     * <p>
     * 与普通版本的区别：
     * </p>
     * <ul>
     * <li>旋转幅度增大到 ±38°</li>
     * <li>错切系数提高到 0.55 / 0.35</li>
     * <li>纵向随机偏移 ±8px</li>
     * <li>每个字符使用不同字体和风格</li>
     * <li>缩放范围扩大到 0.75~1.25</li>
     * </ul>
     */
    private static void drawDistortedCharacters(
            Graphics2D g, String code, int w, int h, Random r)
    {
        int len = code.length();
        int cellWidth = w / Math.max(1, len);
        int fontSize = Math.max(22, Math.min(30, h - 6));

        for (int i = 0; i < len; i++)
        {
            AffineTransform original = g.getTransform();

            /* 字符水平中心在各自单元格内随机 ±6px 偏移 */
            double cx = i * cellWidth + cellWidth / 2.0 + (r.nextInt(13) - 6);
            /* 纵向在图像中线上下随机偏移 ±8px */
            double cy = h / 2.0 + (r.nextInt(17) - 8);

            g.translate(cx, cy);

            /* 旋转：±38° 大幅增加扭曲感 */
            double angle = Math.toRadians(-38 + r.nextInt(77));
            g.rotate(angle);

            /* 错切（shear）：x 方向 0~0.55，y 方向 0~0.35，使字符倾斜变形 */
            double shearX = (r.nextDouble() - 0.3) * 0.55;
            double shearY = (r.nextDouble() - 0.3) * 0.35;
            g.shear(shearX, shearY);

            /* 缩放：0.75 ~ 1.25 区间内随机，使字符宽高各有差异 */
            double scaleX = 0.75 + r.nextDouble() * 0.50;
            double scaleY = 0.75 + r.nextDouble() * 0.45;
            g.scale(scaleX, scaleY);

            /* 每个字符随机选用不同字体和风格 */
            int fi = r.nextInt(FONTS.length);
            g.setFont(new Font(FONTS[fi], FONT_STYLES[fi], fontSize));

            /* 字符颜色在深蓝到深紫之间随机，保证背景浅色下的对比度 */
            g.setColor(new Color(10 + r.nextInt(60), 30 + r.nextInt(70), 90 + r.nextInt(110)));

            /* 在变换后的坐标系中居中绘制当前字符 */
            g.drawString(String.valueOf(code.charAt(i)), -fontSize / 3.0f, fontSize / 3.0f);

            g.setTransform(original);
        }
    }

    /** 字符上层叠加稀疏噪点（约 30 个），模拟扫描/拍照噪声。 */
    private static void drawOverlayNoise(Graphics2D g, int w, int h, Random r)
    {
        for (int i = 0; i < 30; i++)
        {
            g.setColor(new Color(160 + r.nextInt(95), 170 + r.nextInt(85), 180 + r.nextInt(75), 50 + r.nextInt(90)));
            int d = 1 + r.nextInt(2);
            g.fillOval(r.nextInt(w), r.nextInt(h), d, d);
        }
    }
}
