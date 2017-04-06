package org.wildfly.extras.creaper.commands.elytron.credfactory;

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
// @Ignore("https://issues.jboss.org/browse/WFLY-8151")
public class AddCustomCredentialSecurityFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME = "CreaperTestAddCustomCredentialSecurityFactory";
    private static final Address TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS =
        SUBSYSTEM_ADDRESS.and("custom-credential-security-factory", TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME);
    private static final String TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME2 = "CreaperTestAddCustomCredentialSecurityFactory2";
    private static final Address TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS2 =
        SUBSYSTEM_ADDRESS.and("custom-credential-security-factory", TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME2);
    private static final String CUSTOM_CRED_SEC_FACTORY_MODULE_NAME = "org.jboss.customcredsecfacimpl";

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException, InterruptedException, TimeoutException {
        try (OnlineManagementClient client = createManagementClient()) {
            File testJar1 = createJar("testJar", AddCustomCredentialSecurityFactoryImpl.class);
            AddModule addModule = new AddModule.Builder(CUSTOM_CRED_SEC_FACTORY_MODULE_NAME)
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
            RemoveModule removeModule = new RemoveModule(CUSTOM_CRED_SEC_FACTORY_MODULE_NAME);
            client.apply(removeModule);
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addCustomCredentialSecurityFactory() throws Exception {
        AddCustomCredentialSecurityFactory addAddCustomCredentialSecurityFactory =
            new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
                .className(AddCustomCredentialSecurityFactoryImpl.class.getName())
                .module(CUSTOM_CRED_SEC_FACTORY_MODULE_NAME)
                .addConfiguration("param", "parameterValue")
                .build();

        client.apply(addAddCustomCredentialSecurityFactory);

        assertTrue("Add custom credential security factory should be created",
            ops.exists(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS));
    }

    @Test
    public void addCustomCredentialSecurityFactorys() throws Exception {
        AddCustomCredentialSecurityFactory addAddCustomCredentialSecurityFactory =
            new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
            .className(AddCustomCredentialSecurityFactoryImpl.class.getName())
            .module(CUSTOM_CRED_SEC_FACTORY_MODULE_NAME)
            .build();

        AddCustomCredentialSecurityFactory addAddCustomCredentialSecurityFactory2 =
            new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME2)
            .className(AddCustomCredentialSecurityFactoryImpl.class.getName())
            .module(CUSTOM_CRED_SEC_FACTORY_MODULE_NAME)
            .build();

        client.apply(addAddCustomCredentialSecurityFactory);
        client.apply(addAddCustomCredentialSecurityFactory2);

        assertTrue("Add custom credential security factory should be created",
            ops.exists(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS));
        assertTrue("Second add custom credential security factory should be created",
            ops.exists(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS2));

        checkAttribute(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS, "class-name",
            AddCustomCredentialSecurityFactoryImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS2, "class-name",
            AddCustomCredentialSecurityFactoryImpl.class.getName());

        administration.reload();

        checkAttribute(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS, "class-name",
            AddCustomCredentialSecurityFactoryImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS2, "class-name",
            AddCustomCredentialSecurityFactoryImpl.class.getName());
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateCustomCredentialSecurityFactoryNotAllowed() throws Exception {
        AddCustomCredentialSecurityFactory addAddCustomCredentialSecurityFactory =
            new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
            .className(AddCustomCredentialSecurityFactoryImpl.class.getName())
            .module(CUSTOM_CRED_SEC_FACTORY_MODULE_NAME)
            .build();

        client.apply(addAddCustomCredentialSecurityFactory);
        assertTrue("Add custom credential security factory should be created",
            ops.exists(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS));
        client.apply(addAddCustomCredentialSecurityFactory);
        fail("Add custom credential security factory " + TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test
    public void addDuplicateCustomCredentialSecurityFactoryAllowed() throws Exception {
        AddCustomCredentialSecurityFactory addOperation =
            new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
            .className(AddCustomCredentialSecurityFactoryImpl.class.getName())
            .module(CUSTOM_CRED_SEC_FACTORY_MODULE_NAME)
            .build();
        AddCustomCredentialSecurityFactory addOperation2 =
            new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
            .className(AddCustomCredentialSecurityFactoryImpl.class.getName())
            .module(CUSTOM_CRED_SEC_FACTORY_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .replaceExisting()
            .build();

        client.apply(addOperation);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS));
        client.apply(addOperation2);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS));

        // check whether it was really rewritten
        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        checkAttributeProperties(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS, "configuration", expectedValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomCredentialSecurityFactory_nullName() throws Exception {
        new AddCustomCredentialSecurityFactory.Builder(null)
            .className(AddCustomCredentialSecurityFactoryImpl.class.getName());
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomCredentialSecurityFactory_emptyName() throws Exception {
        new AddCustomCredentialSecurityFactory.Builder("")
            .className(AddCustomCredentialSecurityFactoryImpl.class.getName());
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomCredentialSecurityFactory_noModule() throws Exception {
        AddCustomCredentialSecurityFactory addAddCustomCredentialSecurityFactory =
            new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
            .className(AddCustomCredentialSecurityFactoryImpl.class.getName())
            .build();

        client.apply(addAddCustomCredentialSecurityFactory);

        fail("Command should throw exception because Impl class is in non-global module.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomCredentialSecurityFactory_noClassName() throws Exception {
        new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME).build();
        fail("Creating command with no custom should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomCredentialSecurityFactory_emptyClassName() throws Exception {
        new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
            .className("")
            .build();

        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomCredentialSecurityFactory_wrongModule() throws Exception {
        AddCustomCredentialSecurityFactory addAddCustomCredentialSecurityFactory =
            new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
            .className(AddCustomCredentialSecurityFactoryImpl.class.getName())
            .module("wrongModule")
            .build();

        client.apply(addAddCustomCredentialSecurityFactory);

        fail("Command with wrong module-name should throw exception.");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomCredentialSecurityFactory_configurationWithException() throws Exception {
        AddCustomCredentialSecurityFactory addAddCustomCredentialSecurityFactory =
            new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME)
            .className(AddCustomCredentialSecurityFactoryImpl.class.getName())
            .module(CUSTOM_CRED_SEC_FACTORY_MODULE_NAME)
            .addConfiguration("throwException", "parameterValue")
            .build();

        client.apply(addAddCustomCredentialSecurityFactory);

        fail("Creating command with test configuration should throw exception");
    }

    @Test
    public void addCustomCredentialSecurityFactory_configuration() throws Exception {
        AddCustomCredentialSecurityFactory command =
            new AddCustomCredentialSecurityFactory.Builder(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_NAME2)
            .className(AddCustomCredentialSecurityFactoryImpl.class.getName())
            .module(CUSTOM_CRED_SEC_FACTORY_MODULE_NAME)
                .addConfiguration("configParam1", "configParameterValue")
                .addConfiguration("configParam2", "configParameterValue2")
            .build();

        client.apply(command);

        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        expectedValues.add(new Property("configParam2", new ModelNode("configParameterValue2")));
        checkAttributeProperties(TEST_ADD_CUSTOM_CRED_SEC_FACTORY_ADDRESS2, "configuration", expectedValues);
    }
}
