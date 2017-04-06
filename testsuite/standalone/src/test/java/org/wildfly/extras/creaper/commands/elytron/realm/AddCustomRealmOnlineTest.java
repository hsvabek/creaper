package org.wildfly.extras.creaper.commands.elytron.realm;

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
public class AddCustomRealmOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_REALM_NAME = "CreaperTestAddCustomRealm";
    private static final Address TEST_ADD_CUSTOM_REALM_ADDRESS = SUBSYSTEM_ADDRESS.and("custom-realm",
        TEST_ADD_CUSTOM_REALM_NAME);
    private static final String TEST_ADD_CUSTOM_REALM_NAME2 = "CreaperTestAddCustomRealm2";
    private static final Address TEST_ADD_CUSTOM_REALM_ADDRESS2 = SUBSYSTEM_ADDRESS.and("custom-realm",
        TEST_ADD_CUSTOM_REALM_NAME2);
    private static final String CUSTOM_REALM_MODULE_NAME = "org.jboss.customrealmimpl";

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException, InterruptedException, TimeoutException {
        try (OnlineManagementClient client = createManagementClient()) {
            File testJar1 = createJar("testJar", AddCustomRealmImpl.class);
            AddModule addModule = new AddModule.Builder(CUSTOM_REALM_MODULE_NAME)
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
            RemoveModule removeModule = new RemoveModule(CUSTOM_REALM_MODULE_NAME);
            client.apply(removeModule);
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_ADD_CUSTOM_REALM_ADDRESS);
        ops.removeIfExists(TEST_ADD_CUSTOM_REALM_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addCustomRealm() throws Exception {
        AddCustomRealm addAddCustomRealm = new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME)
            .className(AddCustomRealmImpl.class.getName())
            .module(CUSTOM_REALM_MODULE_NAME)
            .addConfiguration("param", "parameterValue")
            .build();

        client.apply(addAddCustomRealm);

        assertTrue("Add custom realm should be created", ops.exists(TEST_ADD_CUSTOM_REALM_ADDRESS));
    }

    @Test
    public void addCustomRealms() throws Exception {
        AddCustomRealm addAddCustomRealm = new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME)
            .className(AddCustomRealmImpl.class.getName())
            .module(CUSTOM_REALM_MODULE_NAME)
            .build();

        AddCustomRealm addAddCustomRealm2 = new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME2)
            .className(AddCustomRealmImpl.class.getName())
            .module(CUSTOM_REALM_MODULE_NAME)
            .build();

        client.apply(addAddCustomRealm);
        client.apply(addAddCustomRealm2);

        assertTrue("Add custom realm should be created", ops.exists(TEST_ADD_CUSTOM_REALM_ADDRESS));
        assertTrue("Second add custom realm should be created",
            ops.exists(TEST_ADD_CUSTOM_REALM_ADDRESS2));

        checkAttribute(TEST_ADD_CUSTOM_REALM_ADDRESS, "class-name", AddCustomRealmImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_REALM_ADDRESS2, "class-name", AddCustomRealmImpl.class.getName());

        administration.reload();

        checkAttribute(TEST_ADD_CUSTOM_REALM_ADDRESS, "class-name", AddCustomRealmImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_REALM_ADDRESS2, "class-name", AddCustomRealmImpl.class.getName());
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateCustomRealmNotAllowed() throws Exception {
        AddCustomRealm addAddCustomRealm = new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME)
            .className(AddCustomRealmImpl.class.getName())
            .module(CUSTOM_REALM_MODULE_NAME)
            .build();

        client.apply(addAddCustomRealm);
        assertTrue("Add custom realm should be created", ops.exists(TEST_ADD_CUSTOM_REALM_ADDRESS));
        client.apply(addAddCustomRealm);
        fail("Add custom realm " + TEST_ADD_CUSTOM_REALM_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test
    public void addDuplicateCustomRealmAllowed() throws Exception {
        AddCustomRealm addOperation =
            new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME)
            .className(AddCustomRealmImpl.class.getName())
            .module(CUSTOM_REALM_MODULE_NAME)
            .build();
        AddCustomRealm addOperation2 =
            new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME)
            .className(AddCustomRealmImpl.class.getName())
            .module(CUSTOM_REALM_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .replaceExisting()
            .build();

        client.apply(addOperation);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_REALM_ADDRESS));
        client.apply(addOperation2);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_REALM_ADDRESS));

        // check whether it was really rewritten
        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        checkAttributeProperties(TEST_ADD_CUSTOM_REALM_ADDRESS, "configuration", expectedValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealm_nullName() throws Exception {
        new AddCustomRealm.Builder(null)
            .className(AddCustomRealmImpl.class.getName());
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomRealm_emptyName() throws Exception {
        new AddCustomRealm.Builder("")
            .className(AddCustomRealmImpl.class.getName());
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomRealm_noModule() throws Exception {
        AddCustomRealm addAddCustomRealm = new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME)
            .className(AddCustomRealmImpl.class.getName())
            .build();

        client.apply(addAddCustomRealm);

        fail("Command should throw exception because Impl class is in non-global module.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealm_noClassName() throws Exception {
        new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME).build();
        fail("Creating command with no custom should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealm_emptyClassName() throws Exception {
        new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME).className("").build();
        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomRealm_wrongModule() throws Exception {
        AddCustomRealm addAddCustomRealm = new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME)
            .className(AddCustomRealmImpl.class.getName())
            .module("wrongModule")
            .build();

        client.apply(addAddCustomRealm);

        fail("Command should throw exception.");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomRealm_configurationWithException() throws Exception {
        AddCustomRealm addAddCustomRealm = new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME)
            .className(AddCustomRealmImpl.class.getName())
            .module(CUSTOM_REALM_MODULE_NAME)
            .addConfiguration("throwException", "parameterValue")
            .build();

        client.apply(addAddCustomRealm);

        fail("Command with wrong module-name should throw exception.");
    }

    @Test
    public void addCustomRealm_configuration() throws Exception {
        AddCustomRealm addAddCustomRealm = new AddCustomRealm.Builder(TEST_ADD_CUSTOM_REALM_NAME2)
            .className(AddCustomRealmImpl.class.getName())
            .module(CUSTOM_REALM_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .addConfiguration("configParam2", "configParameterValue2")
            .build();

        client.apply(addAddCustomRealm);

        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        expectedValues.add(new Property("configParam2", new ModelNode("configParameterValue2")));
        checkAttributeProperties(TEST_ADD_CUSTOM_REALM_ADDRESS2, "configuration", expectedValues);
    }
}
