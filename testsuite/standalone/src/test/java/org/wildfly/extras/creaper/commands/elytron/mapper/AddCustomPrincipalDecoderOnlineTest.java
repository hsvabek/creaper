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
public class AddCustomPrincipalDecoderOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME = "CreaperTestAddCustomPrincipalDecoder";
    private static final Address TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS = SUBSYSTEM_ADDRESS.and("custom-principal-decoder",
        TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME);
    private static final String TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME2 = "CreaperTestAddCustomPrincipalDecoder2";
    private static final Address TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS2 = SUBSYSTEM_ADDRESS.and("custom-principal-decoder",
        TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME2);
    private static final String CUSTOM_PRINCIPAL_DECODER_MODULE_NAME = "org.jboss.customprincipaldecoderimpl";

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException, InterruptedException, TimeoutException {
        try (OnlineManagementClient client = createManagementClient()) {
            File testJar1 = createJar("testJar", AddCustomPrincipalDecoderImpl.class);
            AddModule addModule = new AddModule.Builder(CUSTOM_PRINCIPAL_DECODER_MODULE_NAME)
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
            RemoveModule removeModule = new RemoveModule(CUSTOM_PRINCIPAL_DECODER_MODULE_NAME);
            client.apply(removeModule);
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS);
        ops.removeIfExists(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addCustomPrincipalDecoder() throws Exception {
        AddCustomPrincipalDecoder addAddCustomPrincipalDecoder =
            new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
            .className(AddCustomPrincipalDecoderImpl.class.getName())
            .module(CUSTOM_PRINCIPAL_DECODER_MODULE_NAME)
            .addConfiguration("param", "parameterValue")
            .build();

        client.apply(addAddCustomPrincipalDecoder);

        assertTrue("Add custom principal decoder should be created",
            ops.exists(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS));
    }

    @Test
    public void addCustomPrincipalDecoders() throws Exception {
        AddCustomPrincipalDecoder addAddCustomPrincipalDecoder =
            new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
            .className(AddCustomPrincipalDecoderImpl.class.getName())
            .module(CUSTOM_PRINCIPAL_DECODER_MODULE_NAME)
            .build();

        AddCustomPrincipalDecoder addAddCustomPrincipalDecoder2 =
            new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME2)
            .className(AddCustomPrincipalDecoderImpl.class.getName())
            .module(CUSTOM_PRINCIPAL_DECODER_MODULE_NAME)
            .build();

        client.apply(addAddCustomPrincipalDecoder);
        client.apply(addAddCustomPrincipalDecoder2);

        assertTrue("Add custom principal decoder should be created",
            ops.exists(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS));
        assertTrue("Second add custom principal decoder should be created",
            ops.exists(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS2));

        checkAttribute(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS, "class-name",
            AddCustomPrincipalDecoderImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS2, "class-name",
            AddCustomPrincipalDecoderImpl.class.getName());

        administration.reload();

        checkAttribute(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS, "class-name",
            AddCustomPrincipalDecoderImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS2, "class-name",
            AddCustomPrincipalDecoderImpl.class.getName());
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateCustomPrincipalDecoderNotAllowed() throws Exception {
        AddCustomPrincipalDecoder addAddCustomPrincipalDecoder =
            new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
            .className(AddCustomPrincipalDecoderImpl.class.getName())
            .module(CUSTOM_PRINCIPAL_DECODER_MODULE_NAME)
            .build();

        client.apply(addAddCustomPrincipalDecoder);
        assertTrue("Add custom principal decoder should be created",
            ops.exists(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS));
        client.apply(addAddCustomPrincipalDecoder);
        fail("Add custom principal decoder " + TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test
    public void addDuplicateCustomPrincipalDecoderAllowed() throws Exception {
        AddCustomPrincipalDecoder addOperation =
            new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
            .className(AddCustomPrincipalDecoderImpl.class.getName())
            .module(CUSTOM_PRINCIPAL_DECODER_MODULE_NAME)
            .build();
        AddCustomPrincipalDecoder addOperation2 =
            new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
            .className(AddCustomPrincipalDecoderImpl.class.getName())
            .module(CUSTOM_PRINCIPAL_DECODER_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .replaceExisting()
            .build();

        client.apply(addOperation);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS));
        client.apply(addOperation2);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS));

        // check whether it was really rewritten
        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        checkAttributeProperties(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS, "configuration", expectedValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalDecoder_nullName() throws Exception {
        new AddCustomPrincipalDecoder.Builder(null)
            .className(AddCustomPrincipalDecoderImpl.class.getName());
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomPrincipalDecoder_emptyName() throws Exception {
        new AddCustomPrincipalDecoder.Builder("")
            .className(AddCustomPrincipalDecoderImpl.class.getName());
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomPrincipalDecoder_noModule() throws Exception {
        AddCustomPrincipalDecoder addAddCustomPrincipalDecoder =
            new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
            .className(AddCustomPrincipalDecoderImpl.class.getName())
            .build();

        client.apply(addAddCustomPrincipalDecoder);

        fail("Command should throw exception because Impl class is in non-global module.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalDecoder_noClassName() throws Exception {
        new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME).build();
        fail("Creating command with no custom should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPrincipalDecoder_emptyClassName() throws Exception {
        new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
            .className("")
            .build();

        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomPrincipalDecoder_wrongModule() throws Exception {
        AddCustomPrincipalDecoder addAddCustomPrincipalDecoder =
            new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
            .className(AddCustomPrincipalDecoderImpl.class.getName())
            .module("wrongModule")
            .build();

        client.apply(addAddCustomPrincipalDecoder);

        fail("Command with wrong module-name should throw exception.");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomPrincipalDecoder_configurationWithException() throws Exception {
        AddCustomPrincipalDecoder addAddCustomPrincipalDecoder =
            new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME)
            .className(AddCustomPrincipalDecoderImpl.class.getName())
            .module(CUSTOM_PRINCIPAL_DECODER_MODULE_NAME)
            .addConfiguration("throwException", "parameterValue")
            .build();

        client.apply(addAddCustomPrincipalDecoder);

        fail("Creating command with test configuration should throw exception");
    }

    @Test
    public void addCustomPrincipalDecoder_configuration() throws Exception {
        AddCustomPrincipalDecoder addAddCustomPrincipalDecoder =
            new AddCustomPrincipalDecoder.Builder(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_NAME2)
            .className(AddCustomPrincipalDecoderImpl.class.getName())
            .module(CUSTOM_PRINCIPAL_DECODER_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .addConfiguration("configParam2", "configParameterValue2")
            .build();

        client.apply(addAddCustomPrincipalDecoder);

        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        expectedValues.add(new Property("configParam2", new ModelNode("configParameterValue2")));
        checkAttributeProperties(TEST_ADD_CUSTOM_PRINCIPAL_DECODER_ADDRESS2, "configuration", expectedValues);
    }
}
