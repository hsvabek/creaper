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
public class AddCustomModifiableRealmOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME = "CreaperTestAddCustomModifiableRealm";
    private static final Address TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS = SUBSYSTEM_ADDRESS
        .and("custom-modifiable-realm", TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME);
    private static final String TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME2 = "CreaperTestAddCustomModifiableRealm2";
    private static final Address TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS2 = SUBSYSTEM_ADDRESS
        .and("custom-modifiable-realm", TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME2);
    private static final String CUSTOM_MODIFIABLE_REALM_MODULE_NAME = "org.jboss.custommodifiablerealmimpl";

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException, InterruptedException, TimeoutException {
        try (OnlineManagementClient client = createManagementClient()) {
            File testJar1 = createJar("testJar", AddCustomModifiableRealmImpl.class);
            AddModule addModule = new AddModule.Builder(CUSTOM_MODIFIABLE_REALM_MODULE_NAME)
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
            RemoveModule removeModule = new RemoveModule(CUSTOM_MODIFIABLE_REALM_MODULE_NAME);
            client.apply(removeModule);
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS);
        ops.removeIfExists(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addCustomModifiableRealm() throws Exception {
        AddCustomModifiableRealm addAddCustomModifiableRealm =
            new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME)
            .className(AddCustomModifiableRealmImpl.class.getName())
            .module(CUSTOM_MODIFIABLE_REALM_MODULE_NAME)
            .addConfiguration("param", "parameterValue")
            .build();

        client.apply(addAddCustomModifiableRealm);

        assertTrue("Add custom modifiable realm  should be created",
            ops.exists(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS));
    }

    @Test
    public void addCustomModifiableRealms() throws Exception {
        AddCustomModifiableRealm addAddCustomModifiableRealm =
            new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME)
            .className(AddCustomModifiableRealmImpl.class.getName())
            .module(CUSTOM_MODIFIABLE_REALM_MODULE_NAME)
            .build();

        AddCustomModifiableRealm addAddCustomModifiableRealm2 = new AddCustomModifiableRealm.Builder(
            TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME2).className(AddCustomModifiableRealmImpl.class.getName())
            .module(CUSTOM_MODIFIABLE_REALM_MODULE_NAME)
            .build();

        client.apply(addAddCustomModifiableRealm);
        client.apply(addAddCustomModifiableRealm2);

        assertTrue("Add custom modifiable realm  should be created",
            ops.exists(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS));
        assertTrue("Second add custom modifiable realm  should be created",
            ops.exists(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS2));

        checkAttribute(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS, "class-name",
            AddCustomModifiableRealmImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS2, "class-name",
            AddCustomModifiableRealmImpl.class.getName());

        administration.reload();

        checkAttribute(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS, "class-name",
            AddCustomModifiableRealmImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS2, "class-name",
            AddCustomModifiableRealmImpl.class.getName());
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateCustomModifiableRealmNotAllowed() throws Exception {
        AddCustomModifiableRealm addAddCustomModifiableRealm =
            new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME)
            .className(AddCustomModifiableRealmImpl.class.getName())
            .module(CUSTOM_MODIFIABLE_REALM_MODULE_NAME)
            .build();

        client.apply(addAddCustomModifiableRealm);
        assertTrue("Add custom modifiable realm  should be created",
            ops.exists(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS));
        client.apply(addAddCustomModifiableRealm);
        fail("Add custom modifiable realm  " + TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test
    public void addDuplicateCustomModifiableRealmAllowed() throws Exception {
        AddCustomModifiableRealm addOperation =
            new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME)
            .className(AddCustomModifiableRealmImpl.class.getName())
            .module(CUSTOM_MODIFIABLE_REALM_MODULE_NAME)
            .build();
        AddCustomModifiableRealm addOperation2 =
            new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME)
            .className(AddCustomModifiableRealmImpl.class.getName())
            .module(CUSTOM_MODIFIABLE_REALM_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .replaceExisting()
            .build();

        client.apply(addOperation);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS));
        client.apply(addOperation2);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS));

        // check whether it was really rewritten
        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        checkAttributeProperties(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS, "configuration", expectedValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomModifiableRealm_nullName() throws Exception {
        new AddCustomModifiableRealm.Builder(null)
            .className(AddCustomModifiableRealmImpl.class.getName());
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomModifiableRealm_emptyName() throws Exception {
        new AddCustomModifiableRealm.Builder("")
            .className(AddCustomModifiableRealmImpl.class.getName());
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomModifiableRealm_noModule() throws Exception {
        AddCustomModifiableRealm addAddCustomModifiableRealm =
            new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME)
            .className(AddCustomModifiableRealmImpl.class.getName())
            .build();

        client.apply(addAddCustomModifiableRealm);

        fail("Command should throw exception because Impl class is in non-global module.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomModifiableRealm_noClassName() throws Exception {
        new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME).build();
        fail("Creating command with no custom should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomModifiableRealm_emptyClassName() throws Exception {
        new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME).className("").build();
        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomModifiableRealm_wrongModule() throws Exception {
        AddCustomModifiableRealm addAddCustomModifiableRealm =
            new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME)
            .className(AddCustomModifiableRealmImpl.class.getName())
            .module("wrongModule")
            .build();

        client.apply(addAddCustomModifiableRealm);

        fail("Command with wrong module-name should throw exception.");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomModifiableRealm_configurationWithException() throws Exception {
        AddCustomModifiableRealm addAddCustomModifiableRealm =
            new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME)
            .className(AddCustomModifiableRealmImpl.class.getName())
            .module(CUSTOM_MODIFIABLE_REALM_MODULE_NAME)
            .addConfiguration("throwException", "parameterValue")
            .build();

        client.apply(addAddCustomModifiableRealm);

        fail("Creating command with test configuration should throw exception");
    }

    @Test
    public void addCustomModifiableRealm_configuration() throws Exception {
        AddCustomModifiableRealm command =
            new AddCustomModifiableRealm.Builder(TEST_ADD_CUSTOM_MODIFIABLE_REALM_NAME2)
            .className(AddCustomModifiableRealmImpl.class.getName())
            .module(CUSTOM_MODIFIABLE_REALM_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .addConfiguration("configParam2", "configParameterValue2")
            .build();

        client.apply(command);

        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        expectedValues.add(new Property("configParam2", new ModelNode("configParameterValue2")));
        checkAttributeProperties(TEST_ADD_CUSTOM_MODIFIABLE_REALM_ADDRESS2, "configuration", expectedValues);
    }
}
