<?php
class TokenGen{
	
	static $SECRET="I_pick_my_nose_and _eat_it_too";
	

	static function generateToken( $uid,$isAdmin,$params ){
		
		$time=time();

		$compound="";
		
		if($params){
		
			ksort($params);
		
			foreach($params as $k=>$v){
				$compound = $compound."-".$k."=".$v;
			}
		}
		
		$type=($isAdmin === true)?"2":"1";
		

		$token= md5(  $time.$uid.$type.$compound.TokenGen::$SECRET );	
		
		return $token."-".$time."-".$uid."-".$type.$compound;
	}
	
}
?>