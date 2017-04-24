opendvs:
  core:
    jar_location: salt://opendvs-core-0.0.1-SNAPSHOT.war
    user: opendvs
    group: opendvs
    filesystem_handler_dir: /tmp
    host: 127.0.0.1
    port: 8080
    config:
      - logging.level.me.raska.opendvs = DEBUG
      - spring.http.multipart.max-file-size = 100MB
      - spring.http.multipart.max-request-size = 100MB

  resolver:
    jar_location: salt://opendvs-resolver-0.0.1-SNAPSHOT.jar
    user: opendvs
    group: opendvs
    port: 8081
    config:
      - logging.level.me.raska.opendvs = DEBUG

  probe:
    jar_location: salt://opendvs-worker-probe-0.0.1-SNAPSHOT.jar
    user: opendvs
    group: opendvs
    port: 8082
    concurrency:
      min: 2
      max: 4
    config:
      - logging.level.me.raska.opendvs = DEBUG

  poller:
    jar_location: salt://opendvs-worker-poller-0.0.1-SNAPSHOT.jar
    user: opendvs
    group: opendvs
    port: 8083
    concurrency:
      min: 2
      max: 4
    config:
      - logging.level.me.raska.opendvs = DEBUG

  ui:
    zip_location: salt://ui.zip
    location: /opt/opendvs/ui/

  messaging:
    probe:
      worker: probe_worker
      worker_dl: probe_worker_dl
      core: probe_core
      core_dl: probe_core_dl
    poller:
      worker: poller_worker
      worker_dl: poller_worker_dl
      core: poller_core
      core_dl: poller_core_dl
    resolver: resolver
    resolver_dl: resolver_dl
    fanout: fanout

  rabbitmq:
    vhost: /
    host: 127.0.0.1
    plugins:
      - rabbitmq_shovel
      - rabbitmq_shovel_management
      - rabbitmq_management
    users: 
      core:
        username: core
        password: core
        perms:
          - '.*'
          - '.*'
          - '.*'
      resolver:
        username: resolver
        password: resolver
        perms:
          - '.*'
          - '.*'
          - '.*'
      poller:
        username: poller
        password: poller
        perms:
          - '.*'
          - '.*'
          - '.*'
      probe:
        username: probe
        password: probe
        perms:
          - '.*'
          - '.*'
          - '.*'
      management:
        username: admin
        password: admin
        tags:
          - administrator
        perms:
          - '.*'
          - '.*'
          - '.*'

  mysql:
    database: opendvs
    host: 127.0.0.1
    users:
      core:
        username: core
        password: core
        host: 127.0.0.1
      resolver:
        username: resolver
        password: resolver
        host: 127.0.0.1

  idp:
    client: opendvs
    secret: a16d4ff7-1211-4224-b59c-c9fe21bbce6a
    token_url: http://172.17.0.3:8080/auth/realms/master/protocol/openid-connect/token
    auth_url: http://172.17.0.3:8080/auth/realms/master/protocol/openid-connect/auth
    user_url: http://172.17.0.3:8080/auth/realms/master/protocol/openid-connect/userinfo
    resource: openid
    claim_identifier: email
