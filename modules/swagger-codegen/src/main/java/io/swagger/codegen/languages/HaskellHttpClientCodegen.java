package io.swagger.codegen.languages;

import io.swagger.codegen.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import io.swagger.codegen.languages.helpers.ExtensionHelper;
import io.swagger.codegen.utils.ModelUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.core.util.Json;
import org.apache.commons.io.FileUtils;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.SupportingFile;
import io.swagger.v3.core.util.Yaml;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.regex.Matcher;

import static io.swagger.codegen.CodegenConstants.IS_ENUM_EXT_NAME;
import static io.swagger.codegen.languages.helpers.ExtensionHelper.getBooleanValue;

public class HaskellHttpClientCodegen extends DefaultCodegen implements CodegenConfig {

    // source folder where to write the files
    protected String sourceFolder = "src";

    protected String artifactId = "swagger-haskell-http-client";
    protected String artifactVersion = "1.0.0";

    protected String defaultDateFormat = "%Y-%m-%d";

    protected Boolean useMonadLogger = false;
    protected Boolean genEnums = true;

    // CLI PROPS
    public static final String PROP_ALLOW_FROMJSON_NULLS = "allowFromJsonNulls";
    public static final String PROP_ALLOW_TOJSON_NULLS = "allowToJsonNulls";
    public static final String PROP_DATETIME_FORMAT = "dateTimeFormat";
    public static final String PROP_DATE_FORMAT = "dateFormat";
    public static final String PROP_GENERATE_ENUMS = "generateEnums";
    public static final String PROP_GENERATE_FORM_URLENCODED_INSTANCES = "generateFormUrlEncodedInstances";
    public static final String PROP_GENERATE_LENSES = "generateLenses";
    public static final String PROP_GENERATE_MODEL_CONSTRUCTORS = "generateModelConstructors";
    public static final String PROP_INLINE_MIME_TYPES = "inlineMimeTypes";
    public static final String PROP_MODEL_DERIVING = "modelDeriving";
    public static final String PROP_STRICT_FIELDS = "strictFields";
    public static final String PROP_USE_MONAD_LOGGER = "useMonadLogger";

    // protected String MODEL_IMPORTS = "modelImports";
    // protected String MODEL_EXTENSIONS = "modelExtensions";

    private static final Pattern LEADING_UNDERSCORE = Pattern.compile("^_+");

    static final String MEDIA_TYPE = "mediaType";
    static final String MIME_NO_CONTENT = "MimeNoContent";

    // vendor extensions
    static final String X_ALL_UNIQUE_PARAMS = "x-allUniqueParams";
    static final String X_COLLECTION_FORMAT = "x-collectionFormat";
    static final String X_HADDOCK_PATH = "x-haddockPath";
    static final String X_HAS_BODY_OR_FORM_PARAM = "x-hasBodyOrFormParam";
    static final String X_HAS_ENUM_SECTION = "x-hasEnumSection";
    static final String X_HAS_MIME_FORM_URL_ENCODED = "x-hasMimeFormUrlEncoded";
    static final String X_HAS_NEW_TAG = "x-hasNewTag";
    static final String X_HAS_OPTIONAL_PARAMS = "x-hasOptionalParams";
    static final String X_HAS_UNKNOWN_MIME_TYPES = "x-hasUnknownMimeTypes";
    static final String X_HAS_UNKNOWN_RETURN = "x-hasUnknownReturn";
    static final String X_INLINE_CONTENT_TYPE = "x-inlineContentType";
    static final String X_INLINE_ACCEPT = "x-inlineAccept";
    static final String X_IS_BODY_OR_FORM_PARAM = "x-isBodyOrFormParam";
    static final String X_IS_BODY_PARAM = "x-isBodyParam";
    static final String X_MEDIA_DATA_TYPE = "x-mediaDataType";
    static final String X_DATA_TYPE = "x-dataType";
    static final String X_ENUM_VALUES = "x-enumValues";
    static final String X_MEDIA_IS_JSON = "x-mediaIsJson";
    static final String X_MIME_TYPES = "x-mimeTypes";
    static final String X_OPERATION_TYPE = "x-operationType";
    static final String X_PARAM_NAME_TYPE = "x-paramNameType";
    static final String X_PATH = "x-path";
    static final String X_RETURN_TYPE = "x-returnType";
    static final String X_STRICT_FIELDS = "x-strictFields";
    static final String X_UNKNOWN_MIME_TYPES = "x-unknownMimeTypes";
    static final String X_USE_MONAD_LOGGER = "x-useMonadLogger";
    static final String X_NEWTYPE = "x-newtype";
    static final String X_ENUM = "x-enum";


