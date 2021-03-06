package io.swagger.codegen.languages;

import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.SupportingFile;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CliOption;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.commons.lang3.StringUtils;

import static io.swagger.codegen.languages.helpers.ExtensionHelper.getBooleanValue;

public class PerlClientCodegen extends DefaultCodegen implements CodegenConfig {
    public static final String MODULE_NAME = "moduleName";
    public static final String MODULE_VERSION = "moduleVersion";
    protected String moduleName = "WWW::SwaggerClient";
    protected String modulePathPart = moduleName.replaceAll("::", Matcher.quoteReplacement(File.separator));
    protected String moduleVersion = "1.0.0";
    protected String apiDocPath = "docs/";
    protected String modelDocPath = "docs/";

    protected static int emptyFunctionNameCounter = 0;

    public PerlClientCodegen() {
        super();

        // clear import mapping (from default generator) as perl does not use it
        // at the moment
        importMapping.clear();

        modelPackage = File.separatorChar + "Object";
        outputFolder = "generated-code" + File.separatorChar + "perl";
        modelTemplateFiles.put("object.mustache", ".pm");
        apiTemplateFiles.put("api.mustache", ".pm");
        modelTestTemplateFiles.put("object_test.mustache", ".t");
        apiTestTemplateFiles.put("api_test.mustache", ".t");
        modelDocTemplateFiles.put("object_doc.mustache", ".md");
        apiDocTemplateFiles.put("api_doc.mustache", ".md");
        embeddedTemplateDir = templateDir = "perl";


        setReservedWordsLowerCase(
                Arrays.asList(
                        "else", "lock", "qw",
                        "__END__", "elsif", "lt", "qx",
                        "__FILE__", "eq", "m", "s",
                        "__LINE__", "exp", "ne", "sub",
                        "__PACKAGE__", "for", "no", "tr",
                        "and", "foreach", "or", "unless",
                        "cmp", "ge", "package", "until",
                        "continue", "gt", "q", "while",
                        "CORE", "if", "qq", "xor",
                        "do", "le", "qr", "y",
                        "return"
                )
        );

        languageSpecificPrimitives.clear();
        languageSpecificPrimitives.add("int");
        languageSpecificPrimitives.add("double");
        languageSpecificPrimitives.add("string");
        languageSpecificPrimitives.add("boolean");
        languageSpecificPrimitives.add("DateTime");
        languageSpecificPrimitives.add("ARRAY");
        languageSpecificPrimitives.add("HASH");
        languageSpecificPrimitives.add("object");

        typeMapping.clear();
        typeMapping.put("integer", "int");
        typeMapping.put("long", "int");
        typeMapping.put("float", "double");
        typeMapping.put("double", "double");
        typeMapping.put("boolean", "boolean");
        typeMapping.put("string", "string");
        typeMapping.put("date", "DateTime");
        typeMapping.put("DateTime", "DateTime");
        typeMapping.put("password", "string");
        typeMapping.put("array", "ARRAY");
        typeMapping.put("map", "HASH");
        typeMapping.put("object", "object");
        //TODO binary should be mapped to byte array
        // mapped to String as a workaround
        typeMapping.put("binary", "string");
        typeMapping.put("ByteArray", "string");

        cliOptions.clear();
        cliOptions.add(new CliOption(MODULE_NAME, "Perl module name (convention: CamelCase or Long::Module).").defaultValue("SwaggerClient"));
        cliOptions.add(new CliOption(MODULE_VERSION, "Perl module version.").defaultValue("1.0.0"));
        cliOptions.add(CliOption.newBoolean(CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG,
                CodegenConstants.SORT_PARAMS_BY_REQUIRED_FLAG_DESC).defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(CliOption.newBoolean(CodegenConstants.ENSURE_UNIQUE_PARAMS, CodegenConstants
                .ENSURE_UNIQUE_PARAMS_DESC).defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(new CliOption(CodegenConstants.HIDE_GENERATION_TIMESTAMP, CodegenConstants.HIDE_GENERATION_TIMESTAMP_DESC)
                .defaultValue(Boolean.TRUE.toString()));
    }


    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(MODULE_VERSION)) {
            setModuleVersion((String) additionalProperties.get(MODULE_VERSION));
        } else {
            additionalProperties.put(MODULE_VERSION, moduleVersion);
        }

        if (additionalProperties.containsKey(MODULE_NAME)) {
            setModuleName((String) additionalProperties.get(MODULE_NAME));
            setModulePathPart(moduleName.replaceAll("::", Matcher.quoteReplacement(File.separator)));
        } else {
            additionalProperties.put(MODULE_NAME, moduleName);
        }

        // make api and model doc path available in mustache template
        additionalProperties.put("apiDocPath", apiDocPath);
        additionalProperties.put("modelDocPath", modelDocPath);

        // default HIDE_GENERATION_TIMESTAMP to true
        if (!additionalProperties.containsKey(CodegenConstants.HIDE_GENERATION_TIMESTAMP)) {
            additionalProperties.put(CodegenConstants.HIDE_GENERATION_TIMESTAMP, Boolean.TRUE.toString());
        } else {
            additionalProperties.put(CodegenConstants.HIDE_GENERATION_TIMESTAMP,
            Boolean.valueOf(additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP).toString()));
        }

