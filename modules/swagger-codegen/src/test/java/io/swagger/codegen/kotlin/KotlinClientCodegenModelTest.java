package io.swagger.codegen.kotlin;

import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.languages.KotlinClientCodegen;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.swagger.codegen.languages.helpers.ExtensionHelper.getBooleanValue;

@SuppressWarnings("static-method")
public class KotlinClientCodegenModelTest {

    private Schema getArrayTestModel() {
        return new Schema()
                .type("object")
                .description("a sample model")
                .addProperties("id", new IntegerSchema().format(SchemaTypeUtil.INTEGER64_FORMAT))
                .addProperties("examples", new ArraySchema().items(new StringSchema()))
                .addRequiredItem("id");
    }

    private Schema getSimpleModel() {
        return new Schema()
                .type("object")
                .description("a sample model")
                .addProperties("id", new IntegerSchema().format(SchemaTypeUtil.INTEGER64_FORMAT))
                .addProperties("name", new StringSchema())
                .addProperties("createdAt", new DateTimeSchema())
                .addRequiredItem("id")
                .addRequiredItem("name");
    }

    private Schema getMapModel() {
        return new Schema()
                .type("object")
                .description("a sample model")
                .addProperties("mapping", new MapSchema()
                        .additionalProperties(new StringSchema()))
                .addRequiredItem("id");
    }

    private Schema getComplexModel() {
        return new Schema()
                .type("object")
                .description("a sample model")
                .addProperties("child", new Schema().$ref("#/components/schemas/Child"));
    }

    @Test(enabled = false,description = "convert a simple model")
    public void simpleModelTest() {
        final Schema model = getSimpleModel();
        final DefaultCodegen codegen = new KotlinClientCodegen();

        final CodegenModel cm = codegen.fromModel("sample", model);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 3);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "id");
        Assert.assertEquals(property1.datatype, "kotlin.Long");
        Assert.assertEquals(property1.name, "id");
        Assert.assertEquals(property1.defaultValue, "null");
        Assert.assertEquals(property1.baseType, "kotlin.Long");
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertTrue(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME));
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));

        final CodegenProperty property2 = cm.vars.get(1);
        Assert.assertEquals(property2.baseName, "name");
        Assert.assertEquals(property2.datatype, "kotlin.String");
        Assert.assertEquals(property2.name, "name");
        Assert.assertEquals(property2.defaultValue, "null");
        Assert.assertEquals(property2.baseType, "kotlin.String");
        Assert.assertTrue(getBooleanValue(property2, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertTrue(property2.required);
        Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME));
        Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));

        final CodegenProperty property3 = cm.vars.get(2);
        Assert.assertEquals(property3.baseName, "createdAt");
        Assert.assertEquals(property3.datatype, "java.time.LocalDateTime");
        Assert.assertEquals(property3.name, "createdAt");
        Assert.assertEquals(property3.defaultValue, "null");
        Assert.assertEquals(property3.baseType, "java.time.LocalDateTime");
        Assert.assertFalse(getBooleanValue(property3, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertFalse(property3.required);
        Assert.assertTrue(getBooleanValue(property3, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
    }

    @Test(enabled = false, description = "convert a model with array property to default kotlin.Array")
    public void arrayPropertyTest() {
        final Schema model = getArrayTestModel();

        final DefaultCodegen codegen = new KotlinClientCodegen();
        final CodegenModel generated = codegen.fromModel("sample", model);

        Assert.assertEquals(generated.name, "sample");
        Assert.assertEquals(generated.classname, "Sample");
        Assert.assertEquals(generated.description, "a sample model");
        Assert.assertEquals(generated.vars.size(), 2);

        final CodegenProperty property = generated.vars.get(1);
        Assert.assertEquals(property.baseName, "examples");
        Assert.assertEquals(property.getter, "getExamples");
        Assert.assertEquals(property.setter, "setExamples");
        Assert.assertEquals(property.datatype, "kotlin.Array<kotlin.String>");
        Assert.assertEquals(property.name, "examples");
        Assert.assertEquals(property.defaultValue, "null");
        Assert.assertEquals(property.baseType, "kotlin.Array");
        Assert.assertEquals(property.containerType, "array");
        Assert.assertFalse(property.required);
        Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_CONTAINER_EXT_NAME));
    }
    @Test(enabled = false, description = "convert a model with a map property")
    public void mapPropertyTest() {
        final Schema model = getMapModel();
        final DefaultCodegen codegen = new KotlinClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "mapping");
        Assert.assertEquals(property1.datatype, "kotlin.collections.Map<kotlin.String, kotlin.String>");
        Assert.assertEquals(property1.name, "mapping");
        Assert.assertEquals(property1.baseType, "kotlin.collections.Map");
        Assert.assertEquals(property1.containerType, "map");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_CONTAINER_EXT_NAME));
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME));
    }

    @Test(enabled = false, description = "convert a model with complex property")
    public void complexPropertyTest() {
        final Schema model = getComplexModel();
        final DefaultCodegen codegen = new KotlinClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "child");
        Assert.assertEquals(property1.datatype, "Child");
        Assert.assertEquals(property1.name, "child");
        Assert.assertEquals(property1.baseType, "Child");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
    }

    @DataProvider(name = "modelNames")
    public static Object[][] modelNames(){
        return new Object[][] {
                { "TestNs.TestClass" , new ModelNameTest("TestNs.TestClass", "TestNsTestClass") },
                { "$", new ModelNameTest("$", "Dollar") },
                { "for", new ModelNameTest("`for`","`for`")},
                { "One<Two", new ModelNameTest("One<Two", "OneLess_ThanTwo")},
                { "this is a test", new ModelNameTest("this is a test", "This_is_a_test")}
        };
    }

    @Test(enabled = false, dataProvider = "modelNames", description = "sanitize model names")
    public void sanitizeModelNames(final String name, final ModelNameTest testCase) {
        final Schema schema = getComplexModel();
        final DefaultCodegen codegen = new KotlinClientCodegen();
        final CodegenModel cm = codegen.fromModel(name, schema);

        Assert.assertEquals(cm.name, testCase.expectedName);
        Assert.assertEquals(cm.classname, testCase.expectedClassName);
    }

    private static class ModelNameTest {
        private String expectedName;
        private String expectedClassName;

        private ModelNameTest(String nameAndClass) {
            this.expectedName = nameAndClass;
            this.expectedClassName = nameAndClass;
        }

        private ModelNameTest(String expectedName, String expectedClassName) {
            this.expectedName = expectedName;
            this.expectedClassName = expectedClassName;
        }
    }
}

