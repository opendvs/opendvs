include:
  - java

{% if pillar['opendvs']['core']['user'] != pillar['opendvs']['core']['group'] %}
core-group-{{ pillar['opendvs']['core']['group'] }}:
  group.present:
    - name: {{ pillar['opendvs']['core']['group'] }}
    - require_in:
      - file: /opt/opendvs/core.application.properties
      - user: core-user-{{ pillar['opendvs']['core']['user'] }}
{% endif %}

core-user-{{ pillar['opendvs']['core']['user'] }}:
  user.present:
    - name: {{ pillar['opendvs']['core']['user'] }}
    - require_in:
      - file: /opt/opendvs/core.application.properties
{% if pillar['opendvs']['core']['user'] != pillar['opendvs']['core']['group'] %}
    - groups:
      - {{ pillar['opendvs']['core']['group'] }}
{% endif %}

/opt/opendvs/core.application.properties:
  file.managed:
    - source: salt://opendvs/files/core.properties.jinja
    - user: {{ pillar['opendvs']['core']['user'] }}
    - group: {{ pillar['opendvs']['core']['group'] }}
    - makedirs: True
    - mode: 700
    - template: jinja
    - context: {{ pillar['opendvs'] }}
    - dir_mode: 755

/etc/systemd/system/opendvs-core.service:
  file.managed:
    - source: salt://opendvs/files/application.service.jinja
    - user: root
    - group: root
    - mode: 644
    - template: jinja
    - context:
        application_name: opendvs-core
        application_jar: /opt/opendvs/core.jar
        user: {{ pillar['opendvs']['core']['user'] }}
        application_properties: /opt/opendvs/core.application.properties
        java_opts: {{ pillar['opendvs']['core'].get("java_opts", "") }}

/opt/opendvs/core.jar:
  file.managed:
    - source: {{ pillar['opendvs']['core']['jar_location'] }}
    - user: {{ pillar['opendvs']['core']['user'] }}
    - group: {{ pillar['opendvs']['core']['group'] }}
    - makedirs: True
    - dir_mode: 755

opendvs-core:
  service.running:
    - enable: True
    - restart: True
    - watch:
      - file: /etc/systemd/system/opendvs-core.service
      - file: /opt/opendvs/core.application.properties
      - file: /opt/opendvs/core.jar
       
    - require:
      - file: /etc/systemd/system/opendvs-core.service
      - file: /opt/opendvs/core.application.properties
      - file: /opt/opendvs/core.jar
      - sls: java
