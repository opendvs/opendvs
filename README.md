# OpenDVS

This platform serves as dependency analyzer of applications and determine their states. It detects outdated components with regards to default repositories (ATM) and vulnerabilities against NVD.

## Building
For building the platform ensure you have Maven and Java installed. To build all components you can issue 

    mvn clean install
on the top-level project. All components will be available in their `target/` folders as runnable Java archives, except for JavaScript which will be bundled in `opendvs-react-ui/target/opendvs-react-ui-<VERSION>-bundle.zip`. 
> Note that due to used [Node.js Maven plugin](https://github.com/eirslett/frontend-maven-plugin) Maven `clean` goal will not remove downloaded Node modules. If you want to re-download all dependencies, please remove folder `opendvs-react-ui/node_modules` and then rebuild the project

## Deployment
As all backend applications are using Spring Boot and are shipped with default application.properties, you can modify configuration properties by providing alternative [configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).

As a supporting application ensure you have AMQP 0.9-compatible broker (**RabbitMQ** is recommended and tested) and Hibernate-supported SQL database (**MySQL** and forks are recommended and tested). DB schema migrations are executed by Liquibase on startup and first user will be granted admin rights.

For authentication purposes you are required to configure **OpenID Connect-compatible identity provider** (e.g. GitHub). For testing purposes and standalone deployment you can use [Keycloak](http://www.keycloak.org/), which can be used as federation hub or connected directly to AD/LDAP.

### Reverse proxy
To configure reverse proxy, you need to forward following context-paths to `opendvs-core`:
* /logout
* /login
* /api
* /event

In case you want to run OpenDVS at custom context-path, adjust new paths in `opendvs-react-ui/src/main/js/config.js` and rebuild React frontend. For proper redirects handling, you also need to specify proper `server.context-path` property for `opendvs-core` component.

Following nginx snippet can be used as configuration basis:
```
location /api {
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-Host $host:$server_port;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $http_connection;
        proxy_set_header X-Forwarded-Server $host;

        proxy_pass http://127.0.0.1:8080/api;
}
```

### Salt Formulas
For easier platform deployment, SaltStack states are available inside `docs/salt/state`. Appropriate pillars are available in `docs/salt/pillar`. Currently supported OS are Ubuntu and CentOS, systemd is currently only supported init.

All parts of platform can be applied by triggering highstate for `opendvs.<component>` state. All required runtime dependencies will be installed (as per supplied pillars), except MQ broker and SQL database.

> For installing Oracle Java you need to accept *Oracle Binary Code License Agreement* by specifying `java:accept_licence: True* pillar. Otherwise you need to ensure Java is installed on target machine!

#### RabbitMQ
RabbitMQ can be installed on separate server, all necessary configuration parameters will be set up after installation (users, virtual hosts and management interface).
#### MySQL
MySQL can be installed on separate server, database and users will be provisioned after installation.

#### Nginx
Nginx is requirement for `opendvs-ui` component and has to reside on same server (due to static files serving). Correct proxying on required endpoints will be configured according to supplied pillars. 


### Current limitations
For `FilesystemProjectTypeHandler` you need to ensure shared drive between `opendvs-core` and `opendvs-worker-probe` components. Custom path can be specified by supplying custom `project.handler.filesystem.dir` configuration property for `opendvs-core`. (FIXME allow usage of object storage - S3 for example)

## Architecture
See [Architecture](docs/Architecture.md).

## Extending functionality
* [Implementing custom Probes](docs/CustomProbes.md).
* [Implementing custom Pollers](docs/CustomPollers.md).

## Prefetching CVE data
In order to properly detect vulnerabilities, you need to ensure CVE database is fetched first (FIXME). After platform is up and running, trigger following API endpoint with admin user to force CVE synchronization: `/api/v1/vulnerabilities/cve/trigger?modifier=<YEAR>`
## Testing the functionality
Few test binaries are supplied with the source code (`docs/dist` directory) to verify proper functionality:
* *vulnerable.zip* - detect vulnerable Maven package, CVE is from 2017 dataset
* TODO
