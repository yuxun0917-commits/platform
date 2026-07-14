package com.platform.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.io.InputStream;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * MyBatis-Plus 代码生成器
 *
 * <p>根据数据库表名一键生成 Entity、Mapper、Service 结构代码，
 * 严格遵循多模块分层规范：</p>
 * <ul>
 *   <li>Entity     → platform-common 模块（com.platform.common.entity）</li>
 *   <li>Mapper     → platform-service 模块（com.platform.service.mapper）</li>
 *   <li>Service    → platform-service 模块（com.platform.service.service）</li>
 *   <li>ServiceImpl→ platform-service 模块（com.platform.service.service.impl）</li>
 *   <li>Mapper XML → platform-service 模块（resources/mapper）</li>
 * </ul>
 *
 * <p>使用方式：修改下方数据库连接与表名，运行 main 方法即可。</p>
 *
 * @author platform
 */
public class CodeGenerator {

    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();

        try (InputStream in = CodeGenerator.class.getResourceAsStream("mysql.properties")) {
            props.load(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        URL = props.getProperty("jdbc.url");
        USERNAME = props.getProperty("jdbc.username");
        PASSWORD = props.getProperty("jdbc.password");
    }

    private static final String PLATFORM_PATH = System.getProperty("user.dir");
    private static final String COMMON_MODULE_PATH = PLATFORM_PATH + "/platform-common";
    private static final String SERVICE_MODULE_PATH = PLATFORM_PATH + "/platform-service";

    public static void main(String[] args) {
        String[] tables = {"sys_storage_config"};
        generate(tables);
    }

    private static void generate(String[] tables) {
        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                .globalConfig(builder -> builder
                        .author("yuxun")
                        .outputDir(SERVICE_MODULE_PATH + "/src/main/java")
                        .dateType(DateType.TIME_PACK)
                        .commentDate("yyyy-MM-dd")
                        .disableOpenDir()
                )
                .packageConfig(builder -> builder
                        .parent("com.platform")
                        // Entity → platform-common 模块
                        .entity("common.entity.admin")
                        // Mapper / Service / ServiceImpl → platform-service 模块
                        .mapper("service.mapper")
                        .service("service.service")
                        .serviceImpl("service.service.impl")
                        // 各层输出到不同模块目录
                        .pathInfo(buildPathInfoMap()))
                // 自定义类型转换
                .dataSourceConfig(builder -> builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                    int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                    if (typeCode == Types.SMALLINT || typeCode == Types.TINYINT) {
                        return DbColumnType.INTEGER;
                    }
                    return typeRegistry.getColumnType(metaInfo);
                }))
                // 不生成 Controller
                .templateConfig(builder -> builder.disable(TemplateType.CONTROLLER))
                .strategyConfig(builder -> builder
                        .addInclude(tables)
                        .entityBuilder()
                        .naming(NamingStrategy.underline_to_camel)
                        .columnNaming(NamingStrategy.underline_to_camel)
                        .enableLombok()
                        .enableChainModel()
                        .enableTableFieldAnnotation()
                        .enableFileOverride()
                        .mapperBuilder()
                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                        .formatServiceImplFileName("%sServiceImpl"))
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

        System.out.println("\n================ 代码生成完成 ================");
        System.out.println("Entity      已生成至 platform-common  模块（com.platform.common.entity）");
        System.out.println("Mapper      已生成至 platform-service 模块（com.platform.service.mapper）");
        System.out.println("Service     已生成至 platform-service 模块（com.platform.service.service）");
        System.out.println("ServiceImpl 已生成至 platform-service 模块（com.platform.service.service.impl）");
        System.out.println("Mapper XML  已生成至 platform-service 模块（resources/mapper）");
    }

    /**
     * 构建各层代码输出路径映射
     *
     * <p>MyBatis-Plus 的 pathInfo 存储的是【完整文件路径】（包含包目录），
     * 而非仅模块根目录。默认路径 = outputDir + 包名转路径。
     * 自定义 pathInfo 会整体覆盖默认值，因此必须手动拼接包路径。</p>
     *
     * Entity → platform-common，其余 → platform-service
     */
    private static Map<OutputFile, String> buildPathInfoMap() {
        Map<OutputFile, String> pathInfo = new HashMap<>();
        // Entity → platform-common 模块（包路径需手动拼接）
        String entityPackage = "com.platform.common.entity";
        pathInfo.put(OutputFile.entity, joinPath(COMMON_MODULE_PATH + "/src/main/java", entityPackage));
        // Mapper XML → platform-service 模块的 resources（XML 无包名，直接指定目录）
        pathInfo.put(OutputFile.xml, SERVICE_MODULE_PATH + "/src/main/resources/mapper");
        return pathInfo;
    }

    /**
     * 将输出目录与包名拼接为完整路径
     * 例如：joinPath("/platform/src/main/java", "com.platform.common.entity")
     *      → "/platform/src/main/java/com/platform/common/entity"
     *
     * @param outputDir  输出目录
     * @param packageName 包名
     * @return 完整路径
     */
    private static String joinPath(String outputDir, String packageName) {
        if (!outputDir.endsWith("/") && !outputDir.endsWith("\\")) {
            outputDir += "/";
        }
        return outputDir + packageName.replace(".", "/");
    }
}
