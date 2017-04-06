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
public class AddCustomRealmMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_REALM_MAPPER_NAME = "CreaperTestAddCustomRealmMapper";
    private static final Address TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS.and("custom-realm-mapper",
        TEST_ADD_CUSTOM_REALM_MAPPER_NAME);
    private static final String TEST_ADD_CUSTOM_REALM_MAPPER_NAME2 = "CreaperTestAddCustomRealmMapper2";
    private static final Address TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS.and("custom-realm-mapper",
        TEST_ADD_CUSTOM_REALM_MAPPER_NAME2);
    private static final String CUSTOM_REALM_MAPPER_MODULE_NAME = "org.jboss.customrealmmapperimpl";

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException, InterruptedException, TimeoutException {
        try (OnlineManagementClient client = createManagementClient()) {
            File testJar1 = createJar("testJar", AddCustomRealmMapperImpl.class);
            AddModule addModule = new AddModule.Builder(CUSTOM_REALM_MAPPER_MODULE_NAME)
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
            RemoveModule removeModule = new RemoveModule(CUSTOM_REALM_MAPPER_MODULE_NAME);
            client.apply(removeModule);
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addCustomRealmMapper() throws Exception {
        AddCustomRealmMapper addAddCustomRealmMapper =
            new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME)
            .className(AddCustomRealmMapperImpl.class.getName())
            .module(CUSTOM_REALM_MAPPER_MODULE_NAME)
            .addConfiguration("param", "parameterValue")
            .build();

        client.apply(addAddCustomRealmMapper);

        assertTrue("Add custom realm mapper should be created", ops.exists(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS));
    }

    @Test
    public void addCustomRealmMappers() throws Exception {
        AddCustomRealmMapper addAddCustomRealmMapper =
            new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME)
            .className(AddCustomRealmMapperImpl.class.getName())
            .module(CUSTOM_REALM_MAPPER_MODULE_NAME)
            .build();

        AddCustomRealmMapper addAddCustomRealmMapper2 =
            new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME2)
            .className(AddCustomRealmMapperImpl.class.getName())
            .module(CUSTOM_REALM_MAPPER_MODULE_NAME)
            .build();

        client.apply(addAddCustomRealmMapper);
        client.apply(addAddCustomRealmMapper2);

        assertTrue("Add custom realm mapper should be created", ops.exists(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS));
        assertTrue("Second add custom realm mapper should be created",
            ops.exists(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS2));

        checkAttribute(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS, "class-name", AddCustomRealmMapperImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS2, "class-name", AddCustomRealmMapperImpl.class.getName());

        administration.reload();

        checkAttribute(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS, "class-name", AddCustomRealmMapperImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS2, "class-name", AddCustomRealmMapperImpl.class.getName());
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateCustomRealmMapperNotAllowed() throws Exception {
        AddCustomRealmMapper addAddCustomRealmMapper =
            new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME)
            .className(AddCustomRealmMapperImpl.class.getName())
            .module(CUSTOM_REALM_MAPPER_MODULE_NAME)
            .build();

        client.apply(addAddCustomRealmMapper);
        assertTrue("Add custom realm mapper should be created", ops.exists(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS));
        client.apply(addAddCustomRealmMapper);
        fail("Add custom realm mapper " + TEST_ADD_CUSTOM_REALM_MAPPER_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test
    public void addDuplicateCustomRealmMapperAllowed() throws Exception {
        AddCustomRealmMapper addOperation =
            new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME)
            .className(AddCustomRealmMapperImpl.class.getName())
            .module(CUSTOM_REALM_MAPPER_MODULE_NAME)
            .build();
        AddCustomRealmMapper addOperation2 =
            new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME)
            .className(AddCustomRealmMapperImpl.class.getName())
            .module(CUSTOM_REALM_MAPPER_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .replaceExisting()
            .build();

        client.apply(addOperation);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS));
        client.apply(addOperation2);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS));

        // check whether it was really rewritten
        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        checkAttributeProperties(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS, "configuration", expectedValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealmMapper_nullName() throws Exception {
        new AddCustomRealmMapper.Builder(null)
            .className(AddCustomRealmMapperImpl.class.getName());
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomRealmMapper_emptyName() throws Exception {
        new AddCustomRealmMapper.Builder("")
            .className(AddCustomRealmMapperImpl.class.getName());
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomRealmMapper_noModule() throws Exception {
        AddCustomRealmMapper addAddCustomRealmMapper =
            new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME)
            .className(AddCustomRealmMapperImpl.class.getName())
            .build();

        client.apply(addAddCustomRealmMapper);

        fail("Command should throw exception because Impl class is in non-global module.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealmMapper_noClassName() throws Exception {
        new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME).build();
        fail("Creating command with no classname should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRealmMapper_emptyClassName() throws Exception {
        new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME).className("").build();
        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomRealmMapper_wrongModule() throws Exception {
        AddCustomRealmMapper addAddCustomRealmMapper =
            new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME)
            .className(AddCustomRealmMapperImpl.class.getName())
            .module("wrongModule")
            .build();

        client.apply(addAddCustomRealmMapper);

        fail("Command with wrong module-name should throw exception.");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomRealmMapper_configurationWithException() throws Exception {
        AddCustomRealmMapper addAddCustomRealmMapper =
            new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME)
            .className(AddCustomRealmMapperImpl.class.getName())
            .module(CUSTOM_REALM_MAPPER_MODULE_NAME)
            .addConfiguration("throwException", "parameterValue")
            .build();

        client.apply(addAddCustomRealmMapper);

        fail("Creating command with test configuration should throw exception");
    }

    @Test
    public void addCustomRealmMapper_configuration() throws Exception {
        AddCustomRealmMapper addAddCustomRealmMapper =
            new AddCustomRealmMapper.Builder(TEST_ADD_CUSTOM_REALM_MAPPER_NAME2)
            .className(AddCustomRealmMapperImpl.class.getName())
            .module(CUSTOM_REALM_MAPPER_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .addConfiguration("configParam2", "configParameterValue2")
            .build();

        client.apply(addAddCustomRealmMapper);

        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        expectedValues.add(new Property("configParam2", new ModelNode("configParameterValue2")));
        checkAttributeProperties(TEST_ADD_CUSTOM_REALM_MAPPER_ADDRESS2, "configuration", expectedValues);
    }
}
