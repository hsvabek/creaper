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
public class AddCustomPermissionMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME = "CreaperTestAddCustomPermissionMapper";
    private static final Address TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
        .and("custom-permission-mapper", TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME);
    private static final String TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME2 = "CreaperTestAddCustomPermissionMapper2";
    private static final Address TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
        .and("custom-permission-mapper", TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME2);
    private static final String CUSTOM_PERMISSION_MAPPER_MODULE_NAME = "org.jboss.custompermissionmapperimpl";

    @BeforeClass
    public static void setUp() throws IOException, CommandFailedException, InterruptedException, TimeoutException {
        try (OnlineManagementClient client = createManagementClient()) {
            File testJar1 = createJar("testJar", AddCustomPermissionMapperImpl.class);
            AddModule addModule = new AddModule.Builder(CUSTOM_PERMISSION_MAPPER_MODULE_NAME)
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
            RemoveModule removeModule = new RemoveModule(CUSTOM_PERMISSION_MAPPER_MODULE_NAME);
            client.apply(removeModule);
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addCustomPermission() throws Exception {
        AddCustomPermissionMapper addAddCustomPermissionMapper =
            new AddCustomPermissionMapper.Builder(TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME)
            .className(AddCustomPermissionMapperImpl.class.getName())
            .module(CUSTOM_PERMISSION_MAPPER_MODULE_NAME)
            .addConfiguration("param", "parameterValue")
            .build();

        client.apply(addAddCustomPermissionMapper);

        assertTrue("Add custom permission mapper should be created",
            ops.exists(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS));
    }

    @Test
    public void addCustomPermissions() throws Exception {
        AddCustomPermissionMapper addAddCustomPermissionMapper =
            new AddCustomPermissionMapper.Builder(TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME)
            .className(AddCustomPermissionMapperImpl.class.getName())
            .module(CUSTOM_PERMISSION_MAPPER_MODULE_NAME)
            .build();

        AddCustomPermissionMapper addAddCustomPermissionMapper2 =
            new AddCustomPermissionMapper.Builder(TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME2)
            .className(AddCustomPermissionMapperImpl.class.getName())
            .module(CUSTOM_PERMISSION_MAPPER_MODULE_NAME)
            .build();

        client.apply(addAddCustomPermissionMapper);
        client.apply(addAddCustomPermissionMapper2);

        assertTrue("Add custom permission mapper mapper should be created",
            ops.exists(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS));
        assertTrue("Second add custom permission mapper should be created",
            ops.exists(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS2));

        checkAttribute(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS, "class-name",
            AddCustomPermissionMapperImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS2, "class-name",
            AddCustomPermissionMapperImpl.class.getName());

        administration.reload();

        checkAttribute(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS, "class-name",
            AddCustomPermissionMapperImpl.class.getName());
        checkAttribute(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS2, "class-name",
            AddCustomPermissionMapperImpl.class.getName());
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateCustomPermissionNotAllowed() throws Exception {
        AddCustomPermissionMapper addAddCustomPermissionMapper =
            new AddCustomPermissionMapper.Builder(TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME)
            .className(AddCustomPermissionMapperImpl.class.getName())
            .module(CUSTOM_PERMISSION_MAPPER_MODULE_NAME)
            .build();

        client.apply(addAddCustomPermissionMapper);
        assertTrue("Add custom permission mapper should be created",
            ops.exists(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS));
        client.apply(addAddCustomPermissionMapper);
        fail("Add custom permission mapper " + TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test
    public void addDuplicateCustomPermissionAllowed() throws Exception {
        AddCustomPermissionMapper addOperation =
            new AddCustomPermissionMapper.Builder(TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME)
            .className(AddCustomPermissionMapperImpl.class.getName())
            .module(CUSTOM_PERMISSION_MAPPER_MODULE_NAME)
            .build();
        AddCustomPermissionMapper addOperation2 =
            new AddCustomPermissionMapper.Builder(TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME)
            .className(AddCustomPermissionMapperImpl.class.getName())
            .module(CUSTOM_PERMISSION_MAPPER_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .replaceExisting()
            .build();

        client.apply(addOperation);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS));
        client.apply(addOperation2);
        assertTrue("Add operation should be successful", ops.exists(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS));

        // check whether it was really rewritten
        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        checkAttributeProperties(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS, "configuration", expectedValues);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPermission_nullName() throws Exception {
        new AddCustomPermissionMapper.Builder(null)
            .className(AddCustomPermissionMapperImpl.class.getName());
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddCustomPermissionMapper_emptyName() throws Exception {
        new AddCustomPermissionMapper.Builder("")
            .className(AddCustomPermissionMapperImpl.class.getName());
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomPermission_noModule() throws Exception {
        AddCustomPermissionMapper addAddCustomPermissionMapper =
            new AddCustomPermissionMapper.Builder(TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME)
            .className(AddCustomPermissionMapperImpl.class.getName())
            .build();

        client.apply(addAddCustomPermissionMapper);

        fail("Command should throw exception because Impl class is in non-global module.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPermission_noClassName() throws Exception {
        new AddCustomPermissionMapper.Builder(TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME).build();
        fail("Creating command with no custom should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCustomPermission_emptyClassName() throws Exception {
        new AddCustomPermissionMapper.Builder(TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME)
            .className("")
            .build();

        fail("Creating command with empty classname should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomPermission_wrongModule() throws Exception {
        AddCustomPermissionMapper addAddCustomPermissionMapper =
            new AddCustomPermissionMapper.Builder(TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME)
            .className(AddCustomPermissionMapperImpl.class.getName())
            .module("wrongModule")
            .build();

        client.apply(addAddCustomPermissionMapper);

        fail("Command with wrong module-name should throw exception.");
    }

    @Test(expected = CommandFailedException.class)
    public void addCustomPermission_configurationWithException() throws Exception {
        AddCustomPermissionMapper addAddCustomPermissionMapper =
            new AddCustomPermissionMapper.Builder(TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME)
            .className(AddCustomPermissionMapperImpl.class.getName())
            .module(CUSTOM_PERMISSION_MAPPER_MODULE_NAME)
            .addConfiguration("throwException", "parameterValue")
            .build();

        client.apply(addAddCustomPermissionMapper);

        fail("Creating command with test configuration should throw exception");
    }

    @Test
    public void addCustomPermission_configuration() throws Exception {
        AddCustomPermissionMapper addAddCustomPermissionMapper =
            new AddCustomPermissionMapper.Builder(TEST_ADD_CUSTOM_PERMISSION_MAPPER_NAME2)
            .className(AddCustomPermissionMapperImpl.class.getName())
            .module(CUSTOM_PERMISSION_MAPPER_MODULE_NAME)
            .addConfiguration("configParam1", "configParameterValue")
            .addConfiguration("configParam2", "configParameterValue2")
            .build();

        client.apply(addAddCustomPermissionMapper);

        List<Property> expectedValues = new ArrayList<>();
        expectedValues.add(new Property("configParam1", new ModelNode("configParameterValue")));
        expectedValues.add(new Property("configParam2", new ModelNode("configParameterValue2")));
        checkAttributeProperties(TEST_ADD_CUSTOM_PERMISSION_MAPPER_ADDRESS2, "configuration", expectedValues);
    }
}
