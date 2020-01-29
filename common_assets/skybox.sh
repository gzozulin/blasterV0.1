cd src/main/resources/textures/$1
mogrify -format jpg *.tga;
mogrify -rotate 90 $1_up.jpg;
mogrify -rotate -90 $1_dn.jpg;
rm *tga
