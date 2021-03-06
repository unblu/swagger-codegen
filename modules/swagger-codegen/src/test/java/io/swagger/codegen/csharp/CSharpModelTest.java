package io.swagger.codegen.csharp;

import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.languages.CSharpClientCodegen;

import com.google.common.collect.Sets;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.swagger.codegen.languages.helpers.ExtensionHelper.getBooleanValue;

@SuppressWarnings("static-method")
public class CSharpModelTest {

    @Test(enabled = false, description = "convert a model with array property to default List<T>")
    public void arrayPropertyTest() {
        final Schema schema = getArrayTestSchema();

        final DefaultCodegen codegen = new CSharpClientCodegen();
        final CodegenModel generated = codegen.fromModel("sample", schema);

        Assert.assertEquals(generated.name, "sample");
        Assert.assertEquals(generated.classname, "Sample");
        Assert.assertEquals(generated.description, "a sample model");
        Assert.assertEquals(generated.vars.size(), 2);

        final CodegenProperty property = generated.vars.get(1);
        Assert.assertEquals(property.baseName, "examples");
        Assert.assertEquals(property.getter, "getExamples");
        Assert.assertEquals(property.setter, "setExamples");
        Assert.assertEquals(property.datatype, "List<string>");
        Assert.assertEquals(property.name, "Examples");
        Assert.assertEquals(property.defaultValue, null);
        Assert.assertEquals(property.baseType, "List");
        Assert.assertEquals(property.containerType, "array");
        Assert.assertFalse(property.required);
        Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_CONTAINER_EXT_NAME));
    }

    @Test(enabled = false, description = "convert a model with array property to Collection<T>")
    public void arrayPropertyCollectionOptionTest() {
        final Schema schema = getArrayTestSchema();

        final CSharpClientCodegen codegen = new CSharpClientCodegen();
        codegen.setUseCollection(true);

        final CodegenModel generated = codegen.fromModel("sample", schema);

        Assert.assertEquals(generated.name, "sample");
        Assert.assertEquals(generated.vars.size(), 2);

        final CodegenProperty property = generated.vars.get(1);
        Assert.assertEquals(property.baseName, "examples");
        Assert.assertEquals(property.name, "Examples");
        Assert.assertEquals(property.defaultValue, null);
        Assert.assertEquals(property.datatype, "Collection<string>");
        Assert.assertEquals(property.baseType, "Collection");
        Assert.assertEquals(property.containerType, "array");
        Assert.assertFalse(property.required);
        Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_CONTAINER_EXT_NAME));
    }

    @Test(enabled = false, description = "convert a model with array property to Collection<T>")
    public void arrayPropertyICollectionOptionTest() {
        final Schema schema = getArrayTestSchema();

        final CSharpClientCodegen codegen = new CSharpClientCodegen();
        codegen.setUseCollection(true);
        codegen.setReturnICollection(true);

        final CodegenModel generated = codegen.fromModel("sample", schema);

        Assert.assertEquals(generated.name, "sample");
        Assert.assertEquals(generated.vars.size(), 2);

        final CodegenProperty property = generated.vars.get(1);
        Assert.assertEquals(property.baseName, "examples");
        Assert.assertEquals(property.name, "Examples");
        Assert.assertEquals(property.datatype, "Collection<string>",
                "returnICollection option should not modify property datatype");
        Assert.assertEquals(property.defaultValue, null);
        Assert.assertEquals(property.baseType, "Collection",
                "returnICollection option should not modify property baseType");
        Assert.assertEquals(property.containerType, "array");
        Assert.assertFalse(property.required);
        Assert.assertTrue(getBooleanValue(property, CodegenConstants.IS_CONTAINER_EXT_NAME));
    }

    private Schema getArrayTestSchema() {
        return new Schema()
                .description("a sample model")
                .addProperties("id", new IntegerSchema().format(SchemaTypeUtil.INTEGER64_FORMAT))
                .addProperties("examples", new ArraySchema().items(new StringSchema()))
                .addRequiredItem("id");
    }

    @Test(enabled = false, description = "convert a simple model")
    public void simpleModelTest() {
        final Schema model = new Schema()
                .description("a sample model")
                .addProperties("id", new IntegerSchema().format(SchemaTypeUtil.INTEGER64_FORMAT))
                .addProperties("name", new StringSchema())
                .addProperties("createdAt", new DateTimeSchema())
                .addRequiredItem("id")
                .addRequiredItem("name");
        final DefaultCodegen codegen = new CSharpClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 3);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "id");
        Assert.assertEquals(property1.datatype, "long?");
        Assert.assertEquals(property1.name, "Id");
        Assert.assertNull(property1.defaultValue);
        Assert.assertEquals(property1.baseType, "long?");
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertTrue(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME));
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));

        final CodegenProperty property2 = cm.vars.get(1);
        Assert.assertEquals(property2.baseName, "name");
        Assert.assertEquals(property2.datatype, "string");
        Assert.assertEquals(property2.name, "Name");
        Assert.assertNull(property2.defaultValue);
        Assert.assertEquals(property2.baseType, "string");
        Assert.assertTrue(getBooleanValue(property2, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertTrue(property2.required);
        Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME));
        Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));

        final CodegenProperty property3 = cm.vars.get(2);
        Assert.assertEquals(property3.baseName, "createdAt");
        Assert.assertEquals(property3.datatype, "DateTime?");
        Assert.assertEquals(property3.name, "CreatedAt");
        Assert.assertNull(property3.defaultValue);
        Assert.assertEquals(property3.baseType, "DateTime?");
        Assert.assertFalse(getBooleanValue(property3, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertFalse(property3.required);
        Assert.assertTrue(getBooleanValue(property3, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
    }

    @Test(enabled = false, description = "convert a model with list property")
    public void listPropertyTest() {
        final Schema model = new Schema()
                .description("a sample model")
                .addProperties("id",  new IntegerSchema().format(SchemaTypeUtil.INTEGER64_FORMAT))
                .addProperties("urls", new ArraySchema()
                        .items(new StringSchema()))
                .addRequiredItem("id");
        final DefaultCodegen codegen = new CSharpClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 2);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "id");
        Assert.assertEquals(property1.datatype, "long?");
        Assert.assertEquals(property1.name, "Id");
        Assert.assertNull(property1.defaultValue);
        Assert.assertEquals(property1.baseType, "long?");
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertTrue(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME));
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));

        final CodegenProperty property2 = cm.vars.get(1);
        Assert.assertEquals(property2.baseName, "urls");
        Assert.assertEquals(property2.datatype, "List<string>");
        Assert.assertEquals(property2.name, "Urls");
        Assert.assertNull(property2.defaultValue);
        Assert.assertEquals(property2.baseType, "List");
        Assert.assertFalse(getBooleanValue(property2, CodegenConstants.HAS_MORE_EXT_NAME));
        Assert.assertEquals(property2.containerType, "array");
        Assert.assertFalse(property2.required);
        Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME));
        Assert.assertTrue(getBooleanValue(property2, CodegenConstants.IS_CONTAINER_EXT_NAME));
    }

    @Test(enabled = false, description = "convert a model with a map property")
    public void mapPropertyTest() {
        final Schema schema = new Schema()
                .description("a sample model")
                .addProperties("translations", new MapSchema()
                        .additionalProperties(new StringSchema()))
                .addRequiredItem("id");
        final DefaultCodegen codegen = new CSharpClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", schema);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "translations");
        Assert.assertEquals(property1.datatype, "Dictionary<string, string>");
        Assert.assertEquals(property1.name, "Translations");
        Assert.assertEquals(property1.baseType, "Dictionary");
        Assert.assertEquals(property1.containerType, "map");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_CONTAINER_EXT_NAME));
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_PRIMITIVE_TYPE_EXT_NAME));
    }

    @Test(enabled = false, description = "convert a model with complex property")
    public void complexPropertyTest() {
        final Schema schema = new Schema()
                .description("a sample model")
                .addProperties("children", new Schema().$ref("#/components/schemas/Children"));
        final DefaultCodegen codegen = new CSharpClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", schema);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "children");
        Assert.assertEquals(property1.datatype, "Children");
        Assert.assertEquals(property1.name, "Children");
        Assert.assertEquals(property1.baseType, "Children");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
    }

    @Test(enabled = false, description = "convert a model with complex list property")
    public void complexListPropertyTest() {
        final Schema schema = new Schema()
                .description("a sample model")
                .addProperties("children", new ArraySchema()
                        .items(new Schema().$ref("#/components/schemas/Children")));
        final DefaultCodegen codegen = new CSharpClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", schema);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "children");
        Assert.assertEquals(property1.complexType, "Children");
        Assert.assertEquals(property1.datatype, "List<Children>");
        Assert.assertEquals(property1.name, "Children");
        Assert.assertEquals(property1.baseType, "List");
        Assert.assertEquals(property1.containerType, "array");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_CONTAINER_EXT_NAME));
    }

    @Test(enabled = false, description = "convert a model with complex map property")
    public void complexMapPropertyTest() {
        final Schema schema = new Schema()
                .description("a sample model")
                .addProperties("children", new MapSchema()
                        .additionalProperties(new Schema().$ref("#/components/schemas/Children")));
        final DefaultCodegen codegen = new CSharpClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", schema);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);
        Assert.assertEquals(Sets.intersection(cm.imports, Sets.newHashSet("Children")).size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "children");
        Assert.assertEquals(property1.complexType, "Children");
        Assert.assertEquals(property1.datatype, "Dictionary<string, Children>");
        Assert.assertEquals(property1.name, "Children");
        Assert.assertEquals(property1.baseType, "Dictionary");
        Assert.assertEquals(property1.containerType, "map");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(getBooleanValue(property1, CodegenConstants.IS_CONTAINER_EXT_NAME));
        Assert.assertFalse(getBooleanValue(property1, CodegenConstants.IS_NOT_CONTAINER_EXT_NAME));
    }

    @Test(enabled = false, description = "convert an array model")
    public void arrayModelTest() {
        final Schema schema = new ArraySchema()
                .items(new Schema().$ref("#/components/schemas/Children"))
                .description("an array model");
        final DefaultCodegen codegen = new CSharpClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", schema);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "an array model");
        Assert.assertEquals(cm.vars.size(), 0);
        Assert.assertEquals(cm.parent, "List<Children>");
        Assert.assertEquals(cm.imports.size(), 1);
        Assert.assertEquals(Sets.intersection(cm.imports, Sets.newHashSet("Children")).size(), 1);
    }

    @Test(enabled = false, description = "convert an map model")
    public void mapModelTest() {
        final Schema schema = new Schema()
                .description("a map model")
                .additionalProperties(new Schema().$ref("#/components/schemas/Children"));
        final DefaultCodegen codegen = new CSharpClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", schema);

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a map model");
        Assert.assertEquals(cm.vars.size(), 0);
        Assert.assertEquals(cm.parent, "Dictionary<String, Children>");
        Assert.assertEquals(cm.imports.size(), 1);
        Assert.assertEquals(Sets.intersection(cm.imports, Sets.newHashSet("Children")).size(), 1);
    }
}
