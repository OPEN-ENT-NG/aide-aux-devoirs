var maxicoursWidget = model.widgets.findWidget("maxicours");
maxicoursWidget.controllerData = {}

maxicoursWidget.getConf = function(){
    http().getJson('/maxicours/conf')
        .done(function(data){
            for(prop in data){
                if(data.hasOwnProperty(prop))
                    maxicoursWidget.controllerData[prop] = data[prop]
            }
        })
    return maxicoursWidget
}

maxicoursWidget.getUserStatus = function(hook){
    http().get('/maxicours/getUserStatus')
        .done(function(xml){
            xmlDocument = $.parseXML(xml),
            $xml = $(xmlDocument)

            maxicoursWidget.controllerData.id  = $xml.find("mxcId").text()

            model.widgets.apply()

            if(typeof hook === "function")
                hook()
        })
    return maxicoursWidget
}

maxicoursWidget.getUserInfo = function(){
    if(maxicoursWidget.controllerData.id < 0)
        return;

    http().get('/maxicours/getUserInfo/'+maxicoursWidget.controllerData.id)
        .done(function(xml){
            xmlDocument = $.parseXML(xml),
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
        })
    return maxicoursWidget
}

////    INIT    ////

maxicoursWidget
    .getConf()
    .getUserStatus(
        maxicoursWidget.getUserInfo
    )
