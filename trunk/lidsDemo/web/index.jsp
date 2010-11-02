<%--
    Document   : ann
    Created on : Oct 28, 2010, 6:38:35 PM
    Author     : ssp
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>LIDS Annotator Interface</title>
        <script type="text/javascript" src="/js/prototype.js"></script>
        <script type="text/javascript">
            function escapeURL(url) {
                return url.replace("/", "%2F", "g").replace("#","%23", "g").replace(":","%3A", "g").replace("?", "%3F", "g").replace("=","%3D", "g");
            }

           function entityToAnnotatorURI(entity) {
                var link = document.createElement("a");
                link.appendChild(document.createTextNode(entity));
                link.setAttribute("href", "http://localhost:8080/annotator?uri=" + escapeURL(entity));
                return link;
            }

            function loadPage() {

            var example_entities = ["http://localhost:8080/universities.rdf#list",
                "http://speiserweb.de/sebastian/foaf.rdf#me",
            "http://harth.org/andreas/foaf#ah"];

            for(var i = 0; i < example_entities.length; i++) {
                var li = document.createElement("li");
                li.appendChild(entityToAnnotatorURI(example_entities[i]));
                document.getElementById('example_links').appendChild(li);
            }
        }

        var oldstr = ""
        function changeSearch() {
            var str = document.getElementById('search').value;
            if(oldstr == str) return;
            if(str.length < 3) {
                return;
            }
            oldstr = str;

            new Ajax.Request('/CachingServlet',
            {
                method:'get',
                parameters: {search: str},
                onSuccess: function(transport){
                    var response = transport.responseText + "\n" + str || str;
                    updateLinks(response);
                },
                onFailure: function(){
                    var response = str;
                    updateLinks(response);
                }
            });
        }
//alert($('search').value.replace("a","Y"));

//if nothing found add normal uri

//                alert(str);
  //              oldstr = str;

        function updateLinks(str) {
            var links = str.split("\n");
            $('found_links').innerHTML = "";
            for(var i=0;i<links.length;i++) {
                if(links[i].replace(" ","","g") == "")
                    continue;
                var li = document.createElement("li");
                li.appendChild(entityToAnnotatorURI(links[i]));
                $('found_links').appendChild(li);
            }
        }
        </script>
    </head>
    <body onload="loadPage();">
        <a style="text-algin:center" href="list.jsp" target="_blank">List of LIDS</a><br /><br />

        <div style="float: left; display: block; padding-left: 100px;">
            Keyword or Entity URI:<br />
            <input type="text" size="80" id="search" onkeyup="changeSearch();" />
            <ul id="found_links">

            </ul>
        </div>
        <div style="float: right; display: block; padding-right: 100px">Example Links
            <ul id="example_links">

            </ul>
        </div>

        <div style="float: right; display: block;">
            <br />
        </div>

    </body>
</html>
