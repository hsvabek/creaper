package org.wildfly.extras.creaper.commands.elytron.mapper;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.modules.AddModule;
import org.wildfly.extras.creaper.commands.modules.RemoveModule;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;


@RunWith(Arquillian.class)
// @Ignore("https://issues.jboss.org/browse/WFLY-8152")
public class AddCustomPrincipalTransformerOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME = "CreaperTestAddCustomPrincipalTransformer";
    private static final Address TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("custom-principal-transformer", TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME);
    private static final String TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME2 = "CreaperTestAddCustomPrincipalTransformer2";
    private static final Address TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("custom-principal-transformer", TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME2);
    private static final String CUSTOM_PRINCIPAL_TRANSFORMER_MODULE_NAME = "org.jboss.customprincipaltransformerimpl";

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException, InterruptedException, TimeoutException {
        try (OnlineManagementClient client = createManagementClient()) {
            File testJar1 = createJar("testJar", AddCustomPrincipalTransformerImpl.class);
            AddModule addModule = new AddModule.Builder(CUSTOM_PRINCIPAL_TRANSFORMER_MODULE_NAME)
                    .resource(testJar1)
                    .resourceDelimiter(":")
                .dependency("org.wildfly.security.elytron")
                .dependency("org.wildfly.extension.elytron")
                    .build();
            client.apply(addModule);
        }
    }

    // @AfterClass
    public static void afterClass() throws IOException, CommandFailedException, InterruptedException, TimeoutException {
        try (OnlineManagementClient client = createManagementClient()) {
            RemoveModule removeModule = new RemoveModule(CUSTOM_PRINCIPAL_TRANSFORMER_MODULE_NAME);
            client.apply(removeModule);
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS);
        ops.removeIfExists(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addCustomPrincipalTransformer() throws Exception {
        AddCustomPrincipalTransformer addAddCustomPrincipalTransformer
                = new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .className(AddCustomPrincipalTransformerImpl.class.getName())
                .module(CUSTOM_PRINCIPAL_TRANSFORMER_MODULE_NAME)
                .addConfiguration("param", "parameterValue")
            .build();

        client.apply(addAddCustomPrincipalTransformer);

        assertTrue("Add custom principal transformer should be created",
                ops.exists(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS));
    }

    @Test
    public void addCustomPrincipalTransformers() throws Exception {
        AddCustomPrincipalTransformer addAddCustomPrincipalTransformer
                = new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .className(AddCustomPrincipalTransformerImpl.class.getName())
                .module(CUSTOM_PRINCIPAL_TRANSFORMER_MODULE_NAME)
                .build();

        AddCustomPrincipalTransformer addAddCustomPrincipalTransformer2
                = new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME2)
                .className(AddCustomPrincipalTransformerImpl.class.getName())
                .module(CUSTOM_PRINCIPAL_TRANSFORMER_MODULE_NAME)
                .build();

        client.apply(addAddCustomPrincipalTransformer);
        client.apply(addAddCustomPrincipalTransformer2);

        assertTrue("Add custom principal transformer should be created",
                ops.exists(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS));
        assertTrue("Second add custom principal transformer should be created",
                ops.exists(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS2));

        checkAttribute(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS, "class-name",
                AddCustomPrincipalTransformerImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS2, "class-name",
                AddCustomPrincipalTransformerImpl.class.getName());

        administration.reload();

        checkAttribute(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS, "class-name",
                AddCustomPrincipalTransformerImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS2, "class-name",
                AddCustomPrincipalTransformerImpl.class.getName());
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateCustomPrincipalTransformerNotAllowed() throws Exception {
        AddCustomPrincipalTransformer addAddCustomPrincipalTransformer
                = new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .className(AddCustomPrincipalTransformerImpl.class.getName())
                .module(CUSTOM_PRINCIPAL_TRANSFORMER_MODULE_NAME)
                .build();

        client.apply(addAddCustomPrincipalTransformer);
        assertTrue("Add custom principal transformer should be created",
                ops.exists(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS));
        client.apply(addAddCustomPrincipalTransformer);
        fail("Add custom principal transformer " + TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME
                + " already exists in configuration, exception should be thrown");
    }

    @Test
    public void addDuplicateCustomPrincipalTransformerAllowed() throws Exception {
        AddCustomPrincipalTransformer addOperation
                = new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .className(AddCustomPrincipalTransformerImpl.class.getName())
                .module(CUSTOM_PRINCIPAL_TRANSFORMER_MODULE_NAME)
                .build();
        AddCustomPrincipalTransformer addOperation2
                = new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .className(AddCustomPrincipalTransformerImpl.class.getName())
                .module(CUSTOM_PRINCIPAL_TRANSFORMER_MODULE_NAME)
                .addConfiguration("configParam1", "configParameterValue")
            .replaceExisting()
            .build();

        client.apply(addOperation);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS));
        client.apply(addOperation2);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS));

        // check whether it was really rewritten
        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        checkAttributeProperties(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS, "configuration", expectedValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalTransformer_nullName() throws Exception {
        new AddCustomPrincipalTransformer.Builder(null)
                .className(AddCustomPrincipalTransformerImpl.class.getName());
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomPrincipalTransformer_emptyName() throws Exception {
        new AddCustomPrincipalTransformer.Builder("")
                .className(AddCustomPrincipalTransformerImpl.class.getName());
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomPrincipalTransformer_noModule() throws Exception {
        AddCustomPrincipalTransformer addAddCustomPrincipalTransformer
                = new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .className(AddCustomPrincipalTransformerImpl.class.getName())
                .build();

        client.apply(addAddCustomPrincipalTransformer);

        fail("Command should throw exception because Impl class is in non-global module.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalTransformer_noClassName() throws Exception {
        new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME).build();
        fail("Creating command with no custom should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalTransformer_emptyClassName() throws Exception {
        new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .className("")
            .build();

        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomPrincipalTransformer_wrongModule() throws Exception {
        AddCustomPrincipalTransformer addAddCustomPrincipalTransformer
                = new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .className(AddCustomPrincipalTransformerImpl.class.getName())
                .module("wrongModule")
            .build();

        client.apply(addAddCustomPrincipalTransformer);

        fail("Command with wrong module-name should throw exception.");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomPrincipalTransformer_configurationWithException() throws Exception {
        AddCustomPrincipalTransformer addAddCustomPrincipalTransformer
                = new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME)
                .className(AddCustomPrincipalTransformerImpl.class.getName())
                .module(CUSTOM_PRINCIPAL_TRANSFORMER_MODULE_NAME)
                .addConfiguration("throwException", "parameterValue")
            .build();

        client.apply(addAddCustomPrincipalTransformer);

        fail("Creating command with test configuration should throw exception");
    }

    @Test
    public void addCustomPrincipalTransformer_configuration() throws Exception {
        AddCustomPrincipalTransformer addAddCustomPrincipalTransformer
                = new AddCustomPrincipalTransformer.Builder(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_NAME2)
                .className(AddCustomPrincipalTransformerImpl.class.getName())
                .module(CUSTOM_PRINCIPAL_TRANSFORMER_MODULE_NAME)
                .addConfiguration("configParam1", "configParameterValue")
                .addConfiguration("configParam2", "configParameterValue2")
            .build();

        client.apply(addAddCustomPrincipalTransformer);

        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        expectedValues.add(new Property("configParam2", new ModelNode("configParameterValue2")));
        checkAttributeProperties(TEST_ADD_CUSTOM_PRINCIPAL_TRANSFORMER_ADDRESS2, "configuration", expectedValues);
    }
}
