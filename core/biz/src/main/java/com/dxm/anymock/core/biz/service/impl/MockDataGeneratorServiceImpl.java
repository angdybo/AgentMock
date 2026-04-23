package com.dxm.anymock.core.biz.service.impl;

import com.dxm.anymock.core.biz.service.MockDataGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mock 数据生成器实现
 */
@Service
public class MockDataGeneratorServiceImpl implements MockDataGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(MockDataGeneratorServiceImpl.class);

    private static final Random random = new Random();

    // 匹配 {{functionName()}} 或 {{functionName(args)}} 格式的占位符
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(\\w+)(?:\\(([^)]*)\\))?\\}\\}");

    // 中文字符（用于生成中文姓名等）
    private static final String CHINESE_CHARS = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜戚谢邹喻柏水窦章云苏潘葛奚范彭郎鲁韦昌马苗凤花方俞任袁柳酆鲍史唐费廉岑薛雷贺倪汤滕殷罗毕郝邬安常乐于时傅皮卞齐康伍余元卜顾孟平黄和穆萧尹姚邵湛汪祁毛禹狄米贝明臧计伏成戴谈宋茅庞熊纪舒屈项祝董梁杜阮蓝闵席季麻强贾路娄危江童颜郭梅盛林刁钟徐邱骆高夏蔡田樊胡凌霍万柯卢莫房缪干宗应丁宣邓郁单杭洪包诸左石崔吉钮龚";
    private static final String[] CHINESE_FIRST_NAMES = {"伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军", "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀兰", "霞", "平", "刚", "桂英", "玉兰", "英", "华", "红", "海燕", "梅", "强", "建国", "国", "明", "秀珍", "文", "建国", "桂兰", "文", "红", "秀珍", "建国", "文", "静", "建国", "红", "秀珍", "建国"};

    @Override
    public String parse(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return responseBody;
        }

        try {
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(responseBody);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String functionName = matcher.group(1);
                String args = matcher.group(2);
                String replacement = generate(functionName, args);
                matcher.appendReplacement(result, replacement);
            }

            matcher.appendTail(result);
            return result.toString();
        } catch (Exception e) {
            logger.error("解析 Mock 数据生成器失败: {}", e.getMessage(), e);
            return responseBody;
        }
    }

    /**
     * 根据函数名生成数据
     */
    private String generate(String functionName, String args) {
        try {
            switch (functionName) {
                // 字符串类
                case "randomString":
                    return generateRandomString(parseInt(args, 10));
                case "uuid":
                    return UUID.randomUUID().toString();
                case "email":
                    return generateEmail();
                case "phone":
                    return generatePhone();
                case "idCard":
                    return generateIdCard();
                case "chineseName":
                    return generateChineseName();
                case "englishName":
                    return generateEnglishName();

                // 数字类
                case "randomInt":
                    String[] intArgs = parseArgs(args);
                    int min = parseInt(intArgs[0], 1);
                    int max = parseInt(intArgs[1], 100);
                    return String.valueOf(generateRandomInt(min, max));
                case "randomFloat":
                    String[] floatArgs = parseArgs(args);
                    float fmin = parseFloat(floatArgs[0], 0);
                    float fmax = parseFloat(floatArgs[1], 1);
                    int decimals = parseInt(floatArgs[2], 2);
                    return String.format("%." + decimals + "f", generateRandomFloat(fmin, fmax));

                // 日期时间类
                case "date":
                    return generateDate(args);
                case "dateTime":
                    return generateDateTime(args);
                case "timestamp":
                    return String.valueOf(System.currentTimeMillis() / 1000);
                case "now":
                    return generateDateTime("yyyy-MM-dd HH:mm:ss");

                // 布尔类
                case "randomBoolean":
                    return random.nextBoolean() ? "true" : "false";

                // 数组类
                case "randomInts":
                    String[] intsArgs = parseArgs(args);
                    int imin = parseInt(intsArgs[0], 1);
                    int imax = parseInt(intsArgs[1], 100);
                    int count = parseInt(intsArgs[2], 5);
                    return generateRandomInts(imin, imax, count);
                case "randomStrings":
                    String[] strsArgs = parseArgs(args);
                    int length = parseInt(strsArgs[0], 10);
                    int scount = parseInt(strsArgs[1], 5);
                    return generateRandomStrings(length, scount);

                // 对象类
                case "address":
                    return generateAddress();
                case "company":
                    return generateCompany();
                case "province":
                    return getRandomProvince();
                case "city":
                    return getRandomCity();

                // 网络类
                case "url":
                    return generateUrl();
                case "ipv4":
                    return generateIPv4();
                case "macAddress":
                    return generateMacAddress();
                case "color":
                    return generateColor();
                case "rgb":
                    return generateRGB();

                // 图片类
                case "image":
                    String[] imgArgs = parseArgs(args);
                    int width = parseInt(imgArgs[0], 300);
                    int height = parseInt(imgArgs[1], 200);
                    return "https://via.placeholder.com/" + width + "x" + height;
                case "avatar":
                    return "https://i.pravatar.cc/150";

                // 特殊类
                case "nullValue":
                    return "null";
                case "emptyString":
                    return "";
                case "emptyArray":
                    return "[]";
                case "emptyObject":
                    return "{}";

                default:
                    logger.warn("未知的 Mock 数据生成器函数: {}", functionName);
                    return "{{" + functionName + "(" + (args != null ? args : "") + ")}}";
            }
        } catch (Exception e) {
            logger.error("生成 Mock 数据失败: {}({})", functionName, args, e);
            return "{{" + functionName + "(" + (args != null ? args : "") + ")}}";
        }
    }

    // ========== 字符串生成器 ==========

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateEmail() {
        String[] domains = {"gmail.com", "yahoo.com", "hotmail.com", "163.com", "qq.com"};
        return generateRandomString(8).toLowerCase() + "@" + domains[random.nextInt(domains.length)];
    }

    private String generatePhone() {
        String[] prefixes = {"138", "139", "150", "151", "152", "186", "187", "188"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < 8; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateIdCard() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 17; i++) {
            sb.append(random.nextInt(10));
        }
        sb.append(random.nextInt(10)); // 最后一位
        return sb.toString();
    }

    private String generateChineseName() {
        String surname = String.valueOf(CHINESE_CHARS.charAt(random.nextInt(CHINESE_CHARS.length())));
        String firstName = CHINESE_FIRST_NAMES[random.nextInt(CHINESE_FIRST_NAMES.length)];
        return surname + firstName;
    }

    private String generateEnglishName() {
        String[] firstNames = {"John", "Mary", "David", "Sarah", "Michael", "Emma", "James", "Olivia", "Robert", "Emily"};
        String[] lastNames = {"Smith", "Johnson", "Brown", "Williams", "Jones", "Miller", "Davis", "Garcia", "Wilson", "Taylor"};
        return firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
    }

    // ========== 数字生成器 ==========

    private int generateRandomInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private float generateRandomFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    // ========== 日期时间生成器 ==========

    private String generateDate(String format) {
        if (format == null || format.trim().isEmpty()) {
            format = "yyyy-MM-dd";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    private String generateDateTime(String format) {
        if (format == null || format.trim().isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    // ========== 数组生成器 ==========

    private String generateRandomInts(int min, int max, int count) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(", ");
            sb.append(generateRandomInt(min, max));
        }
        sb.append("]");
        return sb.toString();
    }

    private String generateRandomStrings(int length, int count) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) sb.append(", ");
            sb.append("\"").append(generateRandomString(length)).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    // ========== 对象生成器 ==========

    private String generateAddress() {
        String[] provinces = {"北京市", "上海市", "广东省", "浙江省", "江苏省", "四川省", "湖北省", "湖南省"};
        String[] cities = {"朝阳区", "海淀区", "浦东新区", "黄浦区", "西湖区", "玄武区"};
        String[] streets = {"路", "街", "大道", "巷", "弄"};
        String province = provinces[random.nextInt(provinces.length)];
        String city = cities[random.nextInt(cities.length)];
        String street = streets[random.nextInt(streets.length)];
        int number = generateRandomInt(1, 999);
        return province + city + "中山" + street + number + "号";
    }

    private String generateCompany() {
        String[] prefixes = {"北京", "上海", "深圳", "广州", "杭州"};
        String[] names = {"科技", "网络", "信息", "数据", "创新"};
        String[] suffixes = {"有限公司", "股份有限公司", "科技有限公司", "网络科技"};
        return prefixes[random.nextInt(prefixes.length)] + names[random.nextInt(names.length)] + suffixes[random.nextInt(suffixes.length)];
    }

    private String getRandomProvince() {
        String[] provinces = {"北京市", "上海市", "广东省", "浙江省", "江苏省", "四川省", "湖北省", "湖南省", "河南省", "山东省"};
        return provinces[random.nextInt(provinces.length)];
    }

    private String getRandomCity() {
        String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "长沙", "郑州", "济南"};
        return cities[random.nextInt(cities.length)];
    }

    // ========== 网络生成器 ==========

    private String generateUrl() {
        String[] protocols = {"http", "https"};
        String[] domains = {"example.com", "test.com", "api.com", "service.com"};
        return protocols[random.nextInt(protocols.length)] + "://" + domains[random.nextInt(domains.length)] + "/" + generateRandomString(8);
    }

    private String generateIPv4() {
        return generateRandomInt(1, 255) + "." +
               generateRandomInt(1, 255) + "." +
               generateRandomInt(1, 255) + "." +
               generateRandomInt(1, 255);
    }

    private String generateMacAddress() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append(":");
            sb.append(String.format("%02X", random.nextInt(256)));
        }
        return sb.toString();
    }

    private String generateColor() {
        return String.format("#%06X", random.nextInt(0xFFFFFF + 1));
    }

    private String generateRGB() {
        return "rgb(" + generateRandomInt(0, 255) + ", " + generateRandomInt(0, 255) + ", " + generateRandomInt(0, 255) + ")";
    }

    // ========== 工具方法 ==========

    private String[] parseArgs(String args) {
        if (args == null || args.trim().isEmpty()) {
            return new String[0];
        }
        // 简单的参数解析，支持逗号分隔
        return args.split(",");
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private float parseFloat(String value, float defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
