{% if grains['os_family'] == "CentOS" %}
epel-release:
  pkg.installed:
    - require_in:
        - pkg: rabbitmq-server
        - pkg: mysql-server
{% endif %}
