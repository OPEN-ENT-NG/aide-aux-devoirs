#Aide aux devoirs - Maxicours

* Licence : [AGPL v3](http://www.gnu.org/licenses/agpl.txt) - Copyright Région Hauts-de-France (ex Picardie)
* Développeur(s) : Edifice
* Financeur(s) : Région Hauts-de-France (ex Picardie)


Maxicours widget for ent-core.
Ce widget permet d'afficher dans l'OPEN ENT NG les parcours, leçon du jours et scores du service numérique Edumaxicours.
Spécifique Région Hauts-de-France (ex Picardie).

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
    "webserviceEndpoint": "${maxicoursWsEndpoint}",
    "connectorEndpoint": "${maxicoursConnectorEndpoint}"
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

Add to the springboard conf. file(s) the two fields :

```groovy
maxicoursWsEndpoint=[ENDPOINT]
maxicoursConnectorEndpoint=[ENDPOINT]
```
