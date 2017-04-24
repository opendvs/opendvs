include:
  - epel

python-mysqldb:
  pkg.installed:
    - require_in:
        - pkg: mysql-server

mysql-server:
  pkg.installed:
    - name: mysql-server
  service.running:
    - name: mysql
    - enable: True
    - require:
        - pkg: mysql-server

mysql-database-{{ salt['pillar.get']("opendvs:mysql:database") }}:
  mysql_database.present:
    - name: {{ salt['pillar.get']("opendvs:mysql:database") }}
    - character_set: utf8
    - require:
        - service: mysql-server

{% for component,data in salt['pillar.get']("opendvs:mysql:users").items() %}
mysql-user-{{ data['username'] }}:
  mysql_user.present:
    - name: {{ data['username'] }}
    - password: {{ data['password'] }}
    - host: {{ data['host'] }}
    - force: True
    - require:
        - service: mysql-server

mysql-user-grants-{{ data['username'] }}:
  mysql_grants.present:
    - user: {{ data['username'] }}
    - host: {{ data['host'] }}
    - database: {{ salt['pillar.get']('opendvs:mysql:database') }}.*
    - grant: all privileges
    - require:
       - mysql_user: mysql-user-{{ data['username'] }}
       - mysql_database: mysql-database-{{ salt['pillar.get']('opendvs:mysql:database') }}
{% endfor %}

