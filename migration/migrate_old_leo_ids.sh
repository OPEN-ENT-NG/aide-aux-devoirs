#!/bin/bash

if [ $# -eq 0 ]
  then
    echo "Please provide the csv file path as an argument."
fi

DEST_FILE=maxicours_leo_ids.csv
TEMP_FILE=get_new_ids.cypher
STATEMENTS=$(cat $1 | awk '
BEGIN{
	FPAT="([^;]*)|(\"[^\"]+\")"
	FS=";"
}
NR > 1 {
	gsub(/\"/, "", $2)
	gsub(/\"/, "", $1)
	array[$5] = array[$5] "," "\"" tolower($2) " " tolower($1) "\""
}
END {
	for(i in array){
		sub(",","[",array[i])
		print "MATCH (u:User)-[:IN]->(pg:ProfileGroup)-[:DEPENDS]->(s:Structure), (pg)-[:HAS_PROFILE]->(p:Profile) WHERE not (u:User)-[:DUPLICATE]->(:User) and p.name = \"Teacher\" and s.UAI = \"" i "\" and (lower(u.firstName) + \" \" + lower(u.lastName)) IN " array[i] "] RETURN u.lastName, u.firstName, u.id, s.name, s.UAI;"
	}
}')

echo "$STATEMENTS" > $TEMP_FILE

echo "Nom; Prenom; Id Leo 2; Nom de la structure; RNE" > $DEST_FILE
neo4j-shell -file "$TEMP_FILE" | sed 's/^..//;s/.\{2\}$//;s/|/;/g' | awk -F'"' '
BEGIN { OFS="\"" } { for(i = 1; i <= NF; i += 2) { gsub(/[ \t]+/, "", $i); } print }' | awk '
{
	if(substr($1,1,1) == "\"")
	print
}' >> $DEST_FILE

rm $TEMP_FILE
