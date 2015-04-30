#Aide aux devoirs - Maxicours

Maxicours widget for ent-core.

## Application structure

### Widget part

The widget design and logic is coded in the following files :
- src/main/resources/public/js/maxicours-widget.js
- src/main/resources/public/js/maxicours-widget.html

### Backend part

All the data displayed in the widget is retrieved from Maxicours from their webservice using the SOAP protocol (RPC style).

This logic is implemented in the java part of the application.

## Configuration

####Vertx conf *(/deployment/conf.json.template)*

```json
{
  "name": "org.entcore~maxicours~0.1-SNAPSHOT",
  "config": {
    "main" : "org.entcore.maxicours.Maxicours",
    "port" : 8040,
    "app-name" : "Maxicours",
    "app-address" : "/maxicours",
    "app-icon" : "Maxicours-large",
    "host": "${host}",
    "ssl" : $ssl,
    "auto-redeploy": false,
    "userbook-host": "${host}",
    "integration-mode" : "HTTP",
    "app-registry.port" : 8012,
    "mode" : "${mode}",
    "entcore.port" : 8009,
    "webserviceEndpoint": "",
    "connectorEndpoint": ""
  }
}
```
Two custom fields :
- `webserviceEndpoint`: Maxicours SOAP webservice URL
- `connectorEndpoint`: URL used to connect to Maxicours

####Ent-core conf

Add the widget declaration:

```json
{
    "name": "maxicours",
    "path": "/maxicours/public/template/maxicours-widget.html",
    "js": "/maxicours/public/js/maxicours-widget.js",
    "i18n": "/maxicours/i18n"
}
```
