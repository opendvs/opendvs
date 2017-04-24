include:
  - nginx

{{ pillar['opendvs']['ui']['location'] }}:
  file.directory:
    - mode: 755
    - user: nginx
    - group: nginx
    - makedirs: True

extract-ui:
  archive.extracted:
    - name: {{ pillar['opendvs']['ui']['location'] }}
    - source: {{ pillar['opendvs']['ui']['zip_location'] }}
    - overwrite: True
    - clean: True
    - enforce_toplevel: False
    - archive_format: zip
    - user: nginx
    - group: nginx
    - require:
       - sls: nginx
