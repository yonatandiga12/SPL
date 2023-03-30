
for client:

open termainl in Client :
make 
bin/BGSclient 127.0.0.1 7776


for Server:

The filterd array is in main function in impl -> BGSServer -> ReactorMain or TPCMain. 

write this line inside spl-net folder:
1) 
mvn compile 

2) 
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.TPCMain" -Dexec.args=7776 
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.ReactorMain" -Dexec.args="7776 4" 



Examples:

REGISTER aviv pass 12-12-2005
LOGIN aviv pass 1
POST msg to @rick
FOLLOW 0 rick
PM rick did you get the msg war and Trump ?
LOGSTAT
STAT yona|rick
STAT yona
LOGSTAT
