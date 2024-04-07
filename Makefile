all: civl.jar

civl.jar: GMC SARL ABC CIVL
	mv src/CIVL/civl.jar .

GMC: src/GMC
	cd src/GMC && ant

SARL: src/SARL
	cd src/SARL && ant

ABC: src/ABC
	cd src/ABC && ant

CIVL: src/CIVL
	cd src/CIVL && ant

config: civl.jar
	java -jar civl.jar config

clean:
	cd src/GMC && ant clean
	cd src/SARL && ant clean
	cd src/ABC && ant clean
	cd src/CIVL && ant clean
