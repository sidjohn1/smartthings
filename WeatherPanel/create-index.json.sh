#!/bin/sh
cd Dropbox/Public/Wallpaper/
echo [ >index.json
ls $1 | while read name
    do
        file --mime-type $name | awk ' BEGIN { ORS = ""; print "{\/\@path\/\@: "; } { print "\/\@"$0"\/\@"; } END { print "},"; }' | sed "s^\"^\\\\\"^g;s^\/\@\/\@^\", \"^g;s^\/\@^\"^g" | sed 's/: /\", \"mime\": "/2'
    done >>index.json
echo {\"path\": \"index.json\", \"mime\": \"text/plain\"} >>index.json
echo ] >>index.json
