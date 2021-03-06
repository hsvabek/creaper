package org.wildfly.extras.creaper.commands.elytron.authenticationclient;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.commands.elytron.domain.AddSecurityDomain;
import org.wildfly.extras.creaper.commands.elytron.realm.AddFilesystemRealm;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddAuthenticationConfigurationOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_AUTHENTICATION_CONFIGURATION_NAME = "CreaperTestAuthenticationConfiguration";
    private static final Address TEST_AUTHENTICATION_CONFIGURATION_ADDRESS = SUBSYSTEM_ADDRESS
            .and("authentication-configuration", TEST_AUTHENTICATION_CONFIGURATION_NAME);
    private static final String TEST_AUTHENTICATION_CONFIGURATION_NAME2 = "CreaperTestAuthenticationConfiguration2";
    private static final Address TEST_AUTHENTICATION_CONFIGURATION_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("authentication-configuration", TEST_AUTHENTICATION_CONFIGURATION_NAME2);

    private static final String TEST_SECURITY_DOMAIN_NAME = "CreaperTestSecurityDomain";
    private static final Address TEST_SECURITY_DOMAIN_ADDRESS = SUBSYSTEM_ADDRESS
            .and("security-domain", TEST_SECURITY_DOMAIN_NAME);
    private static final String TEST_FILESYSTEM_REALM_NAME = "CreaperTestFilesystemRealm";
    private static final Address TEST_FILESYSTEM_REALM_ADDRESS = SUBSYSTEM_ADDRESS
            .and("filesystem-realm", TEST_FILESYSTEM_REALM_NAME);
    private final AddFilesystemRealm addFilesystemRealm = new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME)
            .path("/path/to/filesystem")
            .build();

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS);
        ops.removeIfExists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS2);
        ops.removeIfExists(TEST_SECURITY_DOMAIN_ADDRESS);
        ops.removeIfExists(TEST_FILESYSTEM_REALM_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleAuthenticationConfiguration() throws Exception {
        AddAuthenticationConfiguration addAuthenticationConfiguration
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .build();

        client.apply(addAuthenticationConfiguration);

        assertTrue("Authentication Configuration should be created",
                ops.exists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS));
    }

    @Test
    public void addTwoAuthenticationConfigurations() throws Exception {
        AddAuthenticationConfiguration addAuthenticationConfiguration
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .build();

        AddAuthenticationConfiguration addAuthenticationConfiguration2
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME2)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .build();

        client.apply(addAuthenticationConfiguration);
        client.apply(addAuthenticationConfiguration2);

        assertTrue("Authentication Configuration should be created",
                ops.exists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS));
        assertTrue("Second Authentication Configuration should be created",
                ops.exists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS2));
    }

    @Test
    public void addFullAuthenticationConfiguration() throws Exception {
        AddAuthenticationConfiguration addAuthenticationConfiguration2
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME2)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword2")
                        .build())
                .build();
        client.apply(addAuthenticationConfiguration2);

        AddAuthenticationConfiguration addAuthenticationConfiguration
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .addMechanismProperties(new AddAuthenticationConfiguration.Property("property1", "value1"),
                        new AddAuthenticationConfiguration.Property("property2", "value2"))
                .extend(TEST_AUTHENTICATION_CONFIGURATION_NAME2)
                .authenticationName("someAuthenticationName")
                .authorizationName("someAuthorizationName")
                .host("someHost")
                .protocol("someProtocol")
                .port(12345)
                .realm("someRealm")
                .allowAllMechanisms(true)
                .addForbidSaslMechanisms("someSaslMechanism1", "someSaslMechanism2")
                .build();
        client.apply(addAuthenticationConfiguration);

        assertTrue("Authentication Configuration should be created",
                ops.exists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS));

        checkAttribute("extends", TEST_AUTHENTICATION_CONFIGURATION_NAME2);
        checkAttribute("authentication-name", "someAuthenticationName");
        checkAttribute("authorization-name", "someAuthorizationName");
        checkAttribute("host", "someHost");
        checkAttribute("protocol", "someProtocol");
        checkAttribute("port", "12345");
        checkAttribute("realm", "someRealm");
        checkAttribute("allow-all-mechanisms", "true");
        checkAttribute("forbid-sasl-mechanisms[0]", "someSaslMechanism1");
        checkAttribute("forbid-sasl-mechanisms[1]", "someSaslMechanism2");
        checkAttribute("credential-reference.clear-text", "somePassword");
        checkAttribute("mechanism-properties.property1", "value1");
        checkAttribute("mechanism-properties.property2", "value2");
    }

    @Test
    public void addAuthenticationConfiguration_allowSaslMechanisms() throws Exception {
        AddAuthenticationConfiguration addAuthenticationConfiguration
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .addAllowSaslMechanisms("someSaslMechanism1", "someSaslMechanism2")
                .build();
        client.apply(addAuthenticationConfiguration);

        assertTrue("Authentication Configuration should be created",
                ops.exists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS));

        checkAttribute("allow-sasl-mechanisms[0]", "someSaslMechanism1");
        checkAttribute("allow-sasl-mechanisms[1]", "someSaslMechanism2");
    }

    @Test
    public void addAuthenticationConfiguration_anonymous() throws Exception {
        AddAuthenticationConfiguration addAuthenticationConfiguration
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .anonymous(false)
                .build();
        client.apply(addAuthenticationConfiguration);

        assertTrue("Authentication Configuration should be created",
                ops.exists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS));

        checkAttribute("anonymous", "false");
    }

    @Test
    public void addAuthenticationConfiguration_securityDomain() throws Exception {
        client.apply(addFilesystemRealm);

        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME)
                        .build())
                .build();
        client.apply(addSecurityDomain);

        AddAuthenticationConfiguration addAuthenticationConfiguration
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .build();
        client.apply(addAuthenticationConfiguration);

        assertTrue("Authentication Configuration should be created",
                ops.exists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS));

        checkAttribute("security-domain", TEST_SECURITY_DOMAIN_NAME);
    }

    @Test(expected = CommandFailedException.class)
    public void addExistAuthenticationConfigurationNotAllowed() throws Exception {
        AddAuthenticationConfiguration addAuthenticationConfiguration
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .host("someHost1")
                .build();

        AddAuthenticationConfiguration addAuthenticationConfiguration2
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .host("someHost2")
                .build();

        client.apply(addAuthenticationConfiguration);
        assertTrue("Authentication Configuration should be created",
                ops.exists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS));
        client.apply(addAuthenticationConfiguration2);
        fail("Authentication Configuration CreaperTestAuthenticationConfiguration already exists in configuration, exception should be thrown");

    }

    @Test
    public void addExistAuthenticationConfigurationAllowed() throws Exception {
        AddAuthenticationConfiguration addAuthenticationConfiguration
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .host("someHost1")
                .build();

        AddAuthenticationConfiguration addAuthenticationConfiguration2
                = new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .host("someHost2")
                .replaceExisting()
                .build();

        client.apply(addAuthenticationConfiguration);
        assertTrue("Authentication Configuration should be created",
                ops.exists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS));
        client.apply(addAuthenticationConfiguration2);
        assertTrue("Authentication Configuration should be created",
                ops.exists(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS));
        checkAttribute("host", "someHost2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAuthenticationConfiguration_nullName() throws Exception {
        new AddAuthenticationConfiguration.Builder(null)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAuthenticationConfiguration_emptyName() throws Exception {
        new AddAuthenticationConfiguration.Builder("")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAuthenticationConfiguration_allowAllMechanismsAndAllowSaslMechanisms() throws Exception {
        new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .addAllowSaslMechanisms("SomeMechanism")
                .allowAllMechanisms(true)
                .build();
        fail("Creating command with both allowAllMechanisms and allowSaslMechanisms should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAuthenticationConfiguration_authenticationNameAndAnonymous() throws Exception {
        new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .authenticationName("someAuthenticationName")
                .anonymous(true)
                .build();
        fail("Creating command with both authenticationName and anonymous should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAuthenticationConfiguration_authenticationNameAndSecurityDomain() throws Exception {
        new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .authenticationName("someAuthenticationName")
                .securityDomain("someSecurityDomain")
                .build();
        fail("Creating command with both authenticationName and securityDomain should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAuthenticationConfiguration_anonymousAndSecurityDomain() throws Exception {
        new AddAuthenticationConfiguration.Builder(TEST_AUTHENTICATION_CONFIGURATION_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .anonymous(true)
                .securityDomain("someSecurityDomain")
                .build();
        fail("Creating command with both anonymous and securityDomain should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        checkAttribute(TEST_AUTHENTICATION_CONFIGURATION_ADDRESS, attribute, expectedValue);
    }

}
