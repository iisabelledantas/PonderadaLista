package com.inteli.telemetria.Utils

fun formartUrl(endpoint : String, nomedb : String ) : String{
    return "jdbc:postgresql://$endpoint:5432/$nomedb"
}