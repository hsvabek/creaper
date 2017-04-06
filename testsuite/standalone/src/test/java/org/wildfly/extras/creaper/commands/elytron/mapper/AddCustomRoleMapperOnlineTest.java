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
public class AddCustomRoleMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_ROLE_MAPPER_NAME = "CreaperTestAddCustomRoleMapper";
    private static final Address TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS.and("custom-role-mapper",
        TEST_ADD_CUSTOM_ROLE_MAPPER_NAME);
    private static final String TEST_ADD_CUSTOM_ROLE_MAPPER_NAME2 = "CreaperTestAddCustomRoleMapper2";
    private static final Address TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS.and("custom-role-mapper",
        TEST_ADD_CUSTOM_ROLE_MAPPER_NAME2);
    private static final String CUSTOM_ROLE_MAPPER_MODULE_NAME = "org.jboss.customrolemapperimpl";

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException, InterruptedException, TimeoutException {
        try (OnlineManagementClient client = createManagementClient()) {
            File testJar1 = createJar("testJar", AddCustomRoleMapperImpl.class);
            AddModule addModule = new AddModule.Builder(CUSTOM_ROLE_MAPPER_MODULE_NAME)
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
            RemoveModule removeModule = new RemoveModule(CUSTOM_ROLE_MAPPER_MODULE_NAME);
            client.apply(removeModule);
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addCustomRoleMapper() throws Exception {
        AddCustomRoleMapper addAddCustomRoleMapper = new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME)
            .className(AddCustomRoleMapperImpl.class.getName())
            .module(CUSTOM_ROLE_MAPPER_MODULE_NAME)
            .addConfiguration("param", "parameterValue")
            .build();

        client.apply(addAddCustomRoleMapper);

        assertTrue("Add custom role mapper should be created", ops.exists(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS));
    }

    @Test
    public void addCustomRoleMappers() throws Exception {
        AddCustomRoleMapper addAddCustomRoleMapper = new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME)
            .className(AddCustomRoleMapperImpl.class.getName())
            .module(CUSTOM_ROLE_MAPPER_MODULE_NAME)
            .build();

        AddCustomRoleMapper addAddCustomRoleMapper2 =
            new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME2)
            .className(AddCustomRoleMapperImpl.class.getName())
            .module(CUSTOM_ROLE_MAPPER_MODULE_NAME)
            .build();

        client.apply(addAddCustomRoleMapper);
        client.apply(addAddCustomRoleMapper2);

        assertTrue("Add custom role mapper should be created", ops.exists(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS));
        assertTrue("Second add custom role mapper should be created", ops.exists(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS2));

        checkAttribute(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS, "class-name", AddCustomRoleMapperImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS2, "class-name", AddCustomRoleMapperImpl.class.getName());

        administration.reload();

        checkAttribute(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS, "class-name", AddCustomRoleMapperImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS2, "class-name", AddCustomRoleMapperImpl.class.getName());
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateCustomRoleMapperNotAllowed() throws Exception {
        AddCustomRoleMapper addAddCustomRoleMapper = new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME)
            .className(AddCustomRoleMapperImpl.class.getName())
            .module(CUSTOM_ROLE_MAPPER_MODULE_NAME)
            .build();

        client.apply(addAddCustomRoleMapper);
        assertTrue("Add custom role mapper should be created", ops.exists(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS));
        client.apply(addAddCustomRoleMapper);
        fail("Add custom role mapper " + TEST_ADD_CUSTOM_ROLE_MAPPER_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test
    public void addDuplicateCustomRoleMapperAllowed() throws Exception {
        AddCustomRoleMapper addOperation =
            new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME)
            .className(AddCustomRoleMapperImpl.class.getName())
            .module(CUSTOM_ROLE_MAPPER_MODULE_NAME)
            .build();
        AddCustomRoleMapper addOperation2 =
            new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME)
            .className(AddCustomRoleMapperImpl.class.getName())
            .module(CUSTOM_ROLE_MAPPER_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .replaceExisting()
            .build();

        client.apply(addOperation);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS));
        client.apply(addOperation2);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS));

        // check whether it was really rewritten
        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        checkAttributeProperties(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS, "configuration", expectedValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleMapper_nullName() throws Exception {
        new AddCustomRoleMapper.Builder(null)
            .className(AddCustomRoleMapperImpl.class.getName()).build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomRoleMapper_emptyName() throws Exception {
        new AddCustomRoleMapper.Builder("")
            .className(AddCustomRoleMapperImpl.class.getName()).build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomRoleMapper_noModule() throws Exception {
        AddCustomRoleMapper addAddCustomRoleMapper = new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME)
            .className(AddCustomRoleMapperImpl.class.getName()).build();

        client.apply(addAddCustomRoleMapper);

        fail("Command should throw exception because Impl class is in non-global module.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleMapper_noClassName() throws Exception {
        new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME).build();
        fail("Creating command with no custom should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomRoleMapper_emptyClassName() throws Exception {
        new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME).className("").build();
        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomRoleMapper_wrongModule() throws Exception {
        AddCustomRoleMapper addAddCustomRoleMapper = new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME)
            .className(AddCustomRoleMapperImpl.class.getName())
            .module("wrongModule")
            .build();

        client.apply(addAddCustomRoleMapper);

        fail("Command with wrong module-name should throw exception.");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomRoleMapper_configurationWithException() throws Exception {
        AddCustomRoleMapper addAddCustomRoleMapper = new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME)
            .className(AddCustomRoleMapperImpl.class.getName())
            .module(CUSTOM_ROLE_MAPPER_MODULE_NAME)
            .addConfiguration("throwException", "parameterValue")
            .build();

        client.apply(addAddCustomRoleMapper);

        fail("Creating command with test configuration should throw exception");
    }

    @Test
    public void addCustomRoleMapper_configuration() throws Exception {
        AddCustomRoleMapper addAddCustomRoleMapper = new AddCustomRoleMapper.Builder(TEST_ADD_CUSTOM_ROLE_MAPPER_NAME2)
            .className(AddCustomRoleMapperImpl.class.getName())
            .module(CUSTOM_ROLE_MAPPER_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .addConfiguration("configParam2", "configParameterValue2")
            .build();

        client.apply(addAddCustomRoleMapper);

        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        expectedValues.add(new Property("configParam2", new ModelNode("configParameterValue2")));
        checkAttributeProperties(TEST_ADD_CUSTOM_ROLE_MAPPER_ADDRESS2, "configuration", expectedValues);
    }
}
