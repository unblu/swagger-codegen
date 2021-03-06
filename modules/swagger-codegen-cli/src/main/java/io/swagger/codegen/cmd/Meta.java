package io.swagger.codegen.cmd;

import ch.lambdaj.function.convert.Converter;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.samskivert.mustache.Mustache;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.codegen.SupportingFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.collection.LambdaCollections.with;
import static com.google.common.base.Joiner.on;

/**
 * User: lanwen Date: 24.03.15 Time: 20:22
 */
public class Meta implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Meta.class);

    private static final String TEMPLATE_DIR_CLASSPATH = "codegen";
    private static final String MUSTACHE_EXTENSION = ".mustache";

    private String outputFolder = "";
    private String name = "default";
    private String targetPackage = "io.swagger.codegen";

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    @Override
    public void run() {
        final File targetDir = new File(outputFolder);
        LOGGER.info("writing to folder [{}]", targetDir.getAbsolutePath());

        String mainClass = CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, name) + "Generator";

        List<SupportingFile> supportingFiles =
                ImmutableList
                        .of(new SupportingFile("pom.mustache", "", "pom.xml"),
                                new SupportingFile("generatorClass.mustache", on(File.separator)
                                        .join("src/main/java", asPath(targetPackage)), mainClass
                                        .concat(".java")), new SupportingFile("README.mustache",
                                        "", "README.md"), new SupportingFile("api.template",
                                        "src/main/resources" + File.separator + name,
                                        "api.mustache"), new SupportingFile("model.template",
                                        "src/main/resources" + File.separator + name,
                                        "model.mustache"), new SupportingFile("services.mustache",
                                        "src/main/resources/META-INF/services",
                                        "io.swagger.codegen.CodegenConfig"));

        String swaggerVersion = Version.readVersionFromResources();

        Map<String, Object> data =
                new ImmutableMap.Builder<String, Object>().put("generatorPackage", targetPackage)
                        .put("generatorClass", mainClass).put("name", name)
                        .put("fullyQualifiedGeneratorClass", targetPackage + "." + mainClass)
                        .put("swaggerCodegenVersion", swaggerVersion).build();


        with(supportingFiles).convert(processFiles(targetDir, data));
    }

    /**
     * Converter method to process supporting files: execute with mustache, or simply copy to
     * destination directory
     *
     * @param targetDir - destination directory
     * @param data - map with additional params needed to process templates
     * @return converter object to pass to lambdaj
     */
    private static Converter<SupportingFile, File> processFiles(final File targetDir,
            final Map<String, Object> data) {
        return new Converter<SupportingFile, File>() {
            private DefaultGenerator generator = new DefaultGenerator();

            @Override
            public File convert(SupportingFile support) {
                try {
                    File destinationFolder =
                            new File(new File(targetDir.getAbsolutePath()), support.folder);
                    File outputFile = new File(destinationFolder, support.destinationFilename);

                    String template =
                            generator.readTemplate(new File(TEMPLATE_DIR_CLASSPATH,
                                    support.templateFile).getPath());
                    String formatted = template;

                    if (support.templateFile.endsWith(MUSTACHE_EXTENSION)) {
                        LOGGER.info("writing file to {}", outputFile.getAbsolutePath());
                        formatted =
                                Mustache.compiler().withLoader(loader(generator)).defaultValue("")
                                        .compile(template).execute(data);
                    } else {
                        LOGGER.info("copying file to {}", outputFile.getAbsolutePath());
                    }

                    FileUtils.writeStringToFile(outputFile, formatted);
                    return outputFile;

                } catch (IOException e) {
                    throw new RuntimeException("Can't generate project", e);
                }
            }
        };
    }

    /**
     * Creates mustache loader for template using classpath loader
     *
     * @param generator - class with reader getter
     * @return loader for template
     */
    private static Mustache.TemplateLoader loader(final DefaultGenerator generator) {
        return new Mustache.TemplateLoader() {
            @Override
            public Reader getTemplate(String name) {
                return generator.getTemplateReader(TEMPLATE_DIR_CLASSPATH + File.separator
                        + name.concat(MUSTACHE_EXTENSION));
            }
        };
    }

    /**
     * Converts package name to path on file system
     *
     * @param packageName - package name to convert
     * @return relative path
     */
    private static String asPath(String packageName) {
        return packageName.replace(".", File.separator);
    }
}
