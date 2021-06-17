
### About

The repository contains a simple Nuxeo plugin and associated package allowing to use HxP IDP with Nuxeo.

### Quick build

    mvn -nsu -DskipTests clean package

### Nuxeo Deployment

#### Bundles

3 bundles need to be deployed

**nuxeo-platform-login-openid**

Contains the OpenID authentication framework for Nuxeo.

**nuxeo-usermapper**

Contains the service allowing to achieve mapping between Nuxeo users and IDP users.

**nuxeo-hxp-idp-plugin**

Contains the Nuxeo configuration to enable OpenID integration with HxP IDP.

#### Package

The provided package will install the 3 bundles and a configuration template defining the default IDP endpoint.

    bin/nuxeoctl mp-install nuxeo-hxp-idp-package/target/nuxeo-hxp-idp-package-1.0-SNAPSHOT.zip

### Configuration

#### IDP configuration

The OpenID configuration is embedded inside the plugin, but leverage `nuxeo.conf` variable expension.

      <authorizationServerURL>${org.nuxeo.hxp.idp.endpoint}/connect/authorize</authorizationServerURL>
      <tokenServerURL>${org.nuxeo.hxp.idp.endpoint}/connect/token</tokenServerURL>
      <userInfoURL>${org.nuxeo.hxp.idp.endpoint}/connect/userinfo</userInfoURL>
          
      <clientId>${org.nuxeo.hxp.idp.client.id}</clientId>  
      <clientSecret>${org.nuxeo.hxp.idp.client.secret}</clientSecret>

#### Using IDP HXP Mockup

Here is a sample `nuxeo.conf` extract that matched the tests config:

	org.nuxeo.hxp.idp.endpoint=http://127.0.0.1:5002/idp
	org.nuxeo.hxp.idp.client.id=nuxeo-client
	org.nuxeo.hxp.idp.client.secret=secret

The corresponding configuration file for the HxP IDP Mockup can be found [here](sample-config/hxp-idp-config.json).
To generate the encoded secret:

    echo -n secret | openssl dgst -binary -sha256 | base64

#### Enable user creation

Once Nuxeo receive the answer from the IDP it will try to find the corresponding user in the Nuxeo user directory.
By default, if the user is not found it will not be created.

To enable creation, add this line to nuxeo.conf:

    nuxeo.oauth.auth.create.user=true

