include:
  - java

{% if pillar['opendvs']['resolver']['user'] != pillar['opendvs']['resolver']['group'] %}
resolver-group-{{ pillar['opendvs']['resolver']['group'] }}:
  group.present:
    - name: {{ pillar['opendvs']['resolver']['group'] }}
    - require_in:
      - file: /opt/opendvs/resolver.application.properties
      - user: resolver-user-{{ pillar['opendvs']['resolver']['user'] }}
{% endif %}

resolver-user-{{ pillar['opendvs']['resolver']['user'] }}:
  user.present:
    - name: {{ pillar['opendvs']['resolver']['user'] }}
    - require_in:
      - file: /opt/opendvs/resolver.application.properties
{% if pillar['opendvs']['resolver']['user'] != pillar['opendvs']['resolver']['group'] %}
    - groups:
      - {{ pillar['opendvs']['resolver']['group'] }}
{% endif %}

/opt/opendvs/resolver.application.properties:
  file.managed:
    - source: salt://opendvs/files/resolver.properties.jinja
    - user: {{ pillar['opendvs']['resolver']['user'] }}
    - group: {{ pillar['opendvs']['resolver']['group'] }}
    - mode: 700
    - template: jinja
    - context: {{ pillar['opendvs'] }}
    - makedirs: True
    - dir_mode: 755

/etc/systemd/system/opendvs-resolver.service:
  file.managed:
    - source: salt://opendvs/files/application.service.jinja
    - user: root
    - group: root
    - mode: 644
    - template: jinja
    - context:
        application_name: opendvs-resolver
        application_jar: /opt/opendvs/resolver.jar
        user: {{ pillar['opendvs']['resolver']['user'] }}
        application_properties: /opt/opendvs/resolver.application.properties
        java_opts: {{ pillar['opendvs']['resolver'].get("java_opts", "") }}

/opt/opendvs/resolver.jar:
  file.managed:
    - source: {{ pillar['opendvs']['resolver']['jar_location'] }}
    - user: {{ pillar['opendvs']['resolver']['user'] }}
    - group: {{ pillar['opendvs']['resolver']['group'] }}
    - makedirs: True
    - dir_mode: 755

opendvs-resolver:
  service.running:
    - enable: True
    - restart: True
    - watch:
      - file: /etc/systemd/system/opendvs-resolver.service
      - file: /opt/opendvs/resolver.application.properties
      - file: /opt/opendvs/resolver.jar
       
    - require:
      - file: /etc/systemd/system/opendvs-resolver.service
      - file: /opt/opendvs/resolver.application.properties
      - file: /opt/opendvs/resolver.jar
      - sls: java
