<%-- 
    Document   : index
    Created on : Dec 16, 2010, 4:07:05 PM
    Author     : ssp
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Linkedin LIDS Wrapper</title>
    </head>
    <body>
        <h1>Linkedin LIDS Wrapper</h1>
        <p>In order to use the wrapper, you have to <a href="user_auth/authorize">authorise</a> it. You will be redirected to Linkedin. We will not get your password, just an access token, that lets the wrapper access LinkedIn on your behalf. You can revoke this token anytime.
        </p>
        <p>If you already have your credentials, you can enter them here to test the wrapper.</p>
        <form action="test_wrapper" method="post" target="_blank">
            Access Token: <input type="text" name="access_token" /><br />
            Access Secret: <input type="text" name="access_secret" /><br />
            Wrapper URI: <input type="text" name="wrapper_uri" /><br />
            <input type="submit" />
        </form>
    </body>
</html>
