include:
  - java

{% if pillar['opendvs']['probe']['user'] != pillar['opendvs']['probe']['group'] %}
probe-group-{{ pillar['opendvs']['probe']['group'] }}:
  group.present:
    - name: {{ pillar['opendvs']['probe']['group'] }}
    - require_in:
      - file: /opt/opendvs/probe.application.properties
      - user: probe-user-{{ pillar['opendvs']['probe']['user'] }}
{% endif %}

probe-user-{{ pillar['opendvs']['probe']['user'] }}:
  user.present:
    - name: {{ pillar['opendvs']['probe']['user'] }}
    - require_in:
      - file: /opt/opendvs/probe.application.properties
{% if pillar['opendvs']['probe']['user'] != pillar['opendvs']['probe']['group'] %}
    - groups:
      - {{ pillar['opendvs']['probe']['group'] }}
{% endif %}

/opt/opendvs/probe.application.properties:
  file.managed:
    - source: salt://opendvs/files/probe.properties.jinja
    - user: {{ pillar['opendvs']['probe']['user'] }}
    - group: {{ pillar['opendvs']['probe']['group'] }}
    - mode: 700
    - template: jinja
    - context: {{ pillar['opendvs'] }}
    - makedirs: True
    - dir_mode: 755

/etc/systemd/system/opendvs-probe.service:
  file.managed:
    - source: salt://opendvs/files/application.service.jinja
    - user: root
    - group: root
    - mode: 644
    - template: jinja
    - context:
        application_name: opendvs-probe
        application_jar: /opt/opendvs/probe.jar
        user: {{ pillar['opendvs']['probe']['user'] }}
        application_properties: /opt/opendvs/probe.application.properties
        java_opts: {{ pillar['opendvs']['probe'].get("java_opts", "") }}

/opt/opendvs/probe.jar:
  file.managed:
    - source: {{ pillar['opendvs']['probe']['jar_location'] }}
    - user: {{ pillar['opendvs']['probe']['user'] }}
    - group: {{ pillar['opendvs']['probe']['group'] }}
    - makedirs: True
    - dir_mode: 755

opendvs-probe:
  service.running:
    - enable: True
    - restart: True
    - watch:
      - file: /etc/systemd/system/opendvs-probe.service
      - file: /opt/opendvs/probe.application.properties
      - file: /opt/opendvs/probe.jar
       
    - require:
      - file: /etc/systemd/system/opendvs-probe.service
      - file: /opt/opendvs/probe.application.properties
      - file: /opt/opendvs/probe.jar
      - sls: java