    protected ArrayList<Map<String,String>> unknownMimeTypes = new ArrayList<>();
    protected Map<String, Map<String,Object>> uniqueParamNameTypes = new HashMap<>();
    protected Map<String, Set<String>> modelMimeTypes = new HashMap<>();
    protected Map<String, String> knownMimeDataTypes = new HashMap<>();
    protected Set<String> typeNames = new HashSet<String>();

    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }
    public String getName() {
        return "haskell-http-client";
    }
    public String getHelp() {
        return "Generates a Haskell http-client library.";
    }


    final private static Pattern JSON_MIME_PATTERN = Pattern.compile("(?i)application/.*json(;.*)?");

    public HaskellHttpClientCodegen() {
        super();

        // override the mapping to keep the original mapping in Haskell
        specialCharReplacements.put("-", "Dash");
        specialCharReplacements.put(">", "GreaterThan");
        specialCharReplacements.put("<", "LessThan");

        // backslash and double quote need double the escapement for both Java and Haskell
        specialCharReplacements.remove("\\");
        specialCharReplacements.remove("\"");
        specialCharReplacements.put("\\\\", "Back_Slash");
        specialCharReplacements.put("\\\"", "Double_Quote");

        // set the output folder here
        outputFolder = "generated-code/haskell-http-client";

        embeddedTemplateDir = templateDir = "haskell-http-client";
        apiPackage = "API";
        modelPackage = "Model";

        // Haskell keywords and reserved function names, taken mostly from https://wiki.haskell.org/Keywords
        setReservedWordsLowerCase(
                Arrays.asList(
                        // Keywords
                        "as", "case", "of",
                        "class", "data", "family",
                        "default", "deriving",
                        "do", "forall", "foreign", "hiding",
                        "if", "then", "else",
                        "import", "infix", "infixl", "infixr",
                        "instance", "let", "in",
                        "mdo", "module", "newtype",
                        "proc", "qualified", "rec",
                        "type", "where", "pure", "return",
                        "Accept", "ContentType"
                )
        );

        additionalProperties.put("artifactId", artifactId);
        additionalProperties.put("artifactVersion", artifactVersion);

        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));
        supportingFiles.add(new SupportingFile("stack.mustache", "", "stack.yaml"));
        supportingFiles.add(new SupportingFile("Setup.mustache", "", "Setup.hs"));
        supportingFiles.add(new SupportingFile("gitignore.mustache", "", ".gitignore"));
        supportingFiles.add(new SupportingFile(".travis.yml", "", ".travis.yml"));
        supportingFiles.add(new SupportingFile("git_push.sh.mustache", "", "git_push.sh"));

        supportingFiles.add(new SupportingFile("tests/ApproxEq.mustache", "tests", "ApproxEq.hs"));
        supportingFiles.add(new SupportingFile("tests/Instances.mustache", "tests", "Instances.hs"));
        supportingFiles.add(new SupportingFile("tests/PropMime.mustache", "tests", "PropMime.hs"));
        supportingFiles.add(new SupportingFile("tests/Test.mustache", "tests", "Test.hs"));

        languageSpecificPrimitives = new HashSet<String>(
                Arrays.asList(
                        "Bool",
                        "String",
                        "Int",
                        "Integer",
                        "Float",
                        "Char",
                        "Double",
                        "List",
                        "FilePath",
                        "Text"
                )
        );

        typeMapping.clear();
        // prim
        typeMapping.put("boolean", "Bool");
        typeMapping.put("int", "Int");
        typeMapping.put("long", "Integer");
        typeMapping.put("short", "Int");
        typeMapping.put("char", "Char");
        typeMapping.put("float", "Float");
        typeMapping.put("double", "Double");
        typeMapping.put("number", "Double");
        typeMapping.put("integer", "Int");
        typeMapping.put("file", "FilePath");
        // lib
        typeMapping.put("string", "Text");
        typeMapping.put("UUID", "Text");
        typeMapping.put("any", "A.Value");
        typeMapping.put("set", "Set.Set");
        // newtype
        typeMapping.put("binary", "Binary");
        typeMapping.put("ByteArray", "ByteArray");
        typeMapping.put("date", "Date");
        typeMapping.put("DateTime", "DateTime");

        knownMimeDataTypes.put("application/json", "MimeJSON");
        knownMimeDataTypes.put("application/xml", "MimeXML");
        knownMimeDataTypes.put("application/x-www-form-urlencoded", "MimeFormUrlEncoded");
        knownMimeDataTypes.put("application/octet-stream", "MimeOctetStream");
        knownMimeDataTypes.put("multipart/form-data", "MimeMultipartFormData");
        knownMimeDataTypes.put("text/plain", "MimePlainText");
        knownMimeDataTypes.put("*/*", "MimeAny");

        importMapping.clear();

        cliOptions.add(CliOption.newString(CodegenConstants.MODEL_PACKAGE, CodegenConstants.MODEL_PACKAGE_DESC));
        cliOptions.add(CliOption.newString(CodegenConstants.API_PACKAGE, CodegenConstants.API_PACKAGE_DESC));

        cliOptions.add(CliOption.newBoolean(PROP_ALLOW_FROMJSON_NULLS, "allow JSON Null during model decoding from JSON").defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(CliOption.newBoolean(PROP_ALLOW_TOJSON_NULLS, "allow emitting JSON Null during model encoding to JSON").defaultValue(Boolean.FALSE.toString()));
        cliOptions.add(CliOption.newBoolean(PROP_GENERATE_LENSES, "Generate Lens optics for Models").defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(CliOption.newBoolean(PROP_GENERATE_MODEL_CONSTRUCTORS, "Generate smart constructors (only supply required fields) for models").defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(CliOption.newBoolean(PROP_GENERATE_ENUMS, "Generate specific datatypes for swagger enums").defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(CliOption.newBoolean(PROP_GENERATE_FORM_URLENCODED_INSTANCES, "Generate FromForm/ToForm instances for models that are used by operations that produce or consume application/x-www-form-urlencoded").defaultValue(Boolean.TRUE.toString()));
        cliOptions.add(CliOption.newBoolean(PROP_INLINE_MIME_TYPES, "Inline (hardcode) the content-type and accept parameters on operations, when there is only 1 option").defaultValue(Boolean.FALSE.toString()));


        cliOptions.add(CliOption.newString(PROP_MODEL_DERIVING, "Additional classes to include in the deriving() clause of Models"));
        cliOptions.add(CliOption.newBoolean(PROP_STRICT_FIELDS, "Add strictness annotations to all model fields").defaultValue((Boolean.TRUE.toString())));
        cliOptions.add(CliOption.newBoolean(PROP_USE_MONAD_LOGGER, "Use the monad-logger package to provide logging (if false, use the katip logging package)").defaultValue((Boolean.FALSE.toString())));

        cliOptions.add(CliOption.newString(PROP_DATETIME_FORMAT, "format string used to parse/render a datetime"));
        cliOptions.add(CliOption.newString(PROP_DATE_FORMAT, "format string used to parse/render a date").defaultValue(defaultDateFormat));

        cliOptions.add(CliOption.newBoolean(CodegenConstants.HIDE_GENERATION_TIMESTAMP, "hides the timestamp when files were generated").defaultValue(Boolean.TRUE.toString()));

    }

    public void setAllowFromJsonNulls(Boolean value) {
        additionalProperties.put(PROP_ALLOW_FROMJSON_NULLS, value);
    }

    public void setAllowToJsonNulls(Boolean value) {
        additionalProperties.put(PROP_ALLOW_TOJSON_NULLS, value);
    }

    public void setGenerateModelConstructors(Boolean value) {
        additionalProperties.put(PROP_GENERATE_MODEL_CONSTRUCTORS, value);
    }
    public void setGenerateEnums(Boolean value) {
        additionalProperties.put(PROP_GENERATE_ENUMS, value);
        genEnums = value;
    }
    public void setGenerateFormUrlEncodedInstances(Boolean value) {
        additionalProperties.put(PROP_GENERATE_FORM_URLENCODED_INSTANCES, value);
    }
    public void setInlineMimeTypes(Boolean value) {
        additionalProperties.put(PROP_INLINE_MIME_TYPES, value);
    }

    public void setGenerateLenses(Boolean value) {
        additionalProperties.put(PROP_GENERATE_LENSES, value);
    }

    public void setModelDeriving(String value) {
        if (StringUtils.isBlank(value)) {
            additionalProperties.remove(PROP_MODEL_DERIVING);
        } else {
            additionalProperties.put(PROP_MODEL_DERIVING, StringUtils.join(value.split(" "), ","));
        }
    }

    public void setDateTimeFormat(String value) {
        if (StringUtils.isBlank(value)) {
            additionalProperties.remove(PROP_DATETIME_FORMAT);
        } else {
            additionalProperties.put(PROP_DATETIME_FORMAT, value);
        }

    }

    public void setDateFormat(String value) {
        if (StringUtils.isBlank(value)) {
            additionalProperties.remove(PROP_DATE_FORMAT);
        } else {
            additionalProperties.put(PROP_DATE_FORMAT, value);
        }
    }

    public void setStrictFields(Boolean value) {
        additionalProperties.put(X_STRICT_FIELDS, value);
    }

    public void setUseMonadLogger(Boolean value) {
        additionalProperties.put(X_USE_MONAD_LOGGER, value);
        this.useMonadLogger = value;
    }

    @Override
    public void processOpts() {
        super.processOpts();
        // default HIDE_GENERATION_TIMESTAMP to true
        if (additionalProperties.containsKey(CodegenConstants.HIDE_GENERATION_TIMESTAMP)) {
            convertPropertyToBooleanAndWriteBack(CodegenConstants.HIDE_GENERATION_TIMESTAMP);
        } else {
            additionalProperties.put(CodegenConstants.HIDE_GENERATION_TIMESTAMP, true);
        }

        if (additionalProperties.containsKey(PROP_ALLOW_FROMJSON_NULLS)) {
            setAllowFromJsonNulls(convertPropertyToBoolean(PROP_ALLOW_FROMJSON_NULLS));
        } else {
            setAllowFromJsonNulls(true);
        }

        if (additionalProperties.containsKey(PROP_ALLOW_TOJSON_NULLS)) {
            setAllowToJsonNulls(convertPropertyToBoolean(PROP_ALLOW_TOJSON_NULLS));
        } else {
            setAllowToJsonNulls(false);
        }

        if (additionalProperties.containsKey(PROP_GENERATE_MODEL_CONSTRUCTORS)) {
            setGenerateModelConstructors(convertPropertyToBoolean(PROP_GENERATE_MODEL_CONSTRUCTORS));
        } else {
            setGenerateModelConstructors(true);
        }

        if (additionalProperties.containsKey(PROP_GENERATE_ENUMS)) {
            setGenerateEnums(convertPropertyToBoolean(PROP_GENERATE_ENUMS));
        } else {
            setGenerateEnums(true);
        }

        if (additionalProperties.containsKey(PROP_GENERATE_FORM_URLENCODED_INSTANCES)) {
            setGenerateFormUrlEncodedInstances(convertPropertyToBoolean(PROP_GENERATE_FORM_URLENCODED_INSTANCES));
        } else {
            setGenerateFormUrlEncodedInstances(true);
        }

        if (additionalProperties.containsKey(PROP_INLINE_MIME_TYPES)) {
            setInlineMimeTypes(convertPropertyToBoolean(PROP_INLINE_MIME_TYPES));
        } else {
            setInlineMimeTypes(false);
        }

        if (additionalProperties.containsKey(PROP_GENERATE_LENSES)) {
            setGenerateLenses(convertPropertyToBoolean(PROP_GENERATE_LENSES));
        } else {
            setGenerateLenses(true);
        }

        if (additionalProperties.containsKey(PROP_MODEL_DERIVING)) {
            setModelDeriving(additionalProperties.get(PROP_MODEL_DERIVING).toString());
        } else {
            setModelDeriving("");
        }

        if (additionalProperties.containsKey(PROP_DATETIME_FORMAT)) {
            setDateTimeFormat(additionalProperties.get(PROP_DATETIME_FORMAT).toString());
        } else {
            setDateTimeFormat(null); // default should be null
        }

        if (additionalProperties.containsKey(PROP_DATE_FORMAT)) {
            setDateFormat(additionalProperties.get(PROP_DATE_FORMAT).toString());
        } else {
            setDateFormat(defaultDateFormat);
        }

        if (additionalProperties.containsKey(PROP_STRICT_FIELDS)) {
            setStrictFields(convertPropertyToBoolean(PROP_STRICT_FIELDS));
        } else {
            setStrictFields(true);
        }
        if (additionalProperties.containsKey(PROP_USE_MONAD_LOGGER)) {
            setUseMonadLogger(convertPropertyToBoolean(PROP_USE_MONAD_LOGGER));
        } else {
            setUseMonadLogger(false);
        }

    }

    @Override
    public void preprocessOpenAPI(OpenAPI openAPI) {
        // From the title, compute a reasonable name for the package and the API
        String title = openAPI.getInfo().getTitle();

        // Drop any API suffix
        if (title == null) {
            title = "Swagger";
        } else {
            title = title.trim();
            if (title.toUpperCase().endsWith("API")) {
                title = title.substring(0, title.length() - 3);
            }
        }

        String[] words = title.split(" ");

        // The package name is made by appending the lowercased words of the title interspersed with dashes
        List<String> wordsLower = new ArrayList<String>();
        for (String word : words) {
            wordsLower.add(word.toLowerCase());
        }
        String cabalName = StringUtils.join(wordsLower, "-");
        String pathsName = StringUtils.join(wordsLower, "_");

        // The API name is made by appending the capitalized words of the title
        List<String> wordsCaps = new ArrayList<String>();
        for (String word : words) {
            wordsCaps.add(firstLetterToUpper(word));
        }
        String apiName = StringUtils.join(wordsCaps, "");

        // Set the filenames to write for the API

        // root
        supportingFiles.add(new SupportingFile("haskell-http-client.cabal.mustache", "", cabalName + ".cabal"));
        supportingFiles.add(new SupportingFile("swagger.mustache", "", "swagger.yaml"));

        // lib
        supportingFiles.add(new SupportingFile("TopLevel.mustache", "lib/", apiName + ".hs"));
        supportingFiles.add(new SupportingFile("Client.mustache", "lib/" + apiName, "Client.hs"));

        supportingFiles.add(new SupportingFile("API.mustache", "lib/" + apiName, "API.hs"));
        supportingFiles.add(new SupportingFile("Core.mustache", "lib/" + apiName, "Core.hs"));
        supportingFiles.add(new SupportingFile("Model.mustache", "lib/" + apiName, "Model.hs"));
        supportingFiles.add(new SupportingFile("MimeTypes.mustache", "lib/" + apiName, "MimeTypes.hs"));

        // logger
        supportingFiles.add(new SupportingFile(useMonadLogger ? "LoggingMonadLogger.mustache" : "LoggingKatip.mustache", "lib/" + apiName, "Logging.hs"));

        // modelTemplateFiles.put("API.mustache", ".hs");
        // apiTemplateFiles.put("Model.mustache", ".hs");

        // lens
        if ((boolean)additionalProperties.get(PROP_GENERATE_LENSES)) {
            supportingFiles.add(new SupportingFile("ModelLens.mustache", "lib/" + apiName, "ModelLens.hs"));
        }

        additionalProperties.put("title", apiName);
        additionalProperties.put("titleLower", firstLetterToLower(apiName));
        additionalProperties.put("package", cabalName);
        additionalProperties.put("pathsName", pathsName);
        additionalProperties.put("requestType", apiName + "Request");
        additionalProperties.put("configType", apiName + "Config");
        additionalProperties.put("openApiVersion", openAPI.getOpenapi());

        //copy input swagger to output folder
        try {
            String swaggerJson = Json.pretty(openAPI);
            FileUtils.writeStringToFile(new File(outputFolder + File.separator + "swagger.json"), swaggerJson);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

        super.preprocessOpenAPI(openAPI);
    }

    @Override
    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        OpenAPI openAPI = (OpenAPI)objs.get("openapi");
        if(openAPI != null) {
            try {
                objs.put("swagger-yaml", Yaml.mapper().writeValueAsString(openAPI));
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return super.postProcessSupportingFileData(objs);
    }

    @Override
    public String getTypeDeclaration(Schema propertySchema) {
        if (propertySchema instanceof ArraySchema) {
            Schema inner = ((ArraySchema) propertySchema).getItems();
            return String.format("[%s]", getTypeDeclaration(inner));
        } else if (propertySchema instanceof MapSchema && hasSchemaProperties(propertySchema)) {
            Schema inner = (Schema) propertySchema.getAdditionalProperties();
            return String.format("(Map.Map String %s)", getTypeDeclaration(inner));
        }
        return super.getTypeDeclaration(propertySchema);
    }

    @Override
    public String getSchemaType(Schema propertySchema) {
        String schemaType = super.getSchemaType(propertySchema);
        String type = null;
        if (typeMapping.containsKey(schemaType)) {
            return typeMapping.get(schemaType);
        } else if (languageSpecificPrimitives.contains(type)) {
            return type;
        } else if (schemaType.equals("object")) {
            return "A.Value";
        } else {
            return toModelName(schemaType);
        }
    }

    @Override
    public String toInstantiationType(Schema propertySchema) {
        if (propertySchema instanceof MapSchema && hasSchemaProperties(propertySchema)) {
            Schema additionalProperties2 = (Schema) propertySchema.getAdditionalProperties();
            String type = additionalProperties2.getType();
            if (null == type) {
                LOGGER.error("No Type defined for Additional Property " + additionalProperties2 + "\n" //
                        + "\tIn Property: " + propertySchema);
            }
            String inner = getSchemaType(additionalProperties2);
            return "(Map.Map Text " + inner + ")";
        } else if (propertySchema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) propertySchema;
            String inner = getSchemaType(arraySchema.getItems());
            return inner;
        } else {
            return null;
        }
    }
    @Override
    public CodegenOperation fromOperation(String resourcePath, String httpMethod, Operation operation, Map<String, Schema> schemas, OpenAPI openAPI) {
        CodegenOperation op = super.fromOperation(resourcePath, httpMethod, operation, schemas, openAPI);

        // prevent aliasing/sharing of operation.vendorExtensions reference
        op.vendorExtensions = new LinkedHashMap();

        String operationType = toTypeName("Op", op.operationId);
        op.vendorExtensions.put(X_OPERATION_TYPE, operationType);
        typeNames.add(operationType);

        op.vendorExtensions.put(X_HADDOCK_PATH, String.format("%s %s", op.httpMethod, op.path.replace("/", "\\/")));
        op.vendorExtensions.put(X_HAS_BODY_OR_FORM_PARAM, op.getHasBodyParam() || op.getHasFormParams());

        for (CodegenParameter param : op.allParams) {
            param.vendorExtensions = new LinkedHashMap(); // prevent aliasing/sharing
            param.vendorExtensions.put(X_OPERATION_TYPE, operationType);
            boolean isBodyParam = getBooleanValue(param, CodegenConstants.IS_BODY_PARAM_EXT_NAME);
            boolean isFormParam = getBooleanValue(param, CodegenConstants.IS_FORM_PARAM_EXT_NAME);
            param.vendorExtensions.put(X_IS_BODY_OR_FORM_PARAM, isBodyParam || isFormParam);
            if (!StringUtils.isBlank(param.collectionFormat)) {
                param.vendorExtensions.put(X_COLLECTION_FORMAT, mapCollectionFormat(param.collectionFormat));
            }
            if(!param.required) {
                op.vendorExtensions.put(X_HAS_OPTIONAL_PARAMS, true);
            }

            boolean isEnum = getBooleanValue(param, CodegenConstants.IS_ENUM_EXT_NAME);

            if (typeMapping.containsKey(param.dataType)
                    || getBooleanValue(param, CodegenConstants.IS_MAP_CONTAINER_EXT_NAME)
                    || getBooleanValue(param, CodegenConstants.IS_LIST_CONTAINER_EXT_NAME)
                    || getBooleanValue(param, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME)
                    || getBooleanValue(param, CodegenConstants.IS_FILE_EXT_NAME)
                    || isEnum) {

                String dataType = genEnums && isEnum ? param.datatypeWithEnum : param.dataType;

                String paramNameType = toDedupedName(toTypeName("Param", param.paramName), dataType, !isEnum);
                param.vendorExtensions.put(X_PARAM_NAME_TYPE, paramNameType);

                HashMap<String, Object> props = new HashMap<>();
                props.put(X_IS_BODY_PARAM, getBooleanValue(param, CodegenConstants.IS_BODY_PARAM_EXT_NAME));
                addToUniques(X_NEWTYPE, paramNameType, dataType, props);
            }
        }

        processPathExpr(op);

        processProducesConsumes(op);

        processReturnType(op);

        return op;
    }

    public List<CodegenSecurity> fromSecurity(Map<String, SecurityScheme> schemes) {
        List<CodegenSecurity> secs = super.fromSecurity(schemes);
        for(CodegenSecurity sec : secs) {
           String prefix = "";
           if(getBooleanValue(sec, CodegenConstants.IS_BASIC_EXT_NAME)) {
               prefix = "AuthBasic";
           }
           if(getBooleanValue(sec, CodegenConstants.IS_API_KEY_EXT_NAME)) {
               prefix = "AuthApiKey";
           }
           if(getBooleanValue(sec, CodegenConstants.IS_OAUTH_EXT_NAME)) {
               prefix = "AuthOAuth";
           }
           sec.name = prefix + toTypeName("",sec.name);
        }
        return secs;
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> ret = super.postProcessOperations(objs);

        HashMap<String, Object> pathOps = (HashMap<String, Object>)ret.get("operations");
        ArrayList<CodegenOperation> ops = (ArrayList<CodegenOperation>)pathOps.get("operation");
        if(ops.size() > 0) {
            ops.get(0).vendorExtensions.put(X_HAS_NEW_TAG, true);
        }

        updateGlobalAdditionalProps();
        return ret;
    }

    @Override
    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
        updateGlobalAdditionalProps();
        return super.postProcessAllModels(objs);
    }

    public void updateGlobalAdditionalProps() {
        additionalProperties.put(X_HAS_UNKNOWN_MIME_TYPES, !unknownMimeTypes.isEmpty());

        Collections.sort(unknownMimeTypes, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                return o1.get(MEDIA_TYPE).compareTo(o2.get(MEDIA_TYPE));
            }
        });
        additionalProperties.put(X_UNKNOWN_MIME_TYPES, unknownMimeTypes);

        ArrayList<Map<String,Object>> params = new ArrayList<>(uniqueParamNameTypes.values());
        Collections.sort(params, new Comparator<Map<String,Object>>() {
            @Override
            public int compare(Map<String,Object> o1, Map<String,Object> o2) {
                return
                        ((String) o1.get(X_PARAM_NAME_TYPE))
                                .compareTo(
                                        (String) o2.get(X_PARAM_NAME_TYPE));
            }
        });
        additionalProperties.put(X_ALL_UNIQUE_PARAMS, params);
    }

    @Override
    public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {
        for (Object object : allModels) {
            HashMap<String, Object> h = (HashMap<String, Object>) object;
            CodegenModel codegenModel = (CodegenModel) h.get("model");
            if (modelMimeTypes.containsKey(codegenModel.classname)) {
                Set<String> mimeTypes = modelMimeTypes.get(codegenModel.classname);
                codegenModel.vendorExtensions.put(X_MIME_TYPES, mimeTypes);
                if ((boolean)additionalProperties.get(PROP_GENERATE_FORM_URLENCODED_INSTANCES) && mimeTypes.contains("MimeFormUrlEncoded")) {
                    Boolean hasMimeFormUrlEncoded = true;
                    for (CodegenProperty codegenProperty : codegenModel.vars) {
                        boolean isPrimitiveType = getBooleanValue(codegenProperty, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME);
                        boolean isString = getBooleanValue(codegenProperty, CodegenConstants.IS_STRING_EXT_NAME);
                        boolean isDate = getBooleanValue(codegenProperty, CodegenConstants.IS_DATE_EXT_NAME);
                        boolean isDateTime = getBooleanValue(codegenProperty, CodegenConstants.IS_DATE_TIME_EXT_NAME);
                        if (!(isPrimitiveType || isString || isDate || isDateTime)) {
                            hasMimeFormUrlEncoded = false;
                        }
                    }
                    if (hasMimeFormUrlEncoded) {
                        codegenModel.vendorExtensions.put(X_HAS_MIME_FORM_URL_ENCODED, true);
                    }
                }
            }

        }
        return objs;
    }

    @Override
    public CodegenModel fromModel(String name, Schema schema, Map<String, Schema> allSchemas) {
        CodegenModel model = super.fromModel(name, schema, allSchemas);

        while (typeNames.contains(model.classname)) {
            model.classname = generateNextName(model.classname);
        }
        typeNames.add(model.classname);

        // From the model name, compute the prefix for the fields.
        String prefix = StringUtils.uncapitalize(model.classname);
        for (CodegenProperty prop : model.vars) {
            prop.name = toVarName(prefix, prop.name);
        }
        return model;
    }

    @Override
    public String escapeReservedWord(String name) {
        if (this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "_" + name;
    }

    @Override
    public String escapeQuotationMark(String input) {
        // remove " to avoid code injection
        return input.replace("\"", "");
    }

    @Override
    public String escapeUnsafeCharacters(String input) {
        return input.replace("{-", "{_-").replace("-}", "-_}");
    }

    @Override
    public boolean isDataTypeFile(String dataType) {
        return dataType != null && dataType.equals("FilePath");
    }

    @Override
    public boolean isDataTypeBinary(final String dataType) {
        return dataType != null && dataType.equals("B.ByteString");
    }

    private void processReturnType(CodegenOperation codegenOperation) {
        String returnType = codegenOperation.returnType;
        if (returnType == null || returnType.equals("null")) {
            boolean hasProduces = getBooleanValue(codegenOperation, CodegenConstants.HAS_PRODUCES_EXT_NAME);
            if(hasProduces) {
                returnType = "res";
                codegenOperation.vendorExtensions.put(X_HAS_UNKNOWN_RETURN, true);
            } else {
                returnType = "NoContent";
                if(!codegenOperation.vendorExtensions.containsKey(X_INLINE_ACCEPT)) {
                    SetNoContent(codegenOperation, X_INLINE_ACCEPT);
                }
            }
        }
        if (returnType.indexOf(" ") >= 0) {
            returnType = "(" + returnType + ")";
        }
        codegenOperation.vendorExtensions.put(X_RETURN_TYPE, returnType);
    }

    private void processProducesConsumes(CodegenOperation codegenOperation) {
        if (!(Boolean) codegenOperation.vendorExtensions.get(X_HAS_BODY_OR_FORM_PARAM)) {
            SetNoContent(codegenOperation, X_INLINE_CONTENT_TYPE);
        }
        boolean hasConsumes = getBooleanValue(codegenOperation, CodegenConstants.HAS_CONSUMES_EXT_NAME);
        if (hasConsumes) {
            for (Map<String, String> m : codegenOperation.consumes) {
                processMediaType(codegenOperation, m);
                processInlineConsumesContentType(codegenOperation, m);

            }
            if (isMultipartOperation(codegenOperation.consumes)) {
                codegenOperation.getVendorExtensions().put(CodegenConstants.IS_MULTIPART_EXT_NAME, Boolean.TRUE);
            }
        }
        boolean hasProduces = getBooleanValue(codegenOperation, CodegenConstants.HAS_PRODUCES_EXT_NAME);
        if (hasProduces) {
            for (Map<String, String> m : codegenOperation.produces) {
                processMediaType(codegenOperation,m);
                processInlineProducesContentType(codegenOperation, m);
            }
        }
    }

    private void processInlineConsumesContentType(CodegenOperation op, Map<String, String> m) {
        if (op.vendorExtensions.containsKey(X_INLINE_CONTENT_TYPE)) return;
        if ((boolean) additionalProperties.get(PROP_INLINE_MIME_TYPES)
                && op.consumes.size() == 1) {
            op.vendorExtensions.put(X_INLINE_CONTENT_TYPE, m);
            for (CodegenParameter param : op.allParams) {
                boolean isBodyParam = getBooleanValue(param, CodegenConstants.IS_BODY_PARAM_EXT_NAME);
                if (isBodyParam && param.required) {
                    param.vendorExtensions.put(X_INLINE_CONTENT_TYPE, m);
                }
            }
        }
    }

    private void processInlineProducesContentType(CodegenOperation op, Map<String, String> m) {
        if ((boolean) additionalProperties.get(PROP_INLINE_MIME_TYPES)
                && op.produces.size() == 1) {
            op.vendorExtensions.put(X_INLINE_ACCEPT, m);
        }
    }

    private void SetNoContent(CodegenOperation op, String inlineExtentionName) {
        Map<String, String> m = new HashMap<>();
        m.put(X_MEDIA_DATA_TYPE, MIME_NO_CONTENT);
        op.vendorExtensions.put(inlineExtentionName, m);
    }

    private String toDedupedName(String paramNameType, String dataType, Boolean appendDataType) {
        if (appendDataType
                && uniqueParamNameTypes.containsKey(paramNameType)
                && !isDuplicate(paramNameType, dataType)) {
            paramNameType = paramNameType + dataType;
        }

        while (typeNames.contains(paramNameType)) {
            if (isDuplicate(paramNameType, dataType)) {
                break;
            }
            paramNameType = generateNextName(paramNameType);
        }

        typeNames.add(paramNameType);
        return paramNameType;
    }

    public Boolean isDuplicate(String paramNameType, String dataType) {
        Map<String, Object> lastParam = this.uniqueParamNameTypes.get(paramNameType);
        if (lastParam != null) {
            String comparisonKey = lastParam.containsKey(X_ENUM) ? X_ENUM_VALUES : X_DATA_TYPE;
            String lastParamDataType = (String) lastParam.get(comparisonKey);
            if (lastParamDataType != null && lastParamDataType.equals(dataType)) {
                return true;
            }
        }
        return false;
    }

    private Pair<Boolean, String> isDuplicateEnumValues(String enumValues) {
        for (Map<String, Object> vs : uniqueParamNameTypes.values()) {
            if (enumValues.equals(vs.get(X_ENUM_VALUES))) {
                return Pair.of(true, (String) vs.get(X_PARAM_NAME_TYPE));
            }
        }
        return Pair.of(false, null);
    }


    private void addToUniques(String xGroup, String paramNameType, String dataType, Map<String, Object> props) {
        HashMap<String, Object> m = new HashMap<>();
        m.put(X_PARAM_NAME_TYPE, paramNameType);
        m.put(X_DATA_TYPE, dataType);
        m.put(xGroup, true);
        m.putAll(props);
        uniqueParamNameTypes.put(paramNameType, m);
    }

    private void addEnumToUniques(String paramNameType, String datatype, String enumValues, Map<String, Object> allowableValues, String description) {
        HashMap<String, Object> props = new HashMap<>();
        props.put("allowableValues", allowableValues);
        if(StringUtils.isNotBlank(description)) {
            props.put("description", description);
        }
        props.put(X_ENUM_VALUES, enumValues);
        addToUniques(X_ENUM, paramNameType, datatype, props);
        additionalProperties.put(X_HAS_ENUM_SECTION, true);
    }


    // build the parameterized path segments, according to pathParams
    private void processPathExpr(CodegenOperation op) {
        String xPath = "[\"" + escapeText(op.path) + "\"]";
        if (op.getHasPathParams()) {
            for (CodegenParameter param : op.pathParams) {
                xPath = xPath.replaceAll("\\{" + param.baseName + "\\}", "\",toPath " + param.paramName + ",\"");
            }
            xPath = xPath.replaceAll(",\"\",", ",");
            xPath = xPath.replaceAll("\"\",", ",");
            xPath = xPath.replaceAll(",\"\"", ",");
            xPath = xPath.replaceAll("^\\[,", "[");
            xPath = xPath.replaceAll(",\\]$", "]");
        }
        op.vendorExtensions.put(X_PATH, xPath);
    }


    private void processMediaType(CodegenOperation op, Map<String, String> m) {
        String mediaType = m.get(MEDIA_TYPE);

        if (StringUtils.isBlank(mediaType)) return;

        String mimeType = getMimeDataType(mediaType);
        typeNames.add(mimeType);
        m.put(X_MEDIA_DATA_TYPE, mimeType);
        if (isJsonMimeType(mediaType)) {
            m.put(X_MEDIA_IS_JSON, "true");
        }

        if (!knownMimeDataTypes.containsValue(mimeType) && !unknownMimeTypesContainsType(mimeType)) {
            unknownMimeTypes.add(m);
        }
        for (CodegenParameter param : op.allParams) {
            boolean isBodyParam = getBooleanValue(param, CodegenConstants.IS_BODY_PARAM_EXT_NAME);
            boolean isFormParam = getBooleanValue(param, CodegenConstants.IS_FORM_PARAM_EXT_NAME);
            boolean isPrimitiveType = getBooleanValue(param, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME);
            boolean isListContainer = getBooleanValue(param, CodegenConstants.IS_LIST_CONTAINER_EXT_NAME);
            boolean isMapContainer = getBooleanValue(param, CodegenConstants.IS_MAP_CONTAINER_EXT_NAME);

            if (isBodyParam || isFormParam && (!isPrimitiveType && !isListContainer && !isMapContainer)) {
                Set<String> mimeTypes = modelMimeTypes.containsKey(param.dataType) ? modelMimeTypes.get(param.dataType) : new HashSet();
                mimeTypes.add(mimeType);
                modelMimeTypes.put(param.dataType, mimeTypes);
            }
        }
    }

    private Boolean unknownMimeTypesContainsType(String mimeType) {
        for(Map<String,String> m : unknownMimeTypes) {
            String mimeType0 = m.get(X_MEDIA_DATA_TYPE);
            if(mimeType0 != null && mimeType0.equals(mimeType)) {
                return true;
            }
        }

        return false;
    }

    public String firstLetterToUpper(String word) {
        if (word.length() == 0) {
            return word;
        } else if (word.length() == 1) {
            return word.substring(0, 1).toUpperCase();
        } else {
            return word.substring(0, 1).toUpperCase() + word.substring(1);
        }
    }

    public String firstLetterToLower(String word) {
        if (word.length() == 0) {
            return word;
        } else if (word.length() == 1) {
            return word.substring(0, 1).toLowerCase();
        } else {
            return word.substring(0, 1).toLowerCase() + word.substring(1);
        }
    }

    private String mapCollectionFormat(String collectionFormat) {
        switch (collectionFormat) {
            case "csv":
                return "CommaSeparated";
            case "tsv":
                return "TabSeparated";
            case "ssv":
                return "SpaceSeparated";
            case "pipes":
                return "PipeSeparated";
            case "multi":
                return "MultiParamArray";
            default:
                throw new UnsupportedOperationException();
        }
    }

    private String getMimeDataType(String mimeType) {
        if (StringUtils.isBlank(mimeType)) {
            return MIME_NO_CONTENT;
        }
        if (knownMimeDataTypes.containsKey(mimeType)) {
            return knownMimeDataTypes.get(mimeType);
        }
        String shortenedName = mimeType.replaceFirst("application/","");
        return "Mime" + toTypeName("", shortenedName);
    }

    private static String generateNextName(String name) {
        Pattern pattern = Pattern.compile("\\d+\\z");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            String numStr = matcher.group();
            int num = Integer.parseInt(numStr) + 1;
            return name.substring(0, name.length() - numStr.length()) + num;
        } else {
            return name + "2";
        }
    }
    private static boolean isMultipartOperation(List<Map<String, String>> consumes) {
        for(Map<String, String> consume : consumes) {
            if (consume != null) {
                if ("multipart/form-data".equals(consume.get(MEDIA_TYPE))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toVarName(String name) {
        return toVarName("", name);
    }
    public String toVarName(String prefix, String name) {
        Boolean hasPrefix = !StringUtils.isBlank(prefix);
        name = underscore(sanitizeName(name.replaceAll("-", "_")));
        name = camelize(name, !hasPrefix);
        if(hasPrefix) {
            return prefix + name;
        } else {
            if (name.matches("^\\d.*"))
                name = escapeReservedWord(name);
            if (isReservedWord(name))
                name = escapeReservedWord(name);
            return name;
        }
    }

    @Override
    public String toParamName(String name) {
        return toVarName(name);
    }

    @Override
    public String toModelName(String name) {
        return toTypeName("Model", name);
    }
    @Override
    public String toModelFilename(String name) {
        return toTypeName("Model", name);
    }
    public String toTypeName(String prefix, String name) {
        name =  escapeIdentifier(prefix, camelize(sanitizeName(name)));
        return name;
    }
    @Override
    public String toOperationId(String operationId) {
        if (StringUtils.isEmpty(operationId)) {
            throw new RuntimeException("Empty method/operation name (operationId) not allowed");
        }
        operationId = escapeIdentifier("op",camelize(sanitizeName(operationId), true));
        String uniqueName = operationId;
        String uniqueNameType = toTypeName("Op", operationId);
        while (typeNames.contains(uniqueNameType)) {
            uniqueName = generateNextName(uniqueName);
            uniqueNameType = toTypeName("Op", uniqueName);
        }
        typeNames.add(uniqueNameType);
        if(!operationId.equals(uniqueName)) {
            LOGGER.warn("generated unique operationId `" + uniqueName + "`");
        }
        return uniqueName;
    }
    public String escapeIdentifier(String prefix, String name) {
        if(StringUtils.isBlank(prefix)) return name;

        if (isReservedWord(name)) {
            name = prefix + name;
        }
        if (name.matches("^\\d.*")) {
            name = prefix + name; // e.g. 200Response => Model200Response (after camelize)
        }
        if (languageSpecificPrimitives.contains(name)) {
            name = prefix + name;
        }
        if (typeMapping.containsValue(name)) {
            name = prefix + name;
        }
        return name;
    }
    static boolean isJsonMimeType(String mime) {
        return mime != null && JSON_MIME_PATTERN.matcher(mime).matches();
    }

    @Override
    public String toDefaultValue(Schema propertySchema) {
        if (propertySchema instanceof StringSchema) {
            if (propertySchema.getDefault() != null) {
                return String.format("\"%s\"", escapeText(propertySchema.getDefault().toString()));
            }
        } else if (propertySchema instanceof BooleanSchema) {
            if (propertySchema.getDefault() != null) {
                if (propertySchema.getDefault().toString().equalsIgnoreCase("false")) {
                    return "False";
                } else {
                    return "True";
                }
            }
        } else if (propertySchema.getDefault() != null) {
            if (propertySchema.getDefault() != null) {
                return propertySchema.getDefault().toString();
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> postProcessModels(Map<String, Object> objs) {
        List<Object> models = (List<Object>) objs.get("models");
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");
            final boolean isEnum = getBooleanValue(cm, IS_ENUM_EXT_NAME);
            cm.getVendorExtensions().put(IS_ENUM_EXT_NAME, genEnums && isEnum);

            boolean isAlias = ExtensionHelper.getBooleanValue(cm, CodegenConstants.IS_ALIAS_EXT_NAME);

            if(isAlias) {
                cm.vendorExtensions.put(X_DATA_TYPE, cm.dataType);
            }
            for (CodegenProperty var : cm.vars) {
                String datatype = genEnums && !StringUtils.isBlank(var.datatypeWithEnum)
                        ? var.datatypeWithEnum
                        : var.datatype;
                var.vendorExtensions.put(X_DATA_TYPE, datatype);
            }
        }
        return postProcessModelsEnum(objs);
    }

    @Override
    public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
        Map<String, Object> objsEnum = super.postProcessModelsEnum(objs);
        if (genEnums) {
            List<Object> models = (List<Object>) objsEnum.get("models");
            for (Object _mo : models) {
                Map<String, Object> mo = (Map<String, Object>) _mo;
                CodegenModel cm = (CodegenModel) mo.get("model");
                final boolean isEnum = getBooleanValue(cm, IS_ENUM_EXT_NAME);
                if (isEnum && cm.allowableValues != null) {
                    updateAllowableValuesNames(cm.classname, cm.allowableValues);
                    addEnumToUniques(cm.classname, cm.dataType, cm.allowableValues.values().toString(), cm.allowableValues, cm.description);
                }
            }
        }
        return objsEnum;
    }

    @Override
    protected void updateDataTypeWithEnumForMap(CodegenProperty property) {
        CodegenProperty baseItem = property.items;
        boolean isMapContainer = getBooleanValue(property, CodegenConstants.IS_MAP_CONTAINER_EXT_NAME);
        boolean isListContainer = getBooleanValue(property, CodegenConstants.IS_LIST_CONTAINER_EXT_NAME);
        while (baseItem != null && (isMapContainer || isListContainer)) {
            baseItem = baseItem.items;
        }
        if (baseItem != null) {

            // this replacement is/needs to be language-specific
            property.datatypeWithEnum = property.datatypeWithEnum.replace(baseItem.baseType + ")", toEnumName(baseItem) + ")");

            property.enumName = toEnumName(property);
            if (property.defaultValue != null) {
                property.defaultValue = property.defaultValue.replace(", " + property.items.baseType, ", " + toEnumName(property.items));
            }
        }
    }

    @Override
    public String toEnumName(CodegenProperty var) {
        if (!genEnums) return super.toEnumName(var);
        if (var.items != null && getBooleanValue(var.items, IS_ENUM_EXT_NAME)) {
            return toEnumName(var.items);
        }
        String paramNameType = "E'" + toTypeName("", var.name);
        String enumValues = var._enum.toString();

        Pair<Boolean, String> duplicateEnum = isDuplicateEnumValues(enumValues);
        if (duplicateEnum.getLeft()) {
            paramNameType = duplicateEnum.getRight();
        } else {
            paramNameType = toDedupedName(paramNameType, enumValues, false);
            var.datatypeWithEnum = paramNameType;
            updateCodegenPropertyEnum(var);
            addEnumToUniques(paramNameType, var.datatype, enumValues, var.allowableValues, var.description);
        }

        return paramNameType;
    }

    public void updateCodegenPropertyEnum(CodegenProperty var) {
        ModelUtils.updateCodegenPropertyEnum(var);
        if (!genEnums) return;
        updateCodegenPropertyEnumValues(var, var.datatypeWithEnum);
    }

    public void updateCodegenPropertyEnumValues(CodegenProperty var, String paramNameType) {
        if (var.items != null && var.items.allowableValues != null) {
            updateCodegenPropertyEnumValues(var.items, var.items.datatypeWithEnum);
            return;
        }
        boolean isEnum = getBooleanValue(var, IS_ENUM_EXT_NAME);
        if(isEnum && var.allowableValues != null) {
            updateAllowableValuesNames(paramNameType, var.allowableValues);
        }
    }

    private void updateAllowableValuesNames(String paramNameType, Map<String, Object> allowableValues) {
        if (allowableValues == null) {
            return;
        }
        for (Map<String, String> enumVar : (List<Map<String, String>>) allowableValues.get("enumVars")) {
            enumVar.put("name", paramNameType + enumVar.get("name"));
        }
    }

    @Override
    public String toEnumVarName(String value, String datatype) {
        if (!genEnums) return super.toEnumVarName(value, datatype);

        List<String> num = new ArrayList<>(Arrays.asList("integer","int","double","long","float"));
        if (value.length() == 0) {
            return "'Empty";
        }

        // for symbol, e.g. $, #
        if (getSymbolName(value) != null) {
            return "'" + StringUtils.capitalize(sanitizeName(getSymbolName(value)));
        }

        // number
        if (num.contains(datatype.toLowerCase())) {
            String varName = "Num" + value;
            varName = varName.replaceAll("-", "Minus_");
            varName = varName.replaceAll("\\+", "Plus_");
            varName = varName.replaceAll("\\.", "_Dot_");
            return "'" + StringUtils.capitalize(sanitizeName(varName));
        }

        return "'" + StringUtils.capitalize(sanitizeName(value));
    }

    @Override
    public String toEnumValue(String value, String datatype) {
        List<String> num = new ArrayList<>(Arrays.asList("integer","int","double","long","float"));
        if(num.contains(datatype.toLowerCase())) {
            return value;
        } else {
            return "\"" + escapeText(value) + "\"";
        }
    }

    // override with any special text escaping logic
    @SuppressWarnings("static-method")
    public String escapeText(String input) {
        if (input == null) {
            return input;
        }

        // remove \t, \n, \r
        // replace \ with \\
        // replace " with \"
        // outter unescape to retain the original multi-byte characters
        // finally escalate characters avoiding code injection
        return escapeUnsafeCharacters(
                StringEscapeUtils.unescapeJava(
                        StringEscapeUtils.escapeJava(input)
                                .replace("\\/", "/"))
                        .replaceAll("[\\t\\n\\r]"," ")
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\""));
    }
}
