{% if grains['os'] == "Ubuntu" %}
oracle_jdk_repo:
  pkgrepo.managed:
    - humanname: WebUpd8team Java
    - name: deb http://ppa.launchpad.net/webupd8team/java/ubuntu {{ grains['oscodename'] }} main
    - dist: {{ grains['oscodename'] }}
    - file: /etc/apt/sources.list.d/webup8team-java.list
    - keyid: EEA14886
    - keyserver: keyserver.ubuntu.com

{% if salt['pillar.get']("java:accept_licence", False) %}
debconf-utils:
  pkg.installed

oracle-java8-installer:
  debconf.set:
    - data:
        'shared/accepted-oracle-license-v1-1': {'type': 'boolean', 'value': True}
    - require_in:
        - pkg: oracle_jdk
    - require:
        - pkg: debconf-utils
{% endif %}

oracle_jdk:
  pkg.installed:
    - name: oracle-java8-installer
    - require:
        - pkgrepo: oracle_jdk_repo
{% endif %}
