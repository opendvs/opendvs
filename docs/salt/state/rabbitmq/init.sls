# install EPEL first
include:
  - epel

rabbitmq-server:
  pkg.installed:
    - name: rabbitmq-server
  service.running:
    - enable: True

{% for plugin in salt['pillar.get']("opendvs:rabbitmq:plugins") %}
rabbitmq-plugin-{{ plugin }}:
  rabbitmq_plugin.enabled:
    - name: {{ plugin }}
    - require:
       - service: rabbitmq-server 
{% endfor %}

rabbitmq-vhost-{{ salt['pillar.get']('opendvs:rabbitmq:vhost') }}:
  rabbitmq_vhost.present:
    - name: {{ salt['pillar.get']('opendvs:rabbitmq:vhost') }}
    - require:
       - service: rabbitmq-server

{% for component, data in salt['pillar.get']("opendvs:rabbitmq:users").items() %}
rabbitmq-user-{{ data['username'] }}:
  rabbitmq_user.present:
    - name: {{ data['username'] }}
    - password: {{ data['password'] }}
    - force: True
    - tags: {{ data.get('tags', []) }}
    - perms: 
       - {{ salt['pillar.get']('opendvs:rabbitmq:vhost') }}: {{ data['perms'] }}
    - require:
       - rabbitmq_vhost: rabbitmq-vhost-{{ salt['pillar.get']('opendvs:rabbitmq:vhost') }}

{% endfor %}
