<?php 
//NOTE IFrame 'Canvas Session Parameter' must be enabled in facebook advanced app settings.
	$APP_SECRET= "your face book app secret";
	$APP_ID="your face book app id";
	$APP_CANVAS="http://apps.facebook.com/your face book canvas/";
	$APP_SCOPE="";  //permissions requested. See face book api for permissions.
	$APP_FILE="ComDemo.swf";//swf
	$WIDTH="800";
	$HEIGHT="600";

	$token="guest";
	
if (isset($_GET['session']))
{
	

	
	$session= $_GET['session'];
	$sig=strrpos($session,"}");	
	$session= substr($session,1,$sig-1 );

	$rep = array('"','\\');

	$tok = strtok($session, ",");

	$parts=array();
	
	while ($tok !== false) {
		$subParts= explode(":",$tok);		
		$subParts[0]= str_replace($rep, "", $subParts[0]);
		$subParts[1]= str_replace($rep, "", $subParts[1]);
		$parts[$subParts[0]]=$subParts[1];
    	$tok = strtok(",");
	}
	

	ksort($parts);
	$hash_string="";
    $chk="";
	
    foreach ($parts as $k => $v)
    {
        if($k != "sig")
    	$hash_string .= "{$k}={$v}";
        else{
        	$chk=$v;
        }
    }
    	
    $hash_string .= $APP_SECRET;

    if ( strcmp( md5($hash_string), $chk)== 0){
      require_once "TokenGen.php";
      
 		$isAdmin=false;	
		$moreData=array();
		//$moreData=PHP_LoadUserData($parts['uid']);
		//$isAdmin=$moreData['is_admin'];	
		$moreData['is_cool']="true";
		//$moreData['loves_red5']="true";
		
		$token=TokenGen::generateToken( $parts['uid'], $isAdmin,$moreData );
    }else{
    	$token="bad_session";
    }
    
}
?>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://www.facebook.com/2008/fbml">
	<head>
	 	<!-- Include support librarys first -->
		<script type="text/javascript" src="swfobject.js"></script>
		<script type="text/javascript" src="http://connect.facebook.net/en_US/all.js"></script>
		<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>	
		<!-- Include FBJSBridge to allow for SWF to Facebook communication. -->
		<script type="text/javascript" src="FBJSBridge.js?<? echo(time()) ?>"></script>
		<script type="text/javascript">
		
			function embedPlayer() {

				var flashvars = {allowFullscreen:"true",token:"<?php echo $token;?>", key:"<?php echo $token2;?>" };

				var params = window.location.toString().slice(window.location.toString().indexOf('?'));
				embedSWF("<? echo $APP_FILE; ?>?<? echo(time()); ?>", "ComDemo", "<? echo $WIDTH; ?>", "<? echo $HEIGHT; ?>", "10.0",flashvars,params);
			}
			
			function init() {
				embedPlayer();
			}

			function redirect() {
				var params = window.location.toString().slice(window.location.toString().indexOf('?'));
				top.location = 'https://graph.facebook.com/oauth/authorize?client_id=<?php echo $APP_ID;?>&scope=<?php echo $APP_SCOPE;?>&redirect_uri=<?php echo $APP_CANVAS;?>'+params;
			}

			$(init);
		</script>
  </head>
  <body>
<div id="fb-root">
</div>
  <div id="ComDemo">
        <h1>ComDemo</h1>
        <p></p>
  </div>
</body>
</html>

