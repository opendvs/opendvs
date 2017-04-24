include:
  - java

{% if pillar['opendvs']['poller']['user'] != pillar['opendvs']['poller']['group'] %}
poller-group-{{ pillar['opendvs']['poller']['group'] }}:
  group.present:
    - name: {{ pillar['opendvs']['poller']['group'] }}
    - require_in:
      - file: /opt/opendvs/poller.application.properties
      - user: poller-user-{{ pillar['opendvs']['poller']['user'] }}
{% endif %}

poller-user-{{ pillar['opendvs']['poller']['user'] }}:
  user.present:
    - name: {{ pillar['opendvs']['poller']['user'] }}
    - require_in:
      - file: /opt/opendvs/poller.application.properties
{% if pillar['opendvs']['poller']['user'] != pillar['opendvs']['poller']['group'] %}
    - groups:
      - {{ pillar['opendvs']['poller']['group'] }}
{% endif %}

/opt/opendvs/poller.application.properties:
  file.managed:
    - source: salt://opendvs/files/poller.properties.jinja
    - user: {{ pillar['opendvs']['poller']['user'] }}
    - group: {{ pillar['opendvs']['poller']['group'] }}
    - mode: 700
    - template: jinja
    - makedirs: True
    - context: {{ pillar['opendvs'] }}
    - dir_mode: 755

/etc/systemd/system/opendvs-poller.service:
  file.managed:
    - source: salt://opendvs/files/application.service.jinja
    - user: root
    - group: root
    - mode: 644
    - template: jinja
    - context:
        application_name: opendvs-poller
        application_jar: /opt/opendvs/poller.jar
        user: {{ pillar['opendvs']['poller']['user'] }}
        application_properties: /opt/opendvs/poller.application.properties
        java_opts: {{ pillar['opendvs']['poller'].get("java_opts", "") }}

/opt/opendvs/poller.jar:
  file.managed:
    - source: {{ pillar['opendvs']['poller']['jar_location'] }}
    - user: {{ pillar['opendvs']['poller']['user'] }}
    - group: {{ pillar['opendvs']['poller']['group'] }}
    - makedirs: True
    - dir_mode: 755

opendvs-poller:
  service.running:
    - enable: True
    - restart: True
    - watch:
      - file: /etc/systemd/system/opendvs-poller.service
      - file: /opt/opendvs/poller.application.properties
      - file: /opt/opendvs/poller.jar
       
    - require:
      - file: /etc/systemd/system/opendvs-poller.service
      - file: /opt/opendvs/poller.application.properties
      - file: /opt/opendvs/poller.jar
      - sls: java

maven:
  pkg.installed
