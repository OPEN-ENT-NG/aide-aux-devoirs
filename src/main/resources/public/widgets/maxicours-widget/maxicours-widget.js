var maxicoursWidget = model.widgets.findWidget("maxicours-widget");
maxicoursWidget.controllerData = {}

maxicoursWidget.showWidget = function(){
    return model.me.type === 'ELEVE'
}

maxicoursWidget.loading = function(mode){
    this.loads = mode
    model.widgets.apply()
}

maxicoursWidget.authProcess = function(hook){
    maxicoursWidget.getUserStatus(function(){ maxicoursWidget.getUserInfo(hook) })
}

maxicoursWidget.initAuthProcess = function(){
    var delay = 2000
    var countdown = 5
    maxicoursWidget.loading(true)
    var timeoutFunction = function(){
        if(maxicoursWidget.controllerData.id < 0 && countdown-- > 0){
            maxicoursWidget.authProcess()
            setTimeout(timeoutFunction, delay)
        } else {
            maxicoursWidget.loading(false)
        }
    }
    setTimeout(timeoutFunction, delay)
}

maxicoursWidget.getConf = function(){
    http().getJson('/maxicours/conf')
        .done(function(data){
            for(var prop in data){
                if(data.hasOwnProperty(prop))
                    maxicoursWidget.controllerData[prop] = data[prop]
            }
        })
        .error( function() {
          console.log('HTTP error on /maxicours/conf. aide-aux-devoirs widget will not load.');
        })
    return maxicoursWidget
}

maxicoursWidget.getUserStatus = function(hook){
    http().get('/maxicours/getUserStatus')
        .done(function(xml){
            var xmlDocument = $.parseXML(xml),
            $xml = $(xmlDocument)

            maxicoursWidget.controllerData.id  = $xml.find("mxcId").text()

            model.widgets.apply()

            if(typeof hook === "function")
                hook()
        })
        .error( function() {
          console.log('HTTP error on /maxicours/getUserStatus. aide-aux-devoirs widget  will not load.');
        })
    return maxicoursWidget
}

maxicoursWidget.getUserInfo = function(hook){
    if( (typeof maxicoursWidget.controllerData.id === "string" && maxicoursWidget.controllerData.id.trim().length === 0)
            || maxicoursWidget.controllerData.id < 0 ){
        if(typeof hook === 'function')
            hook()
        return;
    }

    http().get('/maxicours/getUserInfo/'+maxicoursWidget.controllerData.id)
        .done(function(xml){
            var xmlDocument = $.parseXML(xml),
            $xml = $(xmlDocument)

            var getText = function(xml, tagName, parent){
                var tags = parent ? xml.find(parent).find(tagName) : xml.find(tagName)
                return tags.text()
            }
            var getContent = function(xml, tagName, parent){
                var tags = parent ? xml.find(parent).find(tagName) : xml.find(tagName)
                return tags.contents()
            }

            var jsonifyArray = function(arrayTag, type){
                return _.map($(arrayTag).children(), function(item){
                    if(type === "simple"){
                        return $(item).text()
                    }
                    else if(type === "complex"){
                        var serialized = {}
                        var children = $(item).children()
                        for(var i = 0; i < children.length; i++){
                            serialized[children[i].nodeName] = children[i].textContent
                        }
                        return serialized
                    }
                })
            }

            maxicoursWidget.controllerData.userInfo = {
                hasAnActiveAccount: getText($xml, "hasAnActiveAccount"),
                activityScore: getText($xml,"activityScore"),
                hasSessionOfTheDay: getText($xml,"hasSessionOfTheDay"),
                sessionOfTheDayUrl: getText($xml,"sessionOfTheDayUrl"),
                sessionOfTheDayActivities: jsonifyArray($xml.find("sessionOfTheDayActivities"), "complex"),
                hasPersonnalCourses: getText($xml,"hasPersonnalCourses"),
                newPersonnalCourses: jsonifyArray($xml.find("newPersonnalCourses"), "complex"),
                currentPersonnalCourses: jsonifyArray($xml.find("currentPersonnalCourses"), "complex")
            }

            model.widgets.apply()
        }).xhr.always(function(){
            if(typeof hook === 'function')
                hook()
        })
    return maxicoursWidget
}

////    INIT    ////

maxicoursWidget.getConf().authProcess(function(){
    maxicoursWidget.loading(false)
    model.widgets.apply()
})
