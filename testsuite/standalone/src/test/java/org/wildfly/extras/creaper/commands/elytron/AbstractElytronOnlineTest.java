package org.wildfly.extras.creaper.commands.elytron;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.Constants;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

public abstract class AbstractElytronOnlineTest {

    protected OnlineManagementClient client;
    protected Operations ops;
    protected Administration administration;
    private static boolean removeSubsystemAtTheEnd = false;

    protected static final Address SUBSYSTEM_ADDRESS = Address.subsystem("elytron");

    @BeforeClass
    public static void addSubsystem() throws Exception {
        try (OnlineManagementClient client = createManagementClient()) {
            Operations ops = new Operations(client);
            if (!ops.exists(SUBSYSTEM_ADDRESS)) {
                AddExtensionAndSubsystem addExtensionAndSubsystem = new AddExtensionAndSubsystem();
                client.apply(addExtensionAndSubsystem);
                removeSubsystemAtTheEnd = true;
            }
            assertTrue("The Elytron subsystem must exist.", ops.exists(SUBSYSTEM_ADDRESS));
        }
    }

    @AfterClass
    public static void removeSubsystem() throws Exception {
        try (OnlineManagementClient client = createManagementClient()) {
            Operations ops = new Operations(client);
            Administration administration = new Administration(client);
            if (removeSubsystemAtTheEnd && ops.exists(SUBSYSTEM_ADDRESS)) {
                RemoveExtensionAndSubsystem removeExtensionAndSubsystem = new RemoveExtensionAndSubsystem();
                client.apply(removeExtensionAndSubsystem);
                //TODO This is a workaround for JBEAP-5955; Remove this line once the issue is fixed
                administration.reload();
                assertFalse("The Elytron subsystem should not be present anymore.",
                        ops.exists(SUBSYSTEM_ADDRESS));
            }
        }
    }

    @Before
    public void setupCreaperForTest() throws IOException {
        client = createManagementClient();
        assumeTrue("The test requires Elytron (since WildFly 11)",
                client.version().greaterThanOrEqualTo(ServerVersion.VERSION_5_0_0));

        ops = new Operations(client);
        administration = new Administration(client);
    }

    @After
    public void tearDownCreaperForTest() throws IOException {
        client.close();
    }

    protected static OnlineManagementClient createManagementClient() throws IOException {
        return ManagementClient.online(OnlineOptions.standalone().localDefault().build());
    }

    protected void removeAllElytronChildrenType(final String childrenType) throws IOException, OperationException {
        Operations ops = new Operations(client);
        ModelNodeResult result = ops.readChildrenNames(SUBSYSTEM_ADDRESS, childrenType);
        List<String> realmNames = result.stringListValue();

        for (String realmName : realmNames) {
            final Address realmAddress = SUBSYSTEM_ADDRESS.and(childrenType, realmName);

            ops.removeIfExists(realmAddress);
        }
    }

    protected void checkAttribute(Address address, String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }

    protected void checkAttribute(Address address, String attribute, List<String> expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return unexpected value", expectedValue,
                readAttribute.stringListValue());
    }

    protected void checkAttributeObject(Address address, String attribute, String objectProperty, String expectedValue)
        throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
            readAttribute.asObject().get(Constants.RESULT).get(objectProperty).asString());
    }

    protected void checkAttributeProperties(Address address, String attribute, List<Property> expectedValues)
        throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        ModelNode result = readAttribute.get(Constants.RESULT);
        List<Property> propertyList = result.asPropertyList();

        if (propertyList.size() != expectedValues.size()) {
            fail("Configuration properties size must be same as expected values size. Was [" + propertyList.size()
                + "] and matches [" + expectedValues.size() + "]");
        }

        int numberOfMatches = 0;
        for (Property property : propertyList) {
            for (Property expected : expectedValues) {
                if (property.getName().equals(expected.getName()) && property.getValue().equals(expected.getValue())) {
                    numberOfMatches++;
                }
            }
        }

        if (propertyList.size() != numberOfMatches) {
            fail("Configuration properties size must be same as number of matches. Was [" + propertyList.size()
                + "] and matches [" + numberOfMatches + "]");
        }
    }

    /**
     *
     * @param namePrefix - prefix of JAR name
     * @param classes - classes which will be added to JAR
     * @return - JAR file
     * @throws IOException - exception
     */
    protected static File createJar(String namePrefix, Class<?>... classes) throws IOException {
        File testJar = File.createTempFile(namePrefix, ".jar");
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
            .addClasses(classes);
        // TODO hsvabek 2017_03_06 only for custom implementation manual smoke tests....
        jar.addClass(CustomImplInvocationRuntimeException.class);
        jar.as(ZipExporter.class).exportTo(testJar, true);
        return testJar;
    }
}