        supportingFiles.add(new SupportingFile("ApiClient.mustache", ("lib/" + modulePathPart).replace('/', File.separatorChar), "ApiClient.pm"));
        supportingFiles.add(new SupportingFile("Configuration.mustache", ("lib/" + modulePathPart).replace('/', File.separatorChar), "Configuration.pm"));
        supportingFiles.add(new SupportingFile("ApiFactory.mustache", ("lib/" + modulePathPart).replace('/', File.separatorChar), "ApiFactory.pm"));
        supportingFiles.add(new SupportingFile("Role.mustache", ("lib/" + modulePathPart).replace('/', File.separatorChar), "Role.pm"));
        supportingFiles.add(new SupportingFile("AutoDoc.mustache", ("lib/" + modulePathPart + "/Role").replace('/', File.separatorChar), "AutoDoc.pm"));
        supportingFiles.add(new SupportingFile("autodoc.script.mustache", "bin", "autodoc"));
        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));
        supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "perl";
    }

    @Override
    public String getHelp() {
        return "Generates a Perl client library.";
    }

    @Override
    public String escapeReservedWord(String name) {
        if(this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    @Override
    public String apiFileFolder() {
        return (outputFolder + "/lib/" + modulePathPart + apiPackage()).replace('/', File.separatorChar);
    }

    @Override
    public String modelFileFolder() {
        return (outputFolder + "/lib/" + modulePathPart + modelPackage()).replace('/', File.separatorChar);
    }

    @Override
    public String apiTestFileFolder() {
        return (outputFolder + "/t").replace('/', File.separatorChar);
    }

    @Override
    public String modelTestFileFolder() {
        return (outputFolder + "/t").replace('/', File.separatorChar);
    }

    @Override
    public String apiDocFileFolder() {
        return (outputFolder + "/" + apiDocPath).replace('/', File.separatorChar);
    }

    @Override
    public String modelDocFileFolder() {
        return (outputFolder + "/" + modelDocPath).replace('/', File.separatorChar);
    }

    @Override
    public String getTypeDeclaration(Schema propertySchema) {
        if (propertySchema instanceof ArraySchema) {
            Schema inner = ((ArraySchema) propertySchema).getItems();
            return String.format("%s[%s]", getSchemaType(propertySchema), getTypeDeclaration(inner));
        } else if (propertySchema instanceof MapSchema && hasSchemaProperties(propertySchema)) {
            Schema inner = (Schema) propertySchema.getAdditionalProperties();
            return String.format("%s[String, %s]", getSchemaType(propertySchema), getTypeDeclaration(inner));
        }
        return super.getTypeDeclaration(propertySchema);
    }

    @Override
    public String getSchemaType(Schema propertySchema) {
        String schemaType = super.getSchemaType(propertySchema);
        String type = null;
        if (typeMapping.containsKey(schemaType)) {
            type = typeMapping.get(schemaType);
            if (languageSpecificPrimitives.contains(type)) {
                return type;
            }
        } else {
            type = schemaType;
        }
        if (type == null) {
            return null;
        }
        return toModelName(type);
    }

    @Override
    public String toDefaultValue(Schema schema) {
        if(schema instanceof StringSchema) {
            if (schema.getDefault() != null) {
                return String.format("'%s'", schema.getDefault());
            }
        }
        if (schema instanceof DateSchema || schema instanceof DateTimeSchema) {
            // TODO
            return null;
        }
        if (schema.getDefault() != null) {
            return schema.getDefault().toString();
        }
        return null;
    }


    @Override
    public String toVarName(String name) {
        // return the name in underscore style
        // PhoneNumber => phone_number
        name = underscore(name); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // parameter name starting with number won't compile
        // need to escape it by appending _ at the beginning
        if (name.matches("^\\d.*")) {
            name = "_" + name;
        }
        return name;
    }

    @Override
    public String toParamName(String name) {
        // should be the same as variable name
        return toVarName(name);
    }

    @Override
    public String toModelName(String name) {
        name = sanitizeName(name); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // model name cannot use reserved keyword
        if (isReservedWord(name)) {
            LOGGER.warn(name + " (reserved word) cannot be used as model name. Renamed to " + camelize("model_" + name));
            name = "model_" + name;
        }

        // model name starts with number
        if (name.matches("^\\d.*")) {
            LOGGER.warn(name + " (model name starts with number) cannot be used as model name. Renamed to " + camelize("model_" + name));
            name = "model_" + name; // e.g. 200Response => Model200Response (after camelize)
        }

        // add prefix/suffic to model name
        if (!StringUtils.isEmpty(modelNamePrefix)) {
            name = modelNamePrefix + "_" + name;
        }

        if (!StringUtils.isEmpty(modelNameSuffix)) {
            name = name + "_" + modelNameSuffix;
        }

        // camelize the model name
        // phone_number => PhoneNumber
        return camelize(name);
    }

    @Override
    public String toModelFilename(String name) {
        // should be the same as the model name
        return toModelName(name);
    }

    @Override
    public String toModelTestFilename(String name) {
        return toModelFilename(name) + "Test";
    }

    @Override
    public String toModelDocFilename(String name) {
        return toModelFilename(name);
    }

    @Override
    public String toApiTestFilename(String name) {
        return toApiFilename(name) + "Test";
    }

    @Override
    public String toApiDocFilename(String name) {
        return toApiFilename(name);
    }

    @Override
    public String toApiFilename(String name) {
        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_"); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // e.g. phone_number_api.rb => PhoneNumberApi.rb
        return camelize(name) + "Api";
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultApi";
        }
        // e.g. phone_number_api => PhoneNumberApi
        return camelize(name) + "Api";
    }

    @Override
    public String toOperationId(String operationId) {
        //rename to empty_function_name_1 (e.g.) if method name is empty
        if (StringUtils.isEmpty(operationId)) {
            operationId = underscore("empty_function_name_" + emptyFunctionNameCounter++);
            LOGGER.warn("Empty method name (operationId) found. Renamed to " + operationId);
            return operationId;
        }

        // method name cannot use reserved keyword, e.g. return
        if (isReservedWord(operationId)) {
            LOGGER.warn(operationId + " (reserved word) cannot be used as method name. Renamed to " + underscore("call_" + operationId));
            return underscore("call_" + operationId);
        }

        //return underscore(operationId).replaceAll("[^A-Za-z0-9_]", "");
        return underscore(sanitizeName(operationId));
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setModulePathPart(String modulePathPart) {
        this.modulePathPart = modulePathPart;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    @Override
    public void setParameterExampleValue(CodegenParameter p) {
        if (getBooleanValue(p, CodegenConstants.IS_STRING_EXT_NAME) || getBooleanValue(p, CodegenConstants.IS_BINARY_EXT_NAME)
                || getBooleanValue(p, CodegenConstants.IS_BYTE_ARRAY_EXT_NAME)
                || getBooleanValue(p, CodegenConstants.IS_FILE_EXT_NAME)) {
            p.example = "'" + p.example + "'";
        } else if (getBooleanValue(p, CodegenConstants.IS_BOOLEAN_EXT_NAME)) {
            if (Boolean.parseBoolean(p.example))
                p.example = "1";
            else
                p.example = "0";
        } else if (getBooleanValue(p, CodegenConstants.IS_DATE_TIME_EXT_NAME)
                || getBooleanValue(p, CodegenConstants.IS_DATE_EXT_NAME)) {
            p.example = "DateTime->from_epoch(epoch => str2time('" + p.example + "'))";
        }

    }

    @Override
    public String escapeQuotationMark(String input) {
        return input.replace("'", "");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        // remove =end, =cut to avoid code injection
        return input.replace("=begin", "=_begin").replace("=end", "=_end").replace("=cut", "=_cut").replace("=pod", "=_pod");
    }
}
