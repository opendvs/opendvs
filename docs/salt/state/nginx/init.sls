{% if grains['os'] == "Ubuntu" %}
nginx_repo:
  pkgrepo.managed:
    - humanname: nginx
    - name: deb http://nginx.org/packages/ubuntu/ {{ grains['oscodename'] }} nginx
    - dist: {{ grains['oscodename'] }}
    - file: /etc/apt/sources.list.d/nginx.list
    - gpgcheck: 1
    - key_url: https://nginx.org/keys/nginx_signing.key
{% endif %}

{% if grains['os'] == "CentOS" %}
nginx_repo:
  pkgrepo.managed:
    - humanname: nginx
    - mirrorlist: http://nginx.org/packages/centos/$releasever/$basearch/
    - gpgcheck: 0
{% endif %}

nginx:
  pkg.installed:
    - require:
      - pkgrepo: nginx_repo
  service.running:
    - require:
      - pkg: nginx
    - enable: True
    - restart: True
    - watch:
      - file: /etc/nginx/conf.d/default.conf

/etc/nginx/conf.d/default.conf:
  file.managed:
    - source: salt://nginx/files/default.conf.jinja
    - user: root
    - group: root
    - template: jinja
    - context: {{ pillar['opendvs'] }}
    - require:
      - pkg: nginx
