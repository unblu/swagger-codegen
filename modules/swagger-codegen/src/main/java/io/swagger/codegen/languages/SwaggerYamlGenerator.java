package io.swagger.codegen.languages;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.SupportingFile;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.core.util.Yaml;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SwaggerYamlGenerator extends DefaultCodegen implements CodegenConfig {
    public static final String OUTPUT_NAME = "outputFile";

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerYamlGenerator.class);

    protected String outputFile = "swagger.yaml";

    public SwaggerYamlGenerator() {
        super();
        embeddedTemplateDir = templateDir = "swagger";
        outputFolder = "generated-code/swagger";

        cliOptions.add(new CliOption(OUTPUT_NAME, "output filename"));

        supportingFiles.add(new SupportingFile("README.md", "", "README.md"));
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.DOCUMENTATION;
    }

    @Override
    public String getName() {
        return "swagger-yaml";
    }

    @Override
    public String getHelp() {
        return "Creates a static swagger.yaml file.";
    }


    @Override
    public void processOpts() {
        super.processOpts();
        if(additionalProperties.containsKey(OUTPUT_NAME)) {
            this.outputFile = additionalProperties.get(OUTPUT_NAME).toString();
        }
    }

    @Override
    public void processOpenAPI(OpenAPI openAPI) {
        try {
            String swaggerString = Yaml.mapper().writeValueAsString(openAPI);
            String outputFile = outputFolder + File.separator + this.outputFile;
            FileUtils.writeStringToFile(new File(outputFile), swaggerString);
            LOGGER.debug("wrote file to " + outputFile);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String escapeQuotationMark(String input) {
        // just return the original string
        return input;
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        // just return the original string
        return input;
    }   

}
