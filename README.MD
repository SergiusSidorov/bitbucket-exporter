# Simple utility for exporting repositories from Bitbucket Server (Stash)

Current version #1.0.0 can be downloaded from: https://bitbucket.org/Sergiuss/bitbucket-exporter/downloads/bitbucket-exporter.jar

## Usage

java -jar bitbucket-exporter.jar --user=[user_name] --pass=[password] --host=[bitbucket_host]

## Supported options

Option (* = required) | Description
--- | ---
-?, -h, --help | show help
* -u, --user, --username | name of user with access right to specified bitbucket server
* -p, --pass, --password | password or specified user
* -H, --host, --server-host | bitbucket server host
-b, --bare <Boolean> | create bare copy
-d, -o, --dir, --out <File> | output directory for exported repositories
