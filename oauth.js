console.log('oauth.js loaded');

OAUTH = function(x){
    x= x || {}
    // defaults
    this.url = x.url || 'https://login.microsoftonline.com/common/oauth2/authorize'
    this.clientId= x.clientId || '47bba08c-c2e9-4c09-9fce-08fa22fff29c'
    this.redirect_uri= x.redirect_uri || location.href.replace(/\?$/,'')
    this.login = function(){
        localStorage.setItem('msdnOauthConfig',JSON.stringify(this))
        location.href=this.url+'?response_type=code&redirect_uri='+this.redirect_uri+'&client_id='+this.clientId
        localStorage.log=this.url+'?response_type=tokenId&redirect_uri='+this.redirect_uri+'&client_id='+this.clientId
    }
}
OAUTH.getId=function(){
    var oth = JSON.parse(localStorage.msdnOauth)
    var config = {
        instance: 'https://login.microsoftonline.com/',
        tenant: 'roshanatloutlook.onmicrosoft.com',
        clientId: '47bba08c-c2e9-4c09-9fce-08fa22fff29c',
        postLogoutRedirectUri: location.origin+location.pathname,
        cacheLocation: 'localStorage', // enable this for IE, as sessionStorage does not work for localhost.
    };
    var authContext = new AuthenticationContext(config)
    authContext.login()
    4
}

// in case this is being run for show in the oauth sandbox

if(document.getElementById('oauthDiv')){
    var h = '<h3>Azure AD Auth<i id="gitIcon" class="fa fa-github-alt" aria-hidden="true" style="color:maroon"></i></a> <i id="offIcon" class="fa fa-power-off" aria-hidden="true" style="color:maroon" onclick="location.href=location.href.split(/[?#]/)[0]"></i></h3>'
   
    h +='<div id="oauthFun">...</div>'
    // embelish github icon
    oauthDiv.innerHTML=h
    offIcon.onmouseover=gitIcon.onmouseover=function(){
        this.style.color='red'
    }
    offIcon.onmouseleave=gitIcon.onmouseleave=function(){
        this.style.color='maroon'
    }
    // oauth fun now
    var h = '<h4>OAUTH2 fun</h4>'
    if((location.search.length==0)&(!localStorage.msdnOauth)){ // not logged in
        h += 'You are not logged in <button id="loginBt" class="btn btn-primary">Log In</button>'
        oauthFun.innerHTML=h
        loginBt.onclick=function(){
            (new OAUTH).login()
            //(new OAUTH).getId()
        }
    }else{
        h += 'You are logged in, <button id="logoutBt" class="btn btn-success">Log Out</button>'
        if(location.search.length>0){ // collect login credentials
            var str = location.search.slice(1)
            var oth={}
            str.split('&').forEach(function(av){
                av = av.split('=')
                oth[av[0]]=av[1]
            })
            localStorage.msdnOauth=JSON.stringify(oth)
        }
        oth=JSON.parse(localStorage.msdnOauth)
        if(localStorage.msdnOauthConfig){ // there is more
            var moreOth = JSON.parse(localStorage.msdnOauthConfig)
            for( var a in moreOth){
                oth[a]=moreOth[a]
            }
            localStorage.removeItem('msdnOauthConfig')
            localStorage.setItem('msdnOauth',JSON.stringify(oth))
            localStorage.setItem('msdnOauthLast',JSON.stringify(oth))
        }
        h += '<h5 style="color:blue">Login info found at localStorage.msdnOauth:</h5>'
        h += '<pre>'+JSON.stringify(oth,null,3)+'</pre><span style="color:navy">If you are developing a proxy service, you can now send the <span style="color:green">code value</span> above to your service from where you can get user identity as described in <a href="https://graph.microsoft.io/en-us/docs/authorization/app_authorization" target="_blank">this documentation</a>. That apporach will have your service POST the code value, the application id, application secret and redirect uri to <span style="color:green">https://login.microsoftonline.com/common/oauth2/token HTTP/1.1</span>. If you are instead developing a real Web App (what MS calls a "single page Application"), then we have to take a <a href="https://azure.microsoft.com/en-us/documentation/articles/active-directory-authentication-scenarios/#single-page-application-spa" target="_blank">different route</a>:</span><div id="getId" style="color:red"><div><button button id="getIdBt" class="btn btn-info" >Get ID</button></div> ... not coded yet</div>'   
        oauthFun.innerHTML=h
        logoutBt.onclick=function(){
            localStorage.removeItem('msdnOauth')
            location.search=''
        }
        // getId
        
        getIdBt.onclick=function(){
        	
    		var jsondata = {"code":""};
    		var oth = JSON.parse(localStorage.msdnOauth)
    		jsondata.code=oth.code;
       
    		$.ajax({
                url: 'rest/auth',
    			//url: 'http://localhost:8080/abzooba/parseText',
                type: 'POST',
                data: JSON.stringify(jsondata),
                cache: false,
    			crossDomain : true,
                //dataType: 'json',
                processData: false, // Don't process the files
                contentType: 'application/json', // Set content type to false as jQuery will tell the server its a query string request
                success: function(data, textStatus, jqXHR )
                {
                	if(typeof data.error === 'undefined')
                	{
                	
    				  var tabledata = gettableData(data);
                	}
                	else
                	{
                   	   // Handle errors here
                		console.log('ERRORS: ' + data.error);
                	}
                },
                error: function(jqXHR, textStatus, errorThrown)
                {
                	// Handle errors here
                	console.log('ERRORS: ' + textStatus);
                	// STOP LOADING SPINNER
                }
            });
        
        }
        
    }
}



if(location.href.match('#id_token')){ 
    // if this is a return from a login
    localStorage.msdnOauthToken=location.hash.split('=')[1]
    var config = {
        instance: 'https://login.microsoftonline.com/',
        tenant: 'roshanatloutlook.onmicrosoft.com',
        clientId: '47bba08c-c2e9-4c09-9fce-08fa22fff29c',
        postLogoutRedirectUri: location.origin+location.pathname,
        cacheLocation: 'localStorage', // enable this for IE, as sessionStorage does not work for localhost.
    };
    var authContext = new AuthenticationContext(config)
    4

}

function gettableData(jsonObj) {
	if(document.getElementById('oauthDiv')){
	 h +='<div id="oauthTable">...</div>'
    var html = '<table border="0" class="ocrTable">';
    $.each(jsonObj, function(key, value){
        html += '<tr>';
        html += '<td>' + key + '</td>';
        html += '<td>' + value + '</td>';
        html += '</tr>';
    });
    html += '</table>';
    oauthFun.innerHTML=html;
	}
}