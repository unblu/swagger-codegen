package io.swagger.codegen.languages;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.SupportingFile;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import org.junit.Test;
import org.testng.Assert;

public class JavaJAXRSSpecServerCodegenTest {

    private JavaJAXRSSpecServerCodegen generator = new JavaJAXRSSpecServerCodegen();
/** disabled test for currently not supported languages.
    @Test
    public void do_not_process_RestApplication_when_interfaceOnly_is_true() {
        generator.additionalProperties().put(JavaJAXRSSpecServerCodegen.INTERFACE_ONLY, "true");
        generator.processOpts();
        for (SupportingFile file : generator.supportingFiles()) {
            Assert.assertNotEquals("RestApplication.mustache", file.templateFile);
        }
    }

    @Test
    public void do_process_pom_by_default() {
        generator.processOpts();
        for (SupportingFile file : generator.supportingFiles()) {
            if ("pom.mustache".equals(file.templateFile)) {
                return;
            }
        }
        Assert.fail("Missing pom.mustache");
    }

    @Test
    public void process_pom_if_generatePom_is_true() {
        generator.additionalProperties().put(JavaJAXRSSpecServerCodegen.GENERATE_POM, "true");
        generator.processOpts();
        for (SupportingFile file : generator.supportingFiles()) {
            if ("pom.mustache".equals(file.templateFile)) {
                return;
            }
        }
        Assert.fail("Missing pom.mustache");
    }

    @Test
    public void do_not_process_pom_if_generatePom_is_false() {
        generator.additionalProperties().put(JavaJAXRSSpecServerCodegen.GENERATE_POM, "false");
        generator.processOpts();
        for (SupportingFile file : generator.supportingFiles()) {
            Assert.assertNotEquals("pom.mustache", file.templateFile);
        }
    }

    @Test
    public void verify_that_generatePom_exists_as_a_parameter_with_default_true() {
        for (CliOption option : generator.cliOptions()) {
            if (option.getOpt().equals(JavaJAXRSSpecServerCodegen.GENERATE_POM)) {
                Assert.assertEquals(SchemaTypeUtil.BOOLEAN_TYPE, option.getType());
                Assert.assertEquals("true", option.getDefault());
                return;
            }
        }
        Assert.fail("Missing " + JavaJAXRSSpecServerCodegen.GENERATE_POM);
    }

    @Test
    public void verify_that_interfaceOnly_exists_as_a_parameter_with_default_false() {
        for (CliOption option : generator.cliOptions()) {
            if (option.getOpt().equals(JavaJAXRSSpecServerCodegen.INTERFACE_ONLY)) {
                Assert.assertEquals(SchemaTypeUtil.BOOLEAN_TYPE, option.getType());
                Assert.assertEquals("false", option.getDefault());
                return;
            }
        }
        Assert.fail("Missing " + JavaJAXRSSpecServerCodegen.INTERFACE_ONLY);
    }
    */
}
