package io.swagger.codegen.swift4;

import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.languages.Swift4Codegen;
import io.swagger.codegen.languages.helpers.ExtensionHelper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.swagger.codegen.languages.helpers.ExtensionHelper.getBooleanValue;

public class Swift4CodegenTest {

    Swift4Codegen swiftCodegen = new Swift4Codegen();

    @Test(enabled = false)
    public void testCapitalizedReservedWord() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("AS", null), "_as");
    }

    @Test(enabled = false)
    public void testReservedWord() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("Public", null), "_public");
    }

    @Test(enabled = false)
    public void shouldNotBreakNonReservedWord() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("Error", null), "error");
    }

    @Test(enabled = false)
    public void shouldNotBreakCorrectName() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("EntryName", null), "entryName");
    }

    @Test(enabled = false)
    public void testSingleWordAllCaps() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("VALUE", null), "value");
    }

    @Test(enabled = false)
    public void testSingleWordLowercase() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("value", null), "value");
    }

    @Test(enabled = false)
    public void testCapitalsWithUnderscore() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("ENTRY_NAME", null), "entryName");
    }

    @Test(enabled = false)
    public void testCapitalsWithDash() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("ENTRY-NAME", null), "entryName");
    }

    @Test(enabled = false)
    public void testCapitalsWithSpace() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("ENTRY NAME", null), "entryName");
    }

    @Test(enabled = false)
    public void testLowercaseWithUnderscore() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("entry_name", null), "entryName");
    }

    @Test(enabled = false)
    public void testStartingWithNumber() throws Exception {
        Assert.assertEquals(swiftCodegen.toEnumVarName("123EntryName", null), "_123entryName");
        Assert.assertEquals(swiftCodegen.toEnumVarName("123Entry_name", null), "_123entryName");
        Assert.assertEquals(swiftCodegen.toEnumVarName("123EntryName123", null), "_123entryName123");
    }

    @Test(description = "returns Data when response format is binary", enabled = false)
    public void binaryDataTest() {
        // TODO update json file

        final OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/resources/2_0/binaryDataTest.json");
        final DefaultCodegen codegen = new Swift4Codegen();
        final String path = "/tests/binaryResponse";
        final Operation p = openAPI.getPaths().get(path).getPost();
        final CodegenOperation op = codegen.fromOperation(path, "post", p, openAPI.getComponents().getSchemas());

        Assert.assertEquals(op.returnType, "Data");
        Assert.assertEquals(op.bodyParam.dataType, "Data");
        Assert.assertTrue(getBooleanValue(op.bodyParam, CodegenConstants.IS_BINARY_EXT_NAME));
        Assert.assertTrue(getBooleanValue(op.responses.get(0), CodegenConstants.IS_BINARY_EXT_NAME));
    }

    @Test(description = "returns Date when response format is date", enabled = false)
    public void dateTest() {
        final OpenAPI openAPI = new OpenAPIV3Parser().read("src/test/resources/2_0/datePropertyTest.json");
        final DefaultCodegen codegen = new Swift4Codegen();
        final String path = "/tests/dateResponse";
        final Operation p = openAPI.getPaths().get(path).getPost();
        final CodegenOperation op = codegen.fromOperation(path, "post", p, openAPI.getComponents().getSchemas());

        Assert.assertEquals(op.returnType, "Date");
        Assert.assertEquals(op.bodyParam.dataType, "Date");
    }

    @Test(enabled = false)
    public void testDefaultPodAuthors() throws Exception {
        // Given

        // When
        swiftCodegen.processOpts();

        // Then
        final String podAuthors = (String) swiftCodegen.additionalProperties().get(Swift4Codegen.POD_AUTHORS);
        Assert.assertEquals(podAuthors, Swift4Codegen.DEFAULT_POD_AUTHORS);
    }

    @Test(enabled = false)
    public void testPodAuthors() throws Exception {
        // Given
        final String swaggerDevs = "Swagger Devs";
        swiftCodegen.additionalProperties().put(Swift4Codegen.POD_AUTHORS, swaggerDevs);

        // When
        swiftCodegen.processOpts();

        // Then
        final String podAuthors = (String) swiftCodegen.additionalProperties().get(Swift4Codegen.POD_AUTHORS);
        Assert.assertEquals(podAuthors, swaggerDevs);
    }

}
