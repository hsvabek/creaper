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
public class AddCustomRoleDecoderOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_ROLE_DECODER_NAME = "CreaperTestAddCustomRoleDecoder";
    private static final Address TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS = SUBSYSTEM_ADDRESS.and("custom-role-decoder",
        TEST_ADD_CUSTOM_ROLE_DECODER_NAME);
    private static final String TEST_ADD_CUSTOM_ROLE_DECODER_NAME2 = "CreaperTestAddCustomRoleDecoder2";
    private static final Address TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS2 = SUBSYSTEM_ADDRESS.and("custom-role-decoder",
        TEST_ADD_CUSTOM_ROLE_DECODER_NAME2);
    private static final String CUSTOM_ROLE_DECODER_MODULE_NAME = "org.jboss.customroledecoderimpl";

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException, InterruptedException, TimeoutException {
        try (OnlineManagementClient client = createManagementClient()) {
            File testJar1 = createJar("testJar", AddCustomRoleDecoderImpl.class);
            AddModule addModule = new AddModule.Builder(CUSTOM_ROLE_DECODER_MODULE_NAME)
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
            RemoveModule removeModule = new RemoveModule(CUSTOM_ROLE_DECODER_MODULE_NAME);
            client.apply(removeModule);
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS);
        ops.removeIfExists(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addCustomRoleDecoder() throws Exception {
        AddCustomRoleDecoder addAddCustomRoleDecoder =
            new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
            .className(AddCustomRoleDecoderImpl.class.getName())
            .module(CUSTOM_ROLE_DECODER_MODULE_NAME)
            .addConfiguration("param", "parameterValue")
            .build();

        client.apply(addAddCustomRoleDecoder);

        assertTrue("Add custom role decoder should be created",
            ops.exists(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS));
    }

    @Test
    public void addCustomRoleDecoders() throws Exception {
        AddCustomRoleDecoder addAddCustomRoleDecoder =
            new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
            .className(AddCustomRoleDecoderImpl.class.getName())
            .module(CUSTOM_ROLE_DECODER_MODULE_NAME)
            .build();

        AddCustomRoleDecoder addAddCustomRoleDecoder2 =
            new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME2)
            .className(AddCustomRoleDecoderImpl.class.getName())
            .module(CUSTOM_ROLE_DECODER_MODULE_NAME)
            .build();

        client.apply(addAddCustomRoleDecoder);
        client.apply(addAddCustomRoleDecoder2);

        assertTrue("Add custom role decoder should be created",
            ops.exists(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS));
        assertTrue("Second add custom role decoder should be created",
            ops.exists(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS2));

        checkAttribute(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS, "class-name",
            AddCustomRoleDecoderImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS2, "class-name",
            AddCustomRoleDecoderImpl.class.getName());

        administration.reload();

        checkAttribute(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS, "class-name",
            AddCustomRoleDecoderImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS2, "class-name",
            AddCustomRoleDecoderImpl.class.getName());
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateCustomRoleDecoderNotAllowed() throws Exception {
        AddCustomRoleDecoder addAddCustomRoleDecoder =
            new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
            .className(AddCustomRoleDecoderImpl.class.getName())
            .module(CUSTOM_ROLE_DECODER_MODULE_NAME)
            .build();

        client.apply(addAddCustomRoleDecoder);
        assertTrue("Add custom role decoder should be created",
            ops.exists(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS));
        client.apply(addAddCustomRoleDecoder);
        fail("Add custom role decoder " + TEST_ADD_CUSTOM_ROLE_DECODER_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test
    public void addDuplicateCustomRoleDecoderAllowed() throws Exception {
        AddCustomRoleDecoder addOperation =
            new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
            .className(AddCustomRoleDecoderImpl.class.getName())
            .module(CUSTOM_ROLE_DECODER_MODULE_NAME)
            .build();
        AddCustomRoleDecoder addOperation2 =
            new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
            .className(AddCustomRoleDecoderImpl.class.getName())
            .module(CUSTOM_ROLE_DECODER_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .replaceExisting()
            .build();

        client.apply(addOperation);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS));
        client.apply(addOperation2);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS));

        // check whether it was really rewritten
        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        checkAttributeProperties(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS, "configuration", expectedValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleDecoder_nullName() throws Exception {
        new AddCustomRoleDecoder.Builder(null)
            .className(AddCustomRoleDecoderImpl.class.getName());
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomRoleDecoder_emptyName() throws Exception {
        new AddCustomRoleDecoder.Builder("")
            .className(AddCustomRoleDecoderImpl.class.getName());
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomRoleDecoder_noModule() throws Exception {
        AddCustomRoleDecoder addAddCustomRoleDecoder =
            new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
            .className(AddCustomRoleDecoderImpl.class.getName())
            .build();

        client.apply(addAddCustomRoleDecoder);

        fail("Command should throw exception because Impl class is in non-global module.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleDecoder_noClassName() throws Exception {
        new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME).build();
        fail("Creating command with no custom should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleDecoder_emptyClassName() throws Exception {
        new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
            .className("")
            .build();

        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomRoleDecoder_wrongModule() throws Exception {
        AddCustomRoleDecoder addAddCustomRoleDecoder =
            new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
            .className(AddCustomRoleDecoderImpl.class.getName())
            .module("wrongModule")
            .build();

        client.apply(addAddCustomRoleDecoder);

        fail("Command with wrong module-name should throw exception.");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomRoleDecoder_configurationWithException() throws Exception {
        AddCustomRoleDecoder addAddCustomRoleDecoder =
            new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME)
            .className(AddCustomRoleDecoderImpl.class.getName())
            .module(CUSTOM_ROLE_DECODER_MODULE_NAME)
            .addConfiguration("throwException", "parameterValue")
            .build();

        client.apply(addAddCustomRoleDecoder);

        fail("Creating command with test configuration should throw exception");
    }

    @Test
    public void addCustomRoleDecoder_configuration() throws Exception {
        AddCustomRoleDecoder addAddCustomRoleDecoder =
            new AddCustomRoleDecoder.Builder(TEST_ADD_CUSTOM_ROLE_DECODER_NAME2)
            .className(AddCustomRoleDecoderImpl.class.getName())
            .module(CUSTOM_ROLE_DECODER_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .addConfiguration("configParam2", "configParameterValue2")
            .build();

        client.apply(addAddCustomRoleDecoder);

        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        expectedValues.add(new Property("configParam2", new ModelNode("configParameterValue2")));
        checkAttributeProperties(TEST_ADD_CUSTOM_ROLE_DECODER_ADDRESS2, "configuration", expectedValues);
    }
}
