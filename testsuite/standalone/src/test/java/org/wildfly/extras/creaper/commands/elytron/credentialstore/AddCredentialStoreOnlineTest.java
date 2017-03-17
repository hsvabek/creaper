package org.wildfly.extras.creaper.commands.elytron.credentialstore;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddCredentialStoreOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_CREDENTIAL_STORE_NAME = "CreaperTestCredentialStore";
    private static final Address TEST_CREDENTIAL_STORE_ADDRESS = SUBSYSTEM_ADDRESS
            .and("credential-store", TEST_CREDENTIAL_STORE_NAME);
    private static final String TEST_CREDENTIAL_STORE_NAME2 = "CreaperTestCredentialStore2";
    private static final Address TEST_CREDENTIAL_STORE_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("credential-store", TEST_CREDENTIAL_STORE_NAME2);

    private static final String CREDENTIAL_STORE_URI_PREFIX = "cr-store://";
    private static final String TEST_PASSWORD = "somePassword";

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_CREDENTIAL_STORE_ADDRESS);
        ops.removeIfExists(TEST_CREDENTIAL_STORE_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleCredentialStore() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        client.apply(addCredentialStore);

        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));
    }

    @Test
    public void addFullCredentialStoreClearText() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                .providers("elytron")
                .providerName("WildFlyElytron")
                .otherProviders("elytron")
                .relativeTo("jboss.server.data.dir")
                .type("KeyStoreCredentialStore")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        client.apply(addCredentialStore);
        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));

        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "uri", CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "relative-to", "jboss.server.data.dir");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "type", "KeyStoreCredentialStore");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "credential-reference.clear-text", TEST_PASSWORD);
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "providers", "elytron");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "provider-name", "WildFlyElytron");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "other-providers", "elytron");
    }

    @Test
    public void addFullCredentialStoreAliasStore() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                .relativeTo("jboss.server.data.dir")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        AddCredentialStoreAlias addCredentialStoreAlias = new AddCredentialStoreAlias.Builder("alias")
                .credentialStore(TEST_CREDENTIAL_STORE_NAME)
                .secretValue("secret")
                .build();
        client.apply(addCredentialStore, addCredentialStoreAlias);
        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));

        addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME2)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                //                .provider("someProvider") TODO once AddProviderLoader is available
                //                .providerLoader("someLoader") TODO once AddProviderLoader is available
                .relativeTo("jboss.server.data.dir")
                .type("KeyStoreCredentialStore")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .alias("alias")
                        .store(TEST_CREDENTIAL_STORE_NAME)
                        .build())
                .build();

        client.apply(addCredentialStore);
        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS2));

        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS2, "uri", CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS2, "relative-to", "jboss.server.data.dir");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS2, "type", "KeyStoreCredentialStore");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS2, "credential-reference.alias", "alias");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS2, "credential-reference.store", TEST_CREDENTIAL_STORE_NAME);
    }

    @Test
    public void addTwoCredentialStores() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        AddCredentialStore addCredentialStore2 = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME2)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        client.apply(addCredentialStore);
        client.apply(addCredentialStore2);

        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));
        assertTrue("Second credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistCredentialStoreNotAllowed() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        AddCredentialStore addCredentialStore2 = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        client.apply(addCredentialStore);
        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));
        client.apply(addCredentialStore2);
        fail("Credential store CreaperTestCredentialStore already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistCredentialStoreAllowed() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        AddCredentialStore addCredentialStore2 = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.otherCs?create=true")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .replaceExisting()
                .build();

        client.apply(addCredentialStore);
        assertTrue("Constant permission mapper should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));
        client.apply(addCredentialStore2);
        assertTrue("Constant permission mapper should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));

        // check whether it was really rewritten
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "uri", CREDENTIAL_STORE_URI_PREFIX + "test.otherCs?create=true");
    }

    @Test(expected = CommandFailedException.class)
    public void addCredentialStore_uriWithoutCSPrefix() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri("test.testCs?create=true")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        client.apply(addCredentialStore);
        fail("It is not possible to define credential store's uri without cr-store:// prefix");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCredentialStore_nullName() throws Exception {
        new AddCredentialStore.Builder(null)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCredentialStore_emptyName() throws Exception {
        new AddCredentialStore.Builder("")
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCredentialStore_nullUri() throws Exception {
        new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri(null)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();
        fail("Creating command with null uri should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCredentialStore_emptyUri() throws Exception {
        new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri("")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();
        fail("Creating command with empty uri should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCredentialStore_nullCredentialReference() throws Exception {
        new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                .credentialReference(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCredentialStore_insufficientCredentialReference() throws Exception {
        new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .uri(CREDENTIAL_STORE_URI_PREFIX + "test.testCs?create=true")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .alias("someAlias")
                        .build())
                .build();
    }
}